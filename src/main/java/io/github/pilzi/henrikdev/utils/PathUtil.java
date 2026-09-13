package io.github.pilzi.henrikdev.utils;

import org.jspecify.annotations.NonNull;

import java.net.URI;

public class PathUtil {

    private static final String API_VERSION_V1 = "/v1";

    private static final String VALORANT_PATH = "/valorant";
    private static final String PREMIER_PATH = "/premier";
    private static final String SEASONS_PATH = "/seasons";
    private static final String PATH_SEPARATOR = "/";
    private static final String API_URL = "https://api.henrikdev.xyz";

    @NonNull
    public static URI getSeasons(@NonNull String region) {
        return URI.create(String.join("", API_URL, VALORANT_PATH, API_VERSION_V1, PREMIER_PATH, SEASONS_PATH, PATH_SEPARATOR, region));
    }
}
