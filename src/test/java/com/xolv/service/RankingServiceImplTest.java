package com.xolv.service;

import com.xolv.cache.CacheService;
import com.xolv.engine.GoogleSearchEngine;
import com.xolv.engine.SearchEngine;
import com.xolv.exception.SearchEngineException;
import com.xolv.model.RankingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RankingServiceImplTest {

    private CacheService cache;
    private SearchEngine googleEngine;
    private RankingServiceImpl service;

    @BeforeEach
    void setUp() {
        cache = mock(CacheService.class);

        googleEngine = mock(GoogleSearchEngine.class);

        service = new RankingServiceImpl(
                List.of(googleEngine),
                cache
        );
    }

    @Test
    void shouldReturnPositions_whenKeywordFound() {

        when(cache.get("google:xolv")).thenReturn(null);

        when(googleEngine.search("xolv")).thenReturn(List.of(
                "https://xolv.com",
                "https://linkedin.com",
                "https://xolv.io"
        ));

        RankingResult result = service.getRankings("google", "xolv");

        assertEquals("0, 2", result.positions());

        verify(cache).put("google:xolv", "0, 2");
    }

    @Test
    void shouldReturnCachedResult_whenCacheHit() {

        when(cache.get("google:xolv")).thenReturn("1, 5");

        RankingResult result = service.getRankings("google", "xolv");

        assertEquals("1, 5", result.positions());

        verify(googleEngine, never()).search(any());
        verify(cache, never()).put(any(), any());
    }

    @Test
    void shouldThrowException_whenEngineNotSupported() {

        when(cache.get("bing:xolv")).thenReturn(null);

        assertThrows(SearchEngineException.class, () ->
                service.getRankings("bing", "xolv")
        );
    }

    @Test
    void shouldReturnZero_whenNoMatchFound() {

        when(cache.get("google:test")).thenReturn(null);

        when(googleEngine.search("test")).thenReturn(List.of(
                "https://linkedin.com",
                "https://github.com"
        ));

        RankingResult result = service.getRankings("google", "test");

        assertEquals("0", result.positions());
    }

    @Test
    void shouldHandleCaseInsensitiveKeyword() {

        when(cache.get("google:xolv")).thenReturn(null);

        when(googleEngine.search("xolv")).thenReturn(List.of(
                "https://XOLV.com",
                "https://example.com"
        ));

        RankingResult result = service.getRankings("GOOGLE", "XOLV");

        assertEquals("0", result.positions());
    }

    @Test
    void shouldThrowException_whenEngineIsNull() {
        assertThrows(SearchEngineException.class, () ->
                service.getRankings(null, "xolv")
        );
    }

    @Test
    void shouldThrowException_whenEngineIsBlank() {
        assertThrows(SearchEngineException.class, () ->
                service.getRankings(" ", "xolv")
        );
    }

    @Test
    void shouldThrowException_whenKeywordIsNull() {
        assertThrows(SearchEngineException.class, () ->
                service.getRankings("google", null)
        );
    }

    @Test
    void shouldThrowException_whenKeywordIsBlank() {
        assertThrows(SearchEngineException.class, () ->
                service.getRankings("google", " ")
        );
    }

    @Test
    void shouldReturnMultiplePositions() {

        when(cache.get("google:xolv")).thenReturn(null);

        when(googleEngine.search("xolv")).thenReturn(List.of(
                "https://xolv.com",
                "https://xolv.io",
                "https://test.com",
                "https://xolv.org"
        ));

        RankingResult result = service.getRankings("google", "xolv");

        assertEquals("0, 1, 3", result.positions());
    }

    @Test
    void shouldReturnZero_whenEmptyUrlList() {

        when(cache.get("google:xolv")).thenReturn(null);

        when(googleEngine.search("xolv")).thenReturn(List.of());

        RankingResult result = service.getRankings("google", "xolv");

        assertEquals("0", result.positions());
    }
}