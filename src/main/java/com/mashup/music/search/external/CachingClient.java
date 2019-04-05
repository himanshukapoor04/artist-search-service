package com.mashup.music.search.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Parent class for all the clients to call the third party API
 * Adds caching related methods to all the clients and a common webclient builder.
 */
@Slf4j
@RequiredArgsConstructor
public class CachingClient {

    private final CacheManager cacheManager;
    protected final WebClient.Builder builder;

    /**
     * Put an object in the cache store.
     *
     * @param cacheName in which object will be added
     * @param key of the object
     * @param value actual object which will be added in cache
     */
    protected void putInCache(final String cacheName, final Object key, final Object value) {
        log.debug("Putting object with key " + key + " and value " + value + " in cache " + cacheName );
        cacheManager.getCache(cacheName).put(key, value);
    }

    /**
     * Get object from the cache by key.
     *
     * @param cacheName to fetch an object from
     * @param key of the object to be fetched
     * @return {@link Object} which is stored in cache
     */
    protected Object getObjectFromCache(final String cacheName, final Object key) {
        val object = cacheManager.getCache(cacheName).get(key);
        return object != null ? object.get() : null;
    }

}
