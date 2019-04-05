package com.mashup.music.search.external.discogs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * POJO for carrying the artist profile details from Discogs API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistProfile implements Serializable {
    private String profile;
}
