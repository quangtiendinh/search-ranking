package com.xolv.controller;

import com.xolv.cache.CacheService;
import com.xolv.constant.ApiPath;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping(ApiPath.CACHE)
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @DeleteMapping
    public String clearCache(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String engine
    ) {

        if (Objects.isNull(keyword) && Objects.isNull(engine)) {
            cacheService.clear();
            return "All cache cleared";
        }

        if (Objects.isNull(keyword) || Objects.isNull(engine)) {
            throw new IllegalArgumentException("Both keyword and engine must be provided");
        }

        String key = engine.toLowerCase() + ":" + keyword.toLowerCase();
        cacheService.evict(key);

        return "Cache evicted for key: " + key;
    }
}