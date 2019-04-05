package com.mashup.music.search.integration;

import com.mashup.music.search.Application;
import com.mashup.music.search.api.v1.model.Artist;
import com.mashup.music.search.config.SpringSecurityProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;

import java.time.Duration;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class})
public class ArtistSearchIntegrationTest {

    private static final String ARTIST_SEARCH_URI = "/api/v1/artist-search/artist/%s";
    private static final String USER = "user";

    @Autowired
    private WebTestClient client;

    @Autowired
    private SpringSecurityProperties springSecurityProperties;

    @Autowired
    private CacheManager cacheManager;

    @Before
    public void setUp() {
        client = client.mutate()
                .responseTimeout(Duration.ofMillis(30000))
                .build();
    }

    @Test
    public void shouldGetAlbumsAndDescriptionForArtist() {
        val artistId = "f27ec8db-af05-4f36-916e-3d57f91ecf5e";
        val artist = client.get()
                .uri(String.format(ARTIST_SEARCH_URI, artistId))
                .accept(APPLICATION_JSON)
                .header("Authorization", getBasicAuthHeader())
                .exchange()
                .expectBody(Artist.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(artist);
        assertEquals(artistId, artist.getArtistId());

        // Check for albums
        assertNotNull(artist.getAlbums());
        assertFalse(artist.getAlbums().isEmpty());
        val albums = artist.getAlbums().parallelStream()
                .filter(album -> "Forever, Michael".equalsIgnoreCase(album.getTitle()))
                .collect(Collectors.toList());
        assertNotNull(albums);
        assertEquals(1, albums.size());
        assertNotNull(albums.get(0).getImages());
        assertEquals(4, albums.get(0).getImages().size());
    }

    @Test
    public void shouldGetNotFoundForNNonExistentMbid() {
        val artistId = "f27ec8db-af05-4f36-916e-3d57f91ecf5f";
        client.get()
                .uri(String.format(ARTIST_SEARCH_URI, artistId))
                .header("Authorization", getBasicAuthHeader())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    public void shouldGetBadRequestForInvalidMbid() {
        val artistId = "f27ec8db-af05-4f36-916e-3d57f91ecf";
        client.get()
                .uri(String.format(ARTIST_SEARCH_URI, artistId))
                .header("Authorization", getBasicAuthHeader())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @After
    public void destroy() {
        cacheManager.getCacheNames().parallelStream().forEach(cache -> cacheManager.getCache(cache).clear());
    }

    private String getBasicAuthHeader() {
        return "Basic " + Base64Utils.encodeToString((USER + ":" + springSecurityProperties.getPassword())
                .getBytes(UTF_8));
    }

}
