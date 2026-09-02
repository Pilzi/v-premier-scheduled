package io.github.pilzi.henrikdev.service;

import io.github.pilzi.henrikdev.beans.Response;
import io.github.pilzi.henrikdev.beans.Season;

public interface RestService {

    /**
     * Fetches schedule and map data for Valorant Premier seasons.
     * <p>
     * Includes conference queue windows, weekly predetermined maps,
     * and tournament map pools.
     *
     * @return A {@link Response} containing the {@link Season} schedules.
     */
    Response<Season> getSeasons();

}
