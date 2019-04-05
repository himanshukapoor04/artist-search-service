package com.mashup.music.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Configuration file for carrying the properties related to throttling.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties("mashup.artist-search-service.throttling")
public class ThrottlingConfig {

    @NotNull
    private long requestPerSecond;

    @NotNull
    private long timeout;
}
