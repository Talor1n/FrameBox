package net.talor1n.framebox.repository;

import net.talor1n.framebox.db.HibernateSupport;
import net.talor1n.framebox.entity.User;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final HibernateSupport h;

    public UserRepository(HibernateSupport h) { this.h = h; }

    public User save(User u) {
        return h.tx(s -> {
            if (u.getId() == null) { s.persist(u); return u; }
            return s.merge(u);
        });
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(h.tx(s -> s.find(User.class, id)));
    }

    public Optional<User> findByEmail(String email) {
        return h.tx(s ->
                s.createQuery("from User u where u.email = :e", User.class)
                        .setParameter("e", email)
                        .uniqueResultOptional()
        );
    }

    public boolean existsByEmail(String email) {
        Long cnt = h.tx(s ->
                s.createQuery("select count(u.id) from User u where u.email = :e", Long.class)
                        .setParameter("e", email)
                        .getSingleResult()
        );
        return cnt != null && cnt > 0;
    }

    public List<User> findAll() {
        return h.tx(s -> s.createQuery("from User", User.class).getResultList());
    }

    public void deleteById(Long id) {
        h.tx(s -> { var u = s.find(User.class, id); if (u != null) s.remove(u); return null; });
    }
}
