package io.github.pilzi.service.services;

import org.hibernate.SessionFactory;
import org.jspecify.annotations.NonNull;

public interface HibernateSessionFactoryService {
    @NonNull
    SessionFactory get();
}
