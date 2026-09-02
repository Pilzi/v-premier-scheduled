package io.github.pilzi;

import io.github.pilzi.henrikdev.beans.Response;
import io.github.pilzi.henrikdev.beans.Season;
import io.github.pilzi.henrikdev.service.RestService;
import io.github.pilzi.henrikdev.service.impl.RestServiceImpl;

/**
 * Hello world!
 *
 */
public class App 
{
    static void main()
    {
        String token = "<token>";
        String region = "eu";

        RestService service = new RestServiceImpl(token, region);
        Response<Season> seasons = service.getSeasons();
        System.out.println(seasons);
    }
}
