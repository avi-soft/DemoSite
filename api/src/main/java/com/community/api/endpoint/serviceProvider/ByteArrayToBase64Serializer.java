package com.community.api.endpoint.serviceProvider;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;

public class ByteArrayToBase64Serializer extends JsonSerializer<byte[]> {

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            String base64String = Base64.getEncoder().encodeToString(value);
            gen.writeString(base64String);
        }
    }
}
