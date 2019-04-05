package com.mashup.music.search.external.musicbrainz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * POJO for the Relation entity of the MusicBrainz API response.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relation implements Serializable {

    private String type;
    private Url url;

    /**
     * POJO for the Url Json payload of the Relation entity.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Url implements Serializable {
        private String id;
        private String resource;
    }
}
