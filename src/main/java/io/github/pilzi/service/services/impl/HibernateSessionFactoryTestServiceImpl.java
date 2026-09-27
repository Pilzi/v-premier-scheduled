package io.github.pilzi.service.services.impl;

import io.github.pilzi.service.services.HibernateSessionFactoryService;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import static io.github.pilzi.discord.Bot.HIBERNATE_CFG_XML;

public class HibernateSessionFactoryTestServiceImpl implements HibernateSessionFactoryService {
    @NonNull
    private final SessionFactory SESSION_FACTORY;

    @NonNull
    private final String jdbcUrl;

    @NonNull
    private final String username;

    @NonNull
    private final String password;

    public HibernateSessionFactoryTestServiceImpl(@NonNull String jdbcUrl,
                                                  @NonNull String username,
                                                  @NonNull String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.SESSION_FACTORY = build();
    }

    @NonNull
    public SessionFactory get() {
        return SESSION_FACTORY;
    }

    @NonNull
    private SessionFactory build() {
        Configuration configuration = new Configuration().configure(HIBERNATE_CFG_XML);
        configuration.setProperty("hibernate.connection.url", jdbcUrl);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);

        return configuration.buildSessionFactory();
    }
}

