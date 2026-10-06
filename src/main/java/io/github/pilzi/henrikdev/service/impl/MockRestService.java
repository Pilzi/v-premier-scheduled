package io.github.pilzi.henrikdev.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.pilzi.henrikdev.beans.Response;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.service.RestService;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.util.Objects;

public class MockRestService implements RestService {

    @NonNull
    public static final String SEASON_MOCK_JSON = "/season-mock.json";
    @NonNull
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @NonNull
    @Override
    public Response<Season> getSeasons() {
        try (InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream(SEASON_MOCK_JSON))) {

            Response<Season> mockResponse = objectMapper.readValue(stream, new TypeReference<>() {});

            return new Response<>(200, mockResponse.data());

        } catch (IOException e) {
            throw new RuntimeException("Failed to read season-mock.json", e);
        }
    }
}
