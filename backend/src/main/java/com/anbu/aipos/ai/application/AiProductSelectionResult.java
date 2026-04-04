package com.anbu.aipos.ai.application;

import java.util.List;

public record AiProductSelectionResult(
        List<AiSemanticProductMatch> matches
) {
}
