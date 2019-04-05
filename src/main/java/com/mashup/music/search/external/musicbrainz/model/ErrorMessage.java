package com.mashup.music.search.external.musicbrainz.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.io.IOException;

/**
 * POJO for the error message which are published from the MusicBrainz API
 */
@Getter
@AllArgsConstructor
@JsonDeserialize(using = ErrorMessageDeserializer.class)
public class ErrorMessage {
    private String message;
}

/**
 * Deserializer for converting JSON to {@link ErrorMessage}
 */
class ErrorMessageDeserializer extends JsonDeserializer<ErrorMessage> {

    @Override
    public ErrorMessage deserialize(JsonParser jsonParser,
                                    DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);
        val errorMessage = root.findPath("error").textValue();
        return new ErrorMessage(errorMessage);
    }
}