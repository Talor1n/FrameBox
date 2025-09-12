package net.talor1n.framebox.ui;

import lombok.extern.log4j.Log4j2;
import net.talor1n.framebox.service.AuthService;

import javax.swing.*;
import java.awt.*;

@Log4j2
public class AuthWindow {
    private static final String TITLE = "Framebox Auth";
    private final AuthService service;
    public AuthWindow(AuthService service) {
        this.service = service;

        var frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Framebox", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        root.add(header, BorderLayout.NORTH);

        // 🔹 Вкладки (Login / Register)
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Вход", createLoginPanel());
        tabs.addTab("Регистрация", createRegisterPanel());

        tabs.addChangeListener(changeEvent-> {
            frame.pack();
        });

        root.add(tabs, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setVisible(true);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JTextField emailOrLoginField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginBtn = new JButton("Войти");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Логин/Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(emailOrLoginField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(passwordField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        loginBtn.setPreferredSize(new Dimension(200, 35));
        panel.add(loginBtn, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        JPasswordField passwordField = new JPasswordField();
        JButton registerBtn = new JButton("Зарегистрироваться");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Фамилия:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(lastNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Имя:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(firstNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Возраст:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(ageSpinner, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(passwordField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        registerBtn.setPreferredSize(new Dimension(200, 35));
        form.add(registerBtn, gbc);

        return form;
    }
}
