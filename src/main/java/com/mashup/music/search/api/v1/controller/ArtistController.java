package com.mashup.music.search.api.v1.controller;

import com.mashup.music.search.service.ArtistService;
import com.mashup.music.search.api.v1.model.Artist;
import com.mashup.music.search.service.ThrottlingService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Controller to provide search endpoints for the artist.
 */
@RestController
@RequestMapping("/api/v1/artist-search/artist")
@RequiredArgsConstructor
@Slf4j
@Api(value = "/ArtistSearch", description = "API to search for artist details")
class ArtistController {

    private final ArtistService artistService;
    private final ThrottlingService throttlingService;

    /**
     * Search artist by its mbid.
     * It will look for MusicBrainz for the artist details.
     * From the response album cover art will be searched on coverartarchive.org and
     * from discogs' relation artist description will be found from api.discogs.com
     *
     * @param mbid unique identifier of the artist on musicbrainz.org
     * @return {@link Mono<Artist>} Reactor Mono containing details of Artist
     */
    @GetMapping
    @RequestMapping(value = "/{mbid}", method = RequestMethod.GET)
    @ApiOperation(value = "Get Artist information by the unique MusicBrainz identifier", response = Artist.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Artist.class,
                    message = "Get artist information like albums and description"),
            @ApiResponse(code = 400, message = "Invalid Mbid"),
            @ApiResponse(code = 401, message = "User is unauthorized"),
            @ApiResponse(code = 404, message = "Artist is not found"),
            @ApiResponse(code = 429, message = "Reached rate limit per second")
    })
    Mono<Artist> getArtist(final @ApiParam("Artist Mbid(a UUID format string)")
                                  @PathVariable String mbid) {
        return throttlingService.checkRateLimit()
                .flatMap(str -> artistService.getArtistDetails(mbid))
                .flatMap(artist -> addHateoasSpecs(mbid, artist));
    }

    private Mono<Artist> addHateoasSpecs(String mbid,
                                         Artist artist) {
        artist.add(linkTo(methodOn(ArtistController.class)
                .getArtist(mbid)).withSelfRel());
        return Mono.just(artist);
    }
}
