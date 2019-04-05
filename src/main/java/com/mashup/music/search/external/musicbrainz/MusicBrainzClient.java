package com.mashup.music.search.external.musicbrainz;

import com.mashup.music.search.external.musicbrainz.model.ArtistDetails;
import reactor.core.publisher.Mono;

/**
 * Client to call the MusicBrainz API.
 */
public interface MusicBrainzClient {

    /**
     * Fetch artist details with it's MBID.
     * First the artist will be searched in the cache, if it is not found then it will
     * call the MusicBrainz endpoint in order to fetch artist.
     *
     * @param mbid of the artist.
     * @return {@link Mono< ArtistDetails >} response carrying artist details
     */
    Mono<ArtistDetails> getArtist(String mbid);

}
