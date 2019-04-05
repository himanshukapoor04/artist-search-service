package com.mashup.music.search.api.v1.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POJO for the album response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "Album details of the artist")
public class Album {

    @ApiModelProperty(value = "Artist's album id on CoverArtArchive ")
    private String albumId;

    @ApiModelProperty(value = "Title of the album")
    private String title;

    @ApiModelProperty(value = "List of all the image url's of the album")
    private List<String> images;
}
