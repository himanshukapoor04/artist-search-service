package com.mashup.music.search.service;

import com.mashup.music.search.external.coverartarchive.CoverArtArchiveClient;
import com.mashup.music.search.api.v1.model.Album;
import com.mashup.music.search.api.v1.model.Artist;
import com.mashup.music.search.external.coverartarchive.model.AlbumCover;
import com.mashup.music.search.external.discogs.DiscogsClient;
import com.mashup.music.search.external.discogs.model.ArtistProfile;
import com.mashup.music.search.external.musicbrainz.MusicBrainzClient;
import com.mashup.music.search.external.musicbrainz.model.ArtistDetails;
import com.mashup.music.search.external.musicbrainz.model.Relation;
import com.mashup.music.search.external.musicbrainz.model.ReleaseGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {

    private final MusicBrainzClient musicBrainzClient;
    private final CoverArtArchiveClient coverArtArchiveClient;
    private final DiscogsClient discogsClient;

    @Override
    public Mono<Artist> getArtistDetails(String mbid) {
        return Mono.just(new Artist())
                .flatMap(artist -> fetchAndReturnArtist(mbid, artist));
    }

    private Mono<Artist> fetchAndReturnArtist(String mbid,
                                              Artist artist) {
        artist.setArtistId(mbid);
        return musicBrainzClient.getArtist(mbid)
                .flatMap(artistDetails -> fetchAndReturnArtistDetails(artistDetails, artist));
    }

    private Mono<Artist> fetchAndReturnArtistDetails(ArtistDetails artistDetails,
                                                     Artist artist) {
        val monoAlbum = fetchAlbums(artistDetails, artist);
        val monoArtist = fetchArtistProfile(artistDetails, artist);
        return Mono.just(artist).delayUntil(artist1 -> monoAlbum)
                .delayUntil(artist1 -> monoArtist)
                .doOnSuccess(Mono::just);
    }

    private Mono<AlbumCover> fetchAlbums(ArtistDetails artistDetails, Artist artist) {
        final Mono<AlbumCover>[] fAlbumCoverMono = new Mono[]{Mono.empty()};
        if (artistDetails.getReleaseGroups() != null && !artistDetails.getReleaseGroups().isEmpty()) {
            val albums = new ArrayList<Album>();
            artistDetails.getReleaseGroups().parallelStream()
                    .filter(isReleaseGroupAnAlbum())
                    .forEach(album -> {
                        val albumCoverMono = coverArtArchiveClient.getAlbumCover(album.getId());
                        albumCoverMono.subscribe(albumCover -> {
                            if (albumCover.getImages() != null && !albumCover.getImages().isEmpty()) {
                                val images = new ArrayList<String>();
                                albumCover.getImages().parallelStream()
                                        .forEach(image -> images.add(image.getImage()));
                                albums.add(Album.builder().albumId(album.getId())
                                        .title(album.getTitle()).images(images).build());
                            }

                        });
                        fAlbumCoverMono[0] = albumCoverMono;
                    });
            artist.setAlbums(albums);
        }
        return fAlbumCoverMono[0];
    }

    private Predicate<ReleaseGroup> isReleaseGroupAnAlbum() {
        return releaseGroup -> releaseGroup != null &&
                "album".equalsIgnoreCase(releaseGroup.getPrimaryType());
    }

    private Mono<ArtistProfile> fetchArtistProfile(ArtistDetails artistDetails, Artist artist) {
        if (!artistDetails.getRelations().isEmpty()) {
            val discogRelationOptional = artistDetails.getRelations().parallelStream()
                    .filter(isRelationADiscog())
                    .findFirst();
            if (discogRelationOptional.isPresent()) {
                val resources = discogRelationOptional.get().getUrl().getResource().split("/");
                val discogId = resources[resources.length - 1];
                val artistProfileMono = discogsClient.getArtistProfile(discogId);
                artistProfileMono.subscribe(artistProfile -> artist.setDescription(artistProfile.getProfile()));
                return artistProfileMono;
            }
        }
        return Mono.just(new ArtistProfile());
    }

    private Predicate<Relation> isRelationADiscog() {
        return relation -> relation != null && relation.getType() != null &&
                "discogs".equalsIgnoreCase(relation.getType());
    }
}
