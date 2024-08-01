package com.community.api.endpoint.serviceProvider;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Base64;

public class Base64ToByteArrayDeserializer extends JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String base64String = p.getText();
        if (base64String == null || base64String.isEmpty()) {
            return null;
        } else {
            return Base64.getDecoder().decode(base64String);
        }
    }
}
