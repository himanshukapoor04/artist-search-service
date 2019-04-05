package com.mashup.music.search.service;

import com.mashup.music.search.api.v1.model.Artist;
import reactor.core.publisher.Mono;

/**
 * Artist Service class to provide artist related operations
 */
public interface ArtistService {

    /**
     * Get artist details from the artist's MBID.
     *
     * @param artistId MBID given as input of the API
     * @return {@link Mono<Artist>}
     */
    Mono<Artist> getArtistDetails(String artistId);
}
