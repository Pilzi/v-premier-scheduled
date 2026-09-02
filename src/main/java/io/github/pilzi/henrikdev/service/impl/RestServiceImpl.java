package io.github.pilzi.henrikdev.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.pilzi.henrikdev.beans.Response;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.service.RestService;
import io.github.pilzi.henrikdev.utils.PathUtil;
import org.jspecify.annotations.NonNull;

public class RestServiceImpl extends AbstractRestService implements RestService {
    public RestServiceImpl(@NonNull String token,
                           @NonNull String region) {
        super(token, region);
    }

    @Override
    public Response<Season> getSeasons() {
        return get(PathUtil.getSeasons(region), new TypeReference<>() {});
    }
}
