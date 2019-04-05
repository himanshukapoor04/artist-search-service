package com.mashup.music.search.external.discogs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Config files for the Discog related properties
 */
@Data
@Slf4j
@Validated
@Configuration
@ConfigurationProperties("mashup.artist-search-service.discogs-service")
public class DiscogsProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String path;

    @NotBlank
    private String cacheName;

    @NotNull
    private long timeoutSeconds;
}
