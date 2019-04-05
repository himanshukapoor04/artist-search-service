package com.mashup.music.search.service;

import com.google.common.util.concurrent.RateLimiter;
import com.mashup.music.search.exception.RestClientError;
import com.mashup.music.search.config.ThrottlingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ThrottlingServiceImpl implements ThrottlingService {

    private final RateLimiter rateLimiter;
    private final ThrottlingConfig throttlingConfig;

    public ThrottlingServiceImpl(ThrottlingConfig throttlingConfig) {
        this.throttlingConfig = throttlingConfig;
        rateLimiter = RateLimiter.create(this.throttlingConfig.getRequestPerSecond());

    }

    @Override
    public Mono<String> checkRateLimit() {
        if (!rateLimiter.tryAcquire(throttlingConfig.getTimeout(), TimeUnit.MILLISECONDS)) {
            log.warn("Rate limit has reached!");
            return Mono.error(new RestClientError(HttpStatus.TOO_MANY_REQUESTS, "Reached rate limit request per " +
                    "second!"));
        }
        return Mono.just("Permitted");
    }
}
