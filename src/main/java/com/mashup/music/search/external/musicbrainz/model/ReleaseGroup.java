package com.mashup.music.search.external.musicbrainz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * POJO for the ReleaseGroup entity of the JSON pojo.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseGroup implements Serializable {

    private String id;
    private String title;

    @JsonProperty("primary-type")
    private String primaryType;
}
