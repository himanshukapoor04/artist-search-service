package com.mashup.music.search.config;

import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for the in-memory cache.
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${mashup.artist-search-service.cache.retention-days}")
    private long cacheRetention;

    /**
     * Spring bean for creating the cache manager.
     * @return
     */
    @Bean
    @Override
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(){
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(cacheRetention, TimeUnit.DAYS)
                                .build()
                                .asMap(),
                        false
                );
            }
        };
    }

    /**
     * Key Generator for the cache manager.
     * @return {@link SimpleKeyGenerator}
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }


}
