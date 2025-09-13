package net.talor1n.framebox.repository;

import net.talor1n.framebox.db.HibernateSupport;
import net.talor1n.framebox.entity.User;
import net.talor1n.framebox.entity.VideoFile;
import net.talor1n.framebox.exception.ValidationException;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final HibernateSupport h;

    public UserRepository(HibernateSupport h) {
        this.h = h;
    }

    public User save(User u) throws ValidationException {
        h.validate(u);
        return h.tx(s -> {
            if (u.getId() == null) {
                s.persist(u);
                return u;
            }
            return s.merge(u);
        });
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(h.tx(s -> s.find(User.class, id)));
    }

    public Optional<User> findByName(String firstName, String lastName) {
        return h.tx(s ->
                s.createQuery("from User u where u.firstName = :fn and u.lastName = :ln", User.class)
                        .setParameter("fn", firstName)
                        .setParameter("ln", lastName)
                        .uniqueResultOptional()
        );
    }

    public boolean existsByName(String firstName, String lastName) {
        Long cnt = h.tx(s ->
                s.createQuery("select count(u.id) from User u where u.firstName = :fn and u.lastName = :ln", Long.class)
                        .setParameter("fn", firstName)
                        .setParameter("ln", lastName)
                        .getSingleResult()
        );
        return cnt != null && cnt > 0;
    }

    public List<User> findAll() {
        return h.tx(s -> s.createQuery("from User", User.class).getResultList());
    }

    public void deleteById(Long id) {
        h.tx(s -> {
            var u = s.find(User.class, id);
            if (u != null) s.remove(u);
            return null;
        });
    }

    public void addVideoToUser(Long userId, String videoPath) {
        h.tx(s -> {
            User user = s.find(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("Пользователь не найден: " + userId);
            }

            VideoFile video = VideoFile.builder()
                    .path(videoPath)
                    .createdAt(java.time.LocalDateTime.now())
                    .owner(user)
                    .build();

            user.getVideos().add(video);
            s.persist(video);
            return null;
        });
    }
}