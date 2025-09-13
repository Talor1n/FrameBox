package net.talor1n.framebox.ui;

import lombok.extern.log4j.Log4j2;
import net.talor1n.framebox.dto.UserRegistrationDto;
import net.talor1n.framebox.entity.User;
import net.talor1n.framebox.exception.UserAlreadyExistsException;
import net.talor1n.framebox.exception.ValidationException;
import net.talor1n.framebox.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

@Log4j2
public class AuthWindow {
    private static final String TITLE = "Framebox Auth";
    private final AuthService authService;
    private final Consumer<User> onSuccess;

    public AuthWindow(AuthService authService, Consumer<User> onSuccess) {
        this.authService = authService;
        this.onSuccess = onSuccess;

        var frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Framebox", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        root.add(header, BorderLayout.NORTH);

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

        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();

        JPasswordField passwordField = new JPasswordField();
        JPanel passwordPanel = createPasswordFieldWithToggle(passwordField);

        JButton loginBtn = new JButton("Войти");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Фамилия:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(lastNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Имя:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(firstNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(passwordPanel, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        loginBtn.setPreferredSize(new Dimension(200, 35));
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String password = new String(passwordField.getPassword());

            authService.login(firstName, lastName, password)
                    .ifPresentOrElse(
                            user -> {
                                JOptionPane.showMessageDialog(panel,
                                        "Добро пожаловать, " + user.getFirstName() + "!",
                                        "Успех",
                                        JOptionPane.INFORMATION_MESSAGE);
                                onSuccess.accept(user);
                                SwingUtilities.getWindowAncestor(panel).dispose();
                            },
                            () -> JOptionPane.showMessageDialog(panel,
                                    "Неверные имя, фамилия или пароль",
                                    "Ошибка входа",
                                    JOptionPane.ERROR_MESSAGE)
                    );
        });

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
        JPanel passwordPanel = createPasswordFieldWithToggle(passwordField);

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
        form.add(passwordPanel, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        registerBtn.setPreferredSize(new Dimension(200, 35));
        form.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            var firstname = firstNameField.getText().trim();
            var lastName = lastNameField.getText().trim();
            var age = ((Integer) ageSpinner.getValue());
            var password = passwordField.getPassword();

            var dto = new UserRegistrationDto(firstname, lastName, age, new String(password));

            try {
                authService.register(dto);
                JOptionPane.showMessageDialog(form,
                        "Регистрация прошла успешно!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException ex) {
                StringBuilder uiMsg = new StringBuilder("Исправьте ошибки:\n");
                ex.getViolations().forEach(v ->
                        uiMsg.append("• ").append(v.getMessage()).append("\n")
                );

                StringBuilder logMsg = new StringBuilder("Ошибка валидации:\n");
                ex.getViolations().forEach(v ->
                        logMsg.append("• ")
                                .append(v.getPropertyPath())
                                .append(" = ").append(v.getInvalidValue())
                                .append(" → ").append(v.getMessage())
                                .append("\n")
                );

                log.error(logMsg.toString());

                JOptionPane.showMessageDialog(form,
                        uiMsg.toString(),
                        "Ошибка валидации",
                        JOptionPane.ERROR_MESSAGE);
            } catch (UserAlreadyExistsException ex) {
                log.error("{}: {} - {}", ex.getMessage(), firstname, lastName);
                JOptionPane.showMessageDialog(form,
                        ex.getMessage(),
                        "Ошибка регистрации",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        return form;
    }

    private JPanel createPasswordFieldWithToggle(JPasswordField passwordField) {
        JButton toggleBtn = new JButton("👁");
        toggleBtn.setMargin(new Insets(0, 4, 0, 4));
        toggleBtn.setFocusable(false);

        toggleBtn.addActionListener(ev -> {
            if (passwordField.getEchoChar() == '\u0000') {
                passwordField.setEchoChar('•'); // скрыть
            } else {
                passwordField.setEchoChar((char) 0); // показать
            }
        });

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleBtn, BorderLayout.EAST);

        return passwordPanel;
    }
}
