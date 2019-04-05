package com.mashup.music.search.external.coverartarchive;

import com.mashup.music.search.external.coverartarchive.model.AlbumCover;
import reactor.core.publisher.Mono;

/**
 * Client class for calling CoverArtArchive API
 */
public interface CoverArtArchiveClient {

    /**
     * Fetch album cover from the CoverArtArchive API.
     * First it will check in the cache, if it is present then it will be sent
     * else the API will be called.
     *
     * @param mbid of the album
     * @return {@link Mono<AlbumCover>} carrying details of album cover.
     */
    Mono<AlbumCover> getAlbumCover(String mbid);
}
