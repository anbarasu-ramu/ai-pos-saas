package com.anbu.aipos.ai.application;

import com.anbu.aipos.ai.config.OllamaProperties;
import com.anbu.aipos.core.port.in.product.ProductView;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final RestClient restClient;
    private final OllamaProperties properties;
    private final AiModelDecisionParser parser;
    private final AiProductSelectionParser productSelectionParser;

    public OllamaClient(
            RestClient.Builder restClientBuilder,
            OllamaProperties properties,
            AiModelDecisionParser parser,
            AiProductSelectionParser productSelectionParser
    ) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
        this.parser = parser;
        this.productSelectionParser = productSelectionParser;
    }

    public AiModelDecision plan(
            String userMessage,
            AiExecutionContext context,
            List<AiToolDescriptor> availableTools
    ) {
        try {
            OllamaGenerateRequest request = new OllamaGenerateRequest(
                    properties.model(),
                    buildPrompt(userMessage, context, availableTools),
                    false,
                    "json");

            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (response == null || response.response() == null || response.response().isBlank()) {
                throw new AiInvalidModelResponseException("Ollama returned an empty response.");
            }

            return parser.parse(response.response());
        } catch (AiInvalidModelResponseException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.warn("Ollama is unavailable, falling back to local AI planner: {}", ex.getMessage());
            throw new AiModelUnavailableException("Ollama is unavailable.", ex);
        }
    }

    public AiProductSelectionResult selectProducts(
            String query,
            List<ProductView> catalog,
            int limit
    ) {
        try {
            OllamaGenerateRequest request = new OllamaGenerateRequest(
                    properties.model(),
                    buildProductSelectionPrompt(query, catalog, limit),
                    false,
                    "json");

            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (response == null || response.response() == null || response.response().isBlank()) {
                throw new AiInvalidModelResponseException("Ollama returned an empty semantic product selection.");
            }

            return productSelectionParser.parse(response.response());
        } catch (AiInvalidModelResponseException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.warn("Ollama is unavailable for semantic product selection: {}", ex.getMessage());
            throw new AiModelUnavailableException("Ollama is unavailable for semantic product selection.", ex);
        }
    }

    private String buildPrompt(
            String userMessage,
            AiExecutionContext context,
            List<AiToolDescriptor> availableTools
    ) {
        return """
                You are an AI planning service for a tenant-scoped POS backend.
                Return JSON only. Do not include markdown fences.

                Allowed tools:
                %s

                Current user context:
                - tenantId: %s
                - tenantName: %s
                - username: %s
                - email: %s
                - roles: %s

                Rules:
                - Never invent tools outside the allowed list.
                - Never invent tenant IDs or user IDs.
                - Use toolCalls only when a backend capability is needed.
                - Use requiresConfirmation=true for CREATE_CHECKOUT_ORDER unless the user has already explicitly confirmed.
                - If the user asks for an unsupported capability, return no toolCalls and explain briefly.
                - Prefer one tool call unless multiple are clearly necessary.

                Return exactly this JSON shape:
                {
                  "assistantMessage": "short helpful message",
                  "intent": "ONE_OF_THE_TOOL_NAMES_OR_TEXT_RESPONSE",
                  "toolCalls": [
                    {
                      "tool": "GET_LOW_STOCK_PRODUCTS",
                      "arguments": { "threshold": 5 }
                    }
                  ],
                  "requiresConfirmation": false
                }

                User message:
                %s
                """.formatted(
                availableTools.stream()
                        .map(descriptor -> "- %s | readOnly=%s | %s | args=%s".formatted(
                                descriptor.name().name(),
                                descriptor.readOnly(),
                                descriptor.description(),
                                descriptor.arguments()))
                        .reduce((left, right) -> left + "\n" + right)
                        .orElse(""),
                context.tenantId(),
                context.tenantName(),
                context.username(),
                context.email(),
                context.roles(),
                userMessage
        );
    }

    private String buildProductSelectionPrompt(String query, List<ProductView> catalog, int limit) {
        String catalogText = catalog.stream()
                .map(product -> """
                        - {"productId": %d, "name": "%s", "category": "%s", "price": %s, "stockQuantity": %d, "active": %s}
                        """.formatted(
                        product.id(),
                        escape(product.name()),
                        escape(product.category()),
                        product.price(),
                        product.stockQuantity(),
                        product.active()))
                .reduce((left, right) -> left + right)
                .orElse("");

        return """
                You are a semantic product selection agent for a POS system.
                Return JSON only. Do not include markdown fences.

                User query:
                %s

                Candidate products:
                %s

                Rules:
                - Only return productIds from the candidate list.
                - Never invent productIds.
                - Rank the best semantic matches for the user query.
                - Prefer exact meaning over surface wording.
                - Return at most %d matches.
                - If nothing is relevant, return {"matches":[]}.

                Response shape:
                {
                  "matches": [
                    { "productId": 23, "confidence": 0.92, "reason": "Closest dessert match for choco lava cake" }
                  ]
                }
                """.formatted(query, catalogText, limit);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record OllamaGenerateRequest(
            String model,
            String prompt,
            boolean stream,
            String format
    ) {
    }

    private record OllamaGenerateResponse(
            String response
    ) {
    }
}
