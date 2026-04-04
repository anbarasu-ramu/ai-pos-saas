package com.anbu.aipos.ai.web;

import com.anbu.aipos.ai.application.AiChatResponse;
import com.anbu.aipos.ai.application.AiOrchestrator;
import com.anbu.aipos.common.web.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiOrchestrator aiOrchestrator;

    public AiChatController(AiOrchestrator aiOrchestrator) {
        this.aiOrchestrator = aiOrchestrator;
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AiChatResponse response = aiOrchestrator.handle(request.message(), jwt);
        return new ApiResponse<>(response.assistantMessage(), response);
    }

    public record ChatRequest(@NotBlank String message) {
    }
}
