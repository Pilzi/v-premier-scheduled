package io.github.pilzi.database.utils;

import io.github.pilzi.service.services.HibernateSessionFactoryService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class TransactionalUtil {

    public static void executeInTransaction(@NonNull HibernateSessionFactoryService hibernateSessionFactoryService, @NonNull Consumer<Session> consumer) {
        SessionFactory sessionFactory = hibernateSessionFactoryService.get();

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            consumer.accept(session);

            session.flush();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
