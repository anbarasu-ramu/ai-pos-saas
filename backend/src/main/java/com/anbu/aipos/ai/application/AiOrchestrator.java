package com.anbu.aipos.ai.application;

import com.anbu.aipos.application.tenant.TenantQueryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AiOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AiOrchestrator.class);

    private final OllamaClient ollamaClient;
    private final LocalAiPlanner localAiPlanner;
    private final AiToolRegistry toolRegistry;
    private final AiToolExecutionPolicy executionPolicy;
    private final AiToolExecutor toolExecutor;
    private final TenantQueryService tenantQueryService;

    public AiOrchestrator(
            OllamaClient ollamaClient,
            LocalAiPlanner localAiPlanner,
            AiToolRegistry toolRegistry,
            AiToolExecutionPolicy executionPolicy,
            AiToolExecutor toolExecutor,
            TenantQueryService tenantQueryService
    ) {
        this.ollamaClient = ollamaClient;
        this.localAiPlanner = localAiPlanner;
        this.toolRegistry = toolRegistry;
        this.executionPolicy = executionPolicy;
        this.toolExecutor = toolExecutor;
        this.tenantQueryService = tenantQueryService;
    }

    public AiChatResponse handle(String userMessage, Jwt jwt) {
        AiExecutionContext context = buildContext(jwt);
        log.info(
                "AI request received tenantId={} username={} message={}",
                context.tenantId(),
                context.username(),
                safeMessage(userMessage));
        AiModelDecision decision = plan(userMessage, context);
        log.info(
                "AI plan selected intent={} toolCalls={} requiresConfirmation={}",
                decision.intent(),
                decision.toolCalls() == null ? 0 : decision.toolCalls().size(),
                decision.requiresConfirmation());

        if (decision.toolCalls() == null || decision.toolCalls().isEmpty()) {
            return new AiChatResponse(
                    defaultAssistantMessage(decision),
                    decision.intent(),
                    List.of(),
                    Map.of(),
                    false
            );
        }

        if (executionPolicy.requiresConfirmation(decision, userMessage)) {
            return new AiChatResponse(
                    decision.assistantMessage() == null || decision.assistantMessage().isBlank()
                            ? "I’m ready to do that, but I need your explicit confirmation before executing it."
                            : decision.assistantMessage(),
                    decision.intent(),
                    executionPolicy.confirmationRequiredInvocations(decision.toolCalls()),
                    Map.of(),
                    true
            );
        }

        Object lastResult = Map.of();
        String assistantMessage = defaultAssistantMessage(decision);
        List<AiToolInvocation> invocations = new java.util.ArrayList<>();

        for (AiToolCall toolCall : decision.toolCalls()) {
            try {
                log.info("Executing AI tool {} with args={}", toolCall.tool(), toolCall.arguments());
                AiToolExecutionResult executionResult = toolExecutor.execute(toolCall, context);
                invocations.add(new AiToolInvocation(toolCall.tool().name(), toolCall.arguments(), "SUCCESS", null));
                lastResult = executionResult.result();
                assistantMessage = executionResult.summary();
                if (executionResult.clarification()) {
                    log.info("AI tool {} returned clarification response", toolCall.tool());
                } else {
                    log.info("AI tool {} completed successfully", toolCall.tool());
                }
            } catch (RuntimeException ex) {
                log.warn("AI tool execution failed for {}: {}", toolCall.tool(), ex.getMessage());
                invocations.add(new AiToolInvocation(toolCall.tool().name(), toolCall.arguments(), "FAILED", ex.getMessage()));
                return new AiChatResponse(
                        "I couldn’t complete that request safely: " + ex.getMessage(),
                        decision.intent(),
                        invocations,
                        lastResult,
                        false
                );
            }
        }

        return new AiChatResponse(
                assistantMessage,
                decision.intent(),
                invocations,
                lastResult,
                false
        );
    }

    private AiModelDecision plan(String userMessage, AiExecutionContext context) {
        AiModelDecision deterministicDecision = localAiPlanner.plan(userMessage);
        if (!"TEXT_RESPONSE".equals(deterministicDecision.intent())) {
            log.info("AI planning source=LOCAL_DETERMINISTIC intent={}", deterministicDecision.intent());
            return deterministicDecision;
        }

        try {
            AiModelDecision decision = ollamaClient.plan(userMessage, context, toolRegistry.list());
            log.info("AI planning source=OLLAMA intent={}", decision.intent());
            return decision;
        } catch (AiModelUnavailableException ex) {
            log.info("AI planning source=LOCAL_FALLBACK intent={} reason={}", deterministicDecision.intent(), ex.getMessage());
            return deterministicDecision;
        } catch (AiInvalidModelResponseException ex) {
            log.warn("AI model returned invalid output: {}", ex.getMessage());
            return new AiModelDecision(
                    "I couldn’t trust the AI planner response, so I did not execute any actions. Please try again.",
                    "TEXT_RESPONSE",
                    List.of(),
                    false
            );
        }
    }

    private AiExecutionContext buildContext(Jwt jwt) {
        String tenantId = resolveTenantId(jwt);
        String tenantName = null;
        if (tenantId != null && !tenantId.isBlank()) {
            try {
                tenantName = tenantQueryService.findNameById(UUID.fromString(tenantId));
            } catch (RuntimeException ex) {
                log.debug("Unable to resolve tenant name for {}: {}", tenantId, ex.getMessage());
            }
        }

        return new AiExecutionContext(
                tenantId,
                tenantName,
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                extractRoles(jwt)
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> claims)) {
            return List.of();
        }

        Object roles = claims.get("roles");
        if (!(roles instanceof List<?> roleList)) {
            return List.of();
        }

        return roleList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(role -> !role.isBlank())
                .toList();
    }

    private String resolveTenantId(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = jwt.getClaimAsString("tenant");
        }
        return tenantId;
    }

    private String defaultAssistantMessage(AiModelDecision decision) {
        return decision.assistantMessage() == null ? "" : decision.assistantMessage();
    }

    private String safeMessage(String userMessage) {
        if (userMessage == null) {
            return "";
        }
        String trimmed = userMessage.replaceAll("\\s+", " ").trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(0, 200) + "...";
    }
}
