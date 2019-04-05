package com.mashup.music.search.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception class to throw response code errors
 */
@Getter
public class RestClientError extends ResponseStatusException {

    public RestClientError(HttpStatus status, String reason) {
        super(status, reason);
    }
}
