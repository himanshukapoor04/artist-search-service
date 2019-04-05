package com.mashup.music.search.external.discogs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Error message from discogs.
 */
@Getter
@AllArgsConstructor
public class ErrorMessage {

    private String message;
}
