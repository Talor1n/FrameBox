package net.talor1n.framebox;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import jakarta.persistence.Entity;
import lombok.extern.log4j.Log4j2;
import net.talor1n.framebox.db.HibernateSupport;
import net.talor1n.framebox.repository.UserRepository;
import net.talor1n.framebox.service.AuthService;
import net.talor1n.framebox.ui.AuthWindow;
import net.talor1n.framebox.ui.MainWindow;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

@Log4j2
public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        try { UIManager.setLookAndFeel(new FlatMacDarkLaf()); } catch (UnsupportedLookAndFeelException ignored) {}

        JWindow splash = showSplash();

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

        var hibernate = HibernateSupport.fromCfg(entities);
        var repo = new UserRepository(hibernate);
        var authService = new AuthService(repo);

        splash.dispose();

        new AuthWindow(authService, user -> new MainWindow(repo, user.getId()));
    }

    private static JWindow showSplash() {
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());

        JLabel label = new JLabel("FrameBox загружается...", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        label.setForeground(Color.WHITE);

        content.add(label, BorderLayout.CENTER);
        content.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        splash.setContentPane(content);
        splash.setSize(300, 150);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        return splash;
    }
}
