package com.mashup.music.search.service;

import reactor.core.publisher.Mono;

/**
 * Service to provide rate limit capability to the endpoint.
 */
public interface ThrottlingService {

    /**
     * Check if the rate limit has reached.
     *
     * @return {@link Mono<String>} Signify status if permit can be taken or not
     */
    Mono<String> checkRateLimit();
}
