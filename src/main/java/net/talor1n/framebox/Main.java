package net.talor1n.framebox;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import jakarta.persistence.Entity;
import lombok.extern.log4j.Log4j2;
import net.talor1n.framebox.db.HibernateSupport;
import net.talor1n.framebox.entity.User;
import net.talor1n.framebox.repository.UserRepository;
import net.talor1n.framebox.service.AuthService;
import net.talor1n.framebox.ui.AuthWindow;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import javax.swing.*;
import java.util.Set;

@Log4j2
public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        try { UIManager.setLookAndFeel(new FlatMacDarkLaf()); } catch (UnsupportedLookAndFeelException ignored) {}

        var cfg = new ConfigurationBuilder()
                .forPackage("net.talor1n.framebox.entity")
                .addScanners(Scanners.TypesAnnotated);

        var reflections = new Reflections(cfg);
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        if (entities.isEmpty()) {
            log.warn("Reflections не нашёл ни одной @Entity");
        } else {
            entities.forEach(e -> log.info("Found @Entity: {}", e.getName()));
        }

        try (var hibernate = HibernateSupport.fromCfg(entities)) {
            var repo = new UserRepository(hibernate);

            // Test

            var u = User.builder()
                    .email("user" + System.nanoTime() + "@ex.com")
                    .firstName("Hello")
                    .lastName("Hi")
                    .age(18)
                    .passwordHash("$2a$10$dummy.dummy.dummy.dummy.dummy.dummy.dum")
                    .build();

            repo.save(u);
            new AuthWindow(new AuthService());
        }
    }
}
