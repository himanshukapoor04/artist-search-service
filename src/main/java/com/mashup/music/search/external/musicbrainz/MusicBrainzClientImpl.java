package com.mashup.music.search.external.musicbrainz;

import com.mashup.music.search.exception.RestClientError;
import com.mashup.music.search.external.musicbrainz.model.ArtistDetails;
import com.mashup.music.search.external.musicbrainz.model.ErrorMessage;
import com.mashup.music.search.external.CachingClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.logging.Level;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class MusicBrainzClientImpl extends CachingClient implements MusicBrainzClient {

    private static final String RATE_LIMIT_REASON = "Rate limit is reached. Please consider calling at lower rate";
    private static final String ARTIST_NOT_FOUND = "Artist is not found";
    private static final String EXCEPTION_REASON = "Error occurred while calling the service";

    private final MusicBrainzProperties properties;

    public MusicBrainzClientImpl(CacheManager cacheManager,
                                 WebClient.Builder builder,
                                 MusicBrainzProperties properties) {
        super(cacheManager, builder);
        this.properties = properties;
    }

    @Override
    public Mono<ArtistDetails> getArtist(String mbid) {
        val artistDetails = (ArtistDetails) getObjectFromCache(properties.getCacheName(), mbid);
        if(artistDetails == null) {
            return fetchFromApi(mbid);
        } else {
            return Mono.just(artistDetails);
        }
    }

    private Mono<ArtistDetails> fetchFromApi(String mbid) {
        return builder.baseUrl(properties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                .build()
                .get()
                .uri(String.format(properties.getPath(), mbid))
                .accept(APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()), Mono.empty())
                .switchIfEmpty(Mono.error(new RestClientError(HttpStatus.REQUEST_TIMEOUT,
                        "Service is not available")))
                .flatMap(this::extractResponse)
                .log(log.getName(), Level.FINE)
                .doOnSuccess(artistDetails -> putInCache(properties.getCacheName(), mbid, artistDetails));
    }

    private Mono<ArtistDetails> extractResponse(ClientResponse clientResponse) {
        val httpStatusCode = clientResponse.statusCode();
        if(HttpStatus.OK.equals(httpStatusCode)) {
            return clientResponse.bodyToMono(ArtistDetails.class);
        } else {
            return handleError(clientResponse, httpStatusCode);
        }
    }

    private Mono<ArtistDetails> handleError(ClientResponse clientResponse,
                                            HttpStatus httpStatusCode) {
        log.warn("Error occurred while calling service MusicBrianz with error code " + httpStatusCode);
        switch (httpStatusCode) {
            case SERVICE_UNAVAILABLE:
                log.warn("Rate limit reached for MusicBrainz service");
                return Mono.error(new RestClientError(HttpStatus.SERVICE_UNAVAILABLE, RATE_LIMIT_REASON));
            case NOT_FOUND:
                return Mono.error(new RestClientError(
                        HttpStatus.NOT_FOUND, ARTIST_NOT_FOUND));
            case BAD_REQUEST:
                return clientResponse.bodyToMono(ErrorMessage.class)
                        .flatMap(errorMessage -> Mono.error(new RestClientError(httpStatusCode, errorMessage.getMessage())));
            default:
                return Mono.error(new RestClientError(HttpStatus.INTERNAL_SERVER_ERROR, EXCEPTION_REASON));
        }
    }
}
