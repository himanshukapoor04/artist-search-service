package com.mashup.music.search.external.coverartarchive;

import com.mashup.music.search.external.CachingClient;
import com.mashup.music.search.external.coverartarchive.model.AlbumCover;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;


@Service
@Slf4j
public class CoverArtArchiveClientImpl extends CachingClient implements CoverArtArchiveClient {

    public static final String LOCATION = "Location";
    private final CoverArtArchiveProperties properties;

    public CoverArtArchiveClientImpl(CacheManager cacheManager,
                                     WebClient.Builder builder,
                                     CoverArtArchiveProperties properties) {
        super(cacheManager, builder);
        this.properties = properties;
    }

    @Override
    public Mono<AlbumCover> getAlbumCover(String mbid) {
        val albumCover = (AlbumCover) getObjectFromCache(properties.getCacheName(), mbid);
        if(albumCover == null) {
            return fetchFromApi(mbid);
        } else {
            return Mono.just(albumCover);
        }
    }

    private Mono<AlbumCover> fetchFromApi(String mbid) {
        return builder
                .baseUrl(properties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                .build()
                .get()
                .uri(String.format(properties.getPath(), mbid))
                .accept(TEXT_PLAIN)
                .exchange()
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()), Mono.empty())
                .switchIfEmpty(Mono.empty())
                .flatMap(clientResponse -> {
                    /*
                     * Cover Art API redirects when it is called so location need to be fetched from the
                     * headers and then it will be called.
                     */
                    String redirectLocation0 = clientResponse.headers().asHttpHeaders().getFirst(LOCATION);
                    if (redirectLocation0 != null) {
                        return builder.baseUrl(properties.getUrl())
                                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                                .build()
                                .get()
                                .uri(redirectLocation0)
                                .accept(TEXT_PLAIN)
                                .exchange()
                                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()), Mono.empty())
                                .flatMap(clientResponse1 -> {
                                    String redirectLocation1 = clientResponse1.headers().asHttpHeaders().getFirst(LOCATION);
                                    return builder.baseUrl(properties.getUrl())
                                            .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                                            .build()
                                            .get()
                                            .uri(redirectLocation1)
                                            .accept(APPLICATION_JSON)
                                            .retrieve()
                                            .bodyToMono(AlbumCover.class)
                                            .doOnSuccess(albumCover -> putInCache(properties.getCacheName(), mbid, albumCover));
                                });
                    } else {
                        return Mono.just(new AlbumCover());
                    }
                });
    }
}
