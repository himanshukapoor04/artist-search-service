package com.mashup.music.search.service;

import com.mashup.music.search.external.coverartarchive.CoverArtArchiveClient;
import com.mashup.music.search.api.v1.model.Artist;
import com.mashup.music.search.external.coverartarchive.model.AlbumCover;
import com.mashup.music.search.external.coverartarchive.model.Image;
import com.mashup.music.search.external.discogs.DiscogsClient;
import com.mashup.music.search.external.discogs.model.ArtistProfile;
import com.mashup.music.search.external.musicbrainz.MusicBrainzClient;
import com.mashup.music.search.external.musicbrainz.model.ArtistDetails;
import com.mashup.music.search.external.musicbrainz.model.Relation;
import com.mashup.music.search.external.musicbrainz.model.ReleaseGroup;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ArtistServiceTest {

    private ArtistService artistService;

    @Mock
    private MusicBrainzClient musicBrainzClient;

    @Mock
    private CoverArtArchiveClient coverArtArchiveClient;

    @Mock
    private DiscogsClient discogsClient;

    @Before
    public void init() {
        this.artistService = new ArtistServiceImpl(musicBrainzClient, coverArtArchiveClient, discogsClient);
    }

    @Test
    public void shouldGetArtistDetails() {
        mockMusicBrainzResponse();
        mockCoverArtArchiveResponse();
        mockDiscogClientResponse();

        val artist = artistService.getArtistDetails("artistMbid").block();

        assertArtistDetails(artist);
        assertAlbums(artist);
        assertArtistDescription(artist);
    }

    @Test
    public void shouldGetArtistDetailsWhenNoDiscogIsPresent() {
        mockMusicBrainzResponseWithNoRelations();
        mockCoverArtArchiveResponse();

        val artist = artistService.getArtistDetails("artistMbid").block();

        assertArtistDetails(artist);
        assertAlbums(artist);
    }

    @Test
    public void shouldGetArtistDetailsWehnNoAlbumIsPresent() {
        mockMusicBrainzResponseWithNoReleaseGroups();
        mockDiscogClientResponse();

        val artist = artistService.getArtistDetails("artistMbid").block();

        assertArtistDetails(artist);
        assertArtistDescription(artist);
    }

    private void assertArtistDescription(Artist artist) {
        assertNotNull(artist.getDescription());
        assertEquals("artist profile", artist.getDescription());
    }

    private void assertAlbums(Artist artist) {
        assertNotNull(artist.getAlbums());
        assertEquals(1, artist.getAlbums().size());

        val album = artist.getAlbums().get(0);
        assertEquals("releaseId", album.getAlbumId());
        assertEquals("Artist Album", album.getTitle());
        assertNotNull(album.getImages());
        assertEquals(1, album.getImages().size());

        val image = album.getImages().get(0);
        assertEquals("image", image);
    }

    private void assertArtistDetails(Artist artist) {
        assertNotNull(artist);
        assertEquals("artistMbid", artist.getArtistId());
    }



    private void mockMusicBrainzResponse() {
        val relation = getRelation();
        val releaseGroup = getReleaseGroup();
        val artistDetails = getArtistDetails(relation, releaseGroup);

        when(musicBrainzClient.getArtist(anyString()))
                .thenReturn(Mono.just(artistDetails));
    }

    private void mockMusicBrainzResponseWithNoRelations() {
        val releaseGroups = getReleaseGroup();
        val artistDetails = getArtistDetails(null, releaseGroups);

        when(musicBrainzClient.getArtist(anyString()))
                .thenReturn(Mono.just(artistDetails));

    }

    private void mockMusicBrainzResponseWithNoReleaseGroups() {
        val relation = getRelation();
        val artistDetails = getArtistDetails(relation, null);

        when(musicBrainzClient.getArtist(anyString()))
                .thenReturn(Mono.just(artistDetails));

    }

    private ArtistDetails getArtistDetails(Relation relation,
                                           ReleaseGroup releaseGroup) {
        return ArtistDetails.builder()
                .id("artistId").name("Artist").relations(asList(relation))
                .releaseGroups(asList(releaseGroup)).build();
    }

    private ReleaseGroup getReleaseGroup() {
        return ReleaseGroup.builder().id("releaseId").primaryType("album").title("Artist Album").build();
    }

    private Relation getRelation() {
        val relationUrl = Relation.Url.builder().id("descriptionId").resource("http://test.org").build();
        return Relation.builder().type("discogs").url(relationUrl).build();
    }

    private void mockCoverArtArchiveResponse() {
        val image = Image.builder().image("image").build();
        val albumCover = AlbumCover.builder().images(asList(image)).build();

        when(coverArtArchiveClient.getAlbumCover(anyString()))
                .thenReturn(Mono.just(albumCover));
    }

    private void mockDiscogClientResponse() {
        val artistProfile = ArtistProfile.builder().profile("artist profile").build();

        when(discogsClient.getArtistProfile(anyString())).thenReturn(Mono.just(artistProfile));
    }



}
