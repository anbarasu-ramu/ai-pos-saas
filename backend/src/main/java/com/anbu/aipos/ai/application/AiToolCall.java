package com.anbu.aipos.ai.application;

import java.util.Map;

public record AiToolCall(
        AiTool tool,
        Map<String, Object> arguments
) {
}
