package com.mashup.music.search.external.musicbrainz;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Configuration class for Music Braiz Specific properties.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties("mashup.artist-search-service.musicbranz-service")
public class MusicBrainzProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String path;

    @NotBlank
    private String cacheName;

    @NotNull
    private long timeoutSeconds;
}
