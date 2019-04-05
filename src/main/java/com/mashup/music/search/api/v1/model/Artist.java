package com.mashup.music.search.api.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

/**
 * POJO for the Artist response.
 */
@Data
@Builder
@NoArgsConstructor
@ApiModel(description = "Artist information including albums and description")
public class Artist extends ResourceSupport {

    @JsonCreator
    public Artist(@JsonProperty("artistId") String artistId,
                  @JsonProperty("description") String description,
                  @JsonProperty("albums") List<Album> albums) {
        this.artistId = artistId;
        this.description = description;
        this.albums = albums;
    }

    @ApiModelProperty(value = "Artist Id, which is a mbid of the artist.")
    private String artistId;

    @ApiModelProperty(value = "Artist description fetch from Discogs API")
    private String description;

    @ApiModelProperty(value = "All of artist's albums")
    private List<Album> albums;

}
