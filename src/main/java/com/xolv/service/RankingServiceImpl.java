package com.xolv.service;

import com.xolv.cache.CacheService;
import com.xolv.engine.SearchEngine;
import com.xolv.exception.SearchEngineException;
import com.xolv.model.RankingResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    private static final Logger logger =
            Logger.getLogger(RankingServiceImpl.class.getName());

    private final Map<String, SearchEngine> engines;
    private final CacheService cache;

    public RankingServiceImpl(List<SearchEngine> engineList,
                              CacheService cache) {
        this.cache = cache;
        this.engines = engineList.stream()
                .collect(Collectors.toMap(
                        this::resolveEngineName,
                        e -> e
                ));
    }

    @Override
    public RankingResult getRankings(String engine, String keyword) {

        validateInput(engine, keyword);

        String normalizedEngine = engine.toLowerCase();
        String normalizedKeyword = keyword.toLowerCase();

        String cacheKey = normalizedEngine + ":" + normalizedKeyword;
        String cached = cache.get(cacheKey);
        if (Objects.nonNull(cached)) {
            logger.info("Cache hit for key: " + cacheKey);
            return new RankingResult(keyword, normalizedEngine, cached);
        }

        SearchEngine searchEngine = engines.get(normalizedEngine);
        if (Objects.isNull(searchEngine)) {
            throw new SearchEngineException("Unsupported search engine: " + engine);
        }

        List<String> urls = searchEngine.search(normalizedKeyword);

        List<Integer> positions = findPositions(urls, normalizedKeyword);

        String result = formatPositions(positions);

        cache.put(cacheKey, result);

        return new RankingResult(keyword, normalizedEngine, result);
    }

    private List<Integer> findPositions(List<String> urls, String keyword) {

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i).toLowerCase();
            if (url.contains(keyword)) {
                positions.add(i);
            }
        }
        return positions;
    }

    private String formatPositions(List<Integer> positions) {
        if (positions == null || positions.isEmpty()) {
            return "0";
        }

        return positions.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private void validateInput(String engine, String keyword) {
        if (engine == null || engine.isBlank()) {
            throw new SearchEngineException("Engine must not be empty");
        }
        if (keyword == null || keyword.isBlank()) {
            throw new SearchEngineException("Keyword must not be empty");
        }
    }

    private String resolveEngineName(SearchEngine engine) {
        return engine.getClass()
                .getSimpleName()
                .replace("SearchEngine", "")
                .toLowerCase();
    }
}