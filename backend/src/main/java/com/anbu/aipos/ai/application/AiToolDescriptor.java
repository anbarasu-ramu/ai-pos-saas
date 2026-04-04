package com.anbu.aipos.ai.application;

public record AiToolDescriptor(
        AiTool name,
        boolean readOnly,
        String description,
        String arguments
) {
}
