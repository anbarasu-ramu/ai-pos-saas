package com.anbu.aipos.ai.web;

import com.anbu.aipos.common.web.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        return new ApiResponse<>(
                "AI orchestration scaffold is ready.",
                Map.of(
                        "message", "Wire this endpoint to Ollama + tool execution next.",
                        "userPrompt", request.message(),
                        "suggestedActions", List.of("SEARCH_PRODUCT", "CREATE_ORDER", "LOW_STOCK_ALERT")));
    }

    public record ChatRequest(@NotBlank String message) {
    }
}
