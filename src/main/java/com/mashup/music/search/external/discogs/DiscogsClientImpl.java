package com.mashup.music.search.external.discogs;

import com.mashup.music.search.external.discogs.model.ErrorMessage;
import com.mashup.music.search.external.CachingClient;
import com.mashup.music.search.external.discogs.model.ArtistProfile;
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

import static java.util.logging.Level.FINE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class DiscogsClientImpl extends CachingClient implements DiscogsClient {

    private final DiscogsProperties discogsProperties;

    public DiscogsClientImpl(CacheManager cacheManager,
                             WebClient.Builder builder,
                             DiscogsProperties discogsProperties) {
        super(cacheManager, builder);
        this.discogsProperties = discogsProperties;
    }

    @Override
    public Mono<ArtistProfile> getArtistProfile(String discogId) {
        val artistProfile = (ArtistProfile) getObjectFromCache(discogsProperties.getCacheName(), discogId);
        if(artistProfile == null) {
            return fetchFromApi(discogId);
        } else {
            return Mono.just(artistProfile);
        }
    }

    private Mono<ArtistProfile> fetchFromApi(String discogId) {
        return builder
                .baseUrl(discogsProperties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                .build()
                .get()
                .uri(String.format(discogsProperties.getPath(), discogId))
                .accept(APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofSeconds(discogsProperties.getTimeoutSeconds()), Mono.empty())
                .switchIfEmpty(Mono.empty())
                .flatMap(this::extractResponse)
                .log(log.getName(), FINE)
                .doOnSuccess(artistProfile -> putInCache(discogsProperties.getCacheName(), discogId, artistProfile));
    }

    private Mono<ArtistProfile> extractResponse(final ClientResponse clientResponse) {
        val statusCode = clientResponse.statusCode();
        if(HttpStatus.OK.equals(statusCode)) {
            return clientResponse.bodyToMono(ArtistProfile.class);
        } else {
            log.warn("Got bad response from Discogs API " + statusCode);
            clientResponse.bodyToMono(ErrorMessage.class)
                    .log(log.getName(), Level.WARNING);
            if(HttpStatus.SERVICE_UNAVAILABLE.equals(statusCode)) {
                String rateLimit = clientResponse.headers().asHttpHeaders().get("X-RateLimit-Limit").get(0);
                log.warn("Discogs API: rate limit " + rateLimit + " is reached");

            }
            return Mono.just(new ArtistProfile());
        }
    }
}
