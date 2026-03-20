package com.xolv.cache;

public interface CacheService {
    String get(String key);
    void put(String key, String value);
    void clear();
    void evict(String key);
}