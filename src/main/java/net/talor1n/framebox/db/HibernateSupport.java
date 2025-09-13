package net.talor1n.framebox.db;

import net.talor1n.framebox.exception.ValidationException;
import net.talor1n.framebox.util.ValidationUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.*;
import java.util.function.Function;

public class HibernateSupport implements AutoCloseable {
    private final SessionFactory sessionFactory;

    private HibernateSupport(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public static HibernateSupport fromCfg(Collection<Class<?>> annotatedEntities) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();

        MetadataSources sources = new MetadataSources(registry);
        for (Class<?> c : annotatedEntities) sources.addAnnotatedClass(c);
        Metadata metadata = sources.buildMetadata();
        return new HibernateSupport(metadata.buildSessionFactory());
    }

    public <R> R tx(Function<Session, R> work) {
        try (Session s = sessionFactory.openSession()) {
            Transaction tx = s.beginTransaction();
            try {
                R res = work.apply(s);
                tx.commit();
                return res;
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public <T> void validate(T entity) throws ValidationException {
        ValidationUtils.validationWithError(entity);
    }

    @Override
    public void close() {
        sessionFactory.close();
    }
}
