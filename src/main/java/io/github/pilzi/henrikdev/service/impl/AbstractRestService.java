package io.github.pilzi.henrikdev.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newHttpClient;

public abstract class AbstractRestService {
    @NonNull
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    @NonNull
    protected final String token;

    @NonNull
    protected final String region;

    protected AbstractRestService(@NonNull String token,
                                  @NonNull String region) {
        this.token = token;
        this.region = region;
    }

    public <T> T get(@NonNull URI uri, @NonNull TypeReference<T> typeRef) {

        try (HttpClient client = newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .setHeader("Content-Type", "application/json")
                    .header("Authorization", token)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), typeRef);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
