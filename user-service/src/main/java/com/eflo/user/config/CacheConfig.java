package com.eflo.user.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration for caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USERS_CACHE = "users";
    public static final String BUSINESS_UNITS_CACHE = "businessUnits";
    public static final String HIERARCHIES_CACHE = "hierarchies";
    public static final String ROLES_CACHE = "roles";

    /**
     * Configure cache manager with defined caches.
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(USERS_CACHE),
                new ConcurrentMapCache(BUSINESS_UNITS_CACHE),
                new ConcurrentMapCache(HIERARCHIES_CACHE),
                new ConcurrentMapCache(ROLES_CACHE)
        ));
        return cacheManager;
    }
}
