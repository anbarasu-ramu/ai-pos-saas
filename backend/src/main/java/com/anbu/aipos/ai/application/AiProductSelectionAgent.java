package com.anbu.aipos.ai.application;

import com.anbu.aipos.core.port.in.product.ProductView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiProductSelectionAgent {

    private static final int MAX_PRODUCTS_FOR_PROMPT = 80;
    private static final Logger log = LoggerFactory.getLogger(AiProductSelectionAgent.class);

    private final OllamaClient ollamaClient;

    public AiProductSelectionAgent(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public AiProductSelectionResult selectProducts(
            String query,
            List<ProductView> catalog,
            boolean activeOnly,
            int limit
    ) {
        List<ProductView> scopedCatalog = catalog.stream()
                .filter(product -> !activeOnly || Boolean.TRUE.equals(product.active()))
                .sorted(Comparator.comparing(ProductView::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (scopedCatalog.isEmpty()) {
            return new AiProductSelectionResult(List.of());
        }

        List<AiSemanticProductMatch> deterministicMatches = rankDeterministicMatches(query, scopedCatalog);
        List<AiSemanticProductMatch> semanticMatches = rankSemanticMatches(query, scopedCatalog, limit);

        Map<Long, AiSemanticProductMatch> mergedMatches = new LinkedHashMap<>();
        deterministicMatches.forEach(match -> mergedMatches.put(match.productId(), match));
        semanticMatches.forEach(match -> mergedMatches.merge(match.productId(), match, this::pickBetterMatch));

        List<AiSemanticProductMatch> finalMatches = mergedMatches.values().stream()
                .sorted(Comparator
                        .comparingDouble(AiSemanticProductMatch::confidence).reversed()
                        .thenComparing(AiSemanticProductMatch::productId))
                .limit(limit)
                .toList();

        return new AiProductSelectionResult(finalMatches);
    }

    private List<AiSemanticProductMatch> rankSemanticMatches(String query, List<ProductView> scopedCatalog, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<ProductView> promptCatalog = scopedCatalog.stream()
                .limit(MAX_PRODUCTS_FOR_PROMPT)
                .toList();

        try {
            AiProductSelectionResult rawSelection = ollamaClient.selectProducts(query, promptCatalog, limit);
            Set<Long> knownProductIds = promptCatalog.stream()
                    .map(ProductView::id)
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());

            return rawSelection.matches().stream()
                    .filter(match -> knownProductIds.contains(match.productId()))
                    .limit(limit)
                    .toList();
        } catch (AiInvalidModelResponseException | AiModelUnavailableException ex) {
            log.info("Falling back to deterministic product selection for query '{}': {}", query, ex.getMessage());
            return List.of();
        }
    }

    private List<AiSemanticProductMatch> rankDeterministicMatches(String query, List<ProductView> catalog) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedQuery = normalize(query);
        List<String> queryTokens = tokens(normalizedQuery);
        if (queryTokens.isEmpty()) {
            return List.of();
        }

        List<AiSemanticProductMatch> matches = new ArrayList<>();
        for (ProductView product : catalog) {
            if (product.id() == null) {
                continue;
            }

            RankedMatch rankedMatch = scoreProduct(product, normalizedQuery, queryTokens);
            if (rankedMatch == null) {
                continue;
            }

            matches.add(new AiSemanticProductMatch(
                    product.id(),
                    rankedMatch.confidence(),
                    rankedMatch.reason(),
                    rankedMatch.matchType()));
        }

        return matches.stream()
                .sorted(Comparator
                        .comparingDouble(AiSemanticProductMatch::confidence).reversed()
                        .thenComparing(AiSemanticProductMatch::productId))
                .toList();
    }

    private RankedMatch scoreProduct(ProductView product, String normalizedQuery, List<String> queryTokens) {
        String normalizedName = normalize(product.name());
        String normalizedCategory = normalize(product.category());
        List<String> nameTokens = tokens(normalizedName);
        List<String> categoryTokens = tokens(normalizedCategory);

        if (normalizedName.equals(normalizedQuery)) {
            return new RankedMatch(1.0d, "Exact product name match.", "EXACT_NAME");
        }
        if (!normalizedCategory.isBlank() && normalizedCategory.equals(normalizedQuery)) {
            return new RankedMatch(0.98d, "Exact product category match.", "EXACT_CATEGORY");
        }
        if (normalizedName.startsWith(normalizedQuery)) {
            return new RankedMatch(0.94d, "Product name starts with the query.", "NAME_PREFIX");
        }
        if (!normalizedCategory.isBlank() && normalizedCategory.startsWith(normalizedQuery)) {
            return new RankedMatch(0.92d, "Product category starts with the query.", "CATEGORY_PREFIX");
        }
        if (normalizedName.contains(normalizedQuery)) {
            return new RankedMatch(0.88d, "Product name contains the query.", "NAME_CONTAINS");
        }
        if (!normalizedCategory.isBlank() && normalizedCategory.contains(normalizedQuery)) {
            return new RankedMatch(0.84d, "Product category contains the query.", "CATEGORY_CONTAINS");
        }

        double overlap = tokenOverlap(queryTokens, nameTokens, categoryTokens);
        if (overlap >= 0.50d) {
            return new RankedMatch(
                    Math.min(0.80d, 0.58d + overlap * 0.35d),
                    "Product shares the strongest keyword overlap with the query.",
                    "TOKEN_OVERLAP");
        }

        return null;
    }

    private AiSemanticProductMatch pickBetterMatch(AiSemanticProductMatch left, AiSemanticProductMatch right) {
        if (right.confidence() > left.confidence()) {
            return right;
        }
        if (right.confidence() == left.confidence() && !"SEMANTIC".equals(left.matchType())) {
            return left;
        }
        return left.confidence() >= right.confidence() ? left : right;
    }

    private double tokenOverlap(List<String> queryTokens, List<String> nameTokens, List<String> categoryTokens) {
        Set<String> productTokens = new java.util.LinkedHashSet<>();
        productTokens.addAll(nameTokens);
        productTokens.addAll(categoryTokens);
        if (productTokens.isEmpty()) {
            return 0.0d;
        }

        long matches = queryTokens.stream()
                .filter(productTokens::contains)
                .count();
        return matches / (double) queryTokens.size();
    }

    private List<String> tokens(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split("\\s+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private record RankedMatch(
            double confidence,
            String reason,
            String matchType
    ) {
    }
}
