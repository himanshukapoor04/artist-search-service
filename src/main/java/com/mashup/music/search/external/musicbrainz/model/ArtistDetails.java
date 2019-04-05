package com.mashup.music.search.external.musicbrainz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * POJO to fetch artist details from the MusicBrainz API.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistDetails implements Serializable {

    private String id;
    private String name;
    private List<Relation> relations;

    @JsonProperty("release-groups")
    private List<ReleaseGroup> releaseGroups;

}
