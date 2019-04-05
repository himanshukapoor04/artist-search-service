package com.mashup.music.search.external.coverartarchive;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Property file for carruing Cover Art Archive specific properties.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties("mashup.artist-search-service.coverartarchive-service")
public class CoverArtArchiveProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String path;

    @NotBlank
    private String cacheName;

    @NotNull
    private long timeoutSeconds;
}
