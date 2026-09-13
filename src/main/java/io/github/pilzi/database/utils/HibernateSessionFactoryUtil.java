package io.github.pilzi.database.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import static io.github.pilzi.discord.Bot.HIBERNATE_CFG_XML;

public class HibernateSessionFactoryUtil {
    @NonNull
    private static final SessionFactory SESSION_FACTORY = build();

    @NonNull
    public static SessionFactory get() {
        return SESSION_FACTORY;
    }

    @NonNull
    private static SessionFactory build() {
        Dotenv dotenv = Dotenv.load();

        Configuration configuration = new Configuration().configure(HIBERNATE_CFG_XML);
        configuration.setProperty("hibernate.connection.url", dotenv.get("DB_URL"));
        configuration.setProperty("hibernate.connection.username", dotenv.get("DB_USERNAME"));
        configuration.setProperty("hibernate.connection.password", dotenv.get("DB_PASSWORD"));

        return configuration.buildSessionFactory();
    }
}
