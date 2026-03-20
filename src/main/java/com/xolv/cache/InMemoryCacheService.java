package com.xolv.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCacheService implements CacheService {

    private static class CacheEntry {
        String value;
        long expiry;

        CacheEntry(String value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    // 1 hour TTL
    private static final long TTL_MILLIS = 60 * 60 * 1000;

    @Override
    public String get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry == null) return null;

        if (isExpired(entry)) {
            cache.remove(key);
            return null;
        }

        return entry.value;
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void put(String key, String value) {
        long expiryTime = System.currentTimeMillis() + TTL_MILLIS;
        cache.put(key, new CacheEntry(value, expiryTime));
    }

    private boolean isExpired(CacheEntry entry) {
        return entry.expiry < System.currentTimeMillis();
    }
}