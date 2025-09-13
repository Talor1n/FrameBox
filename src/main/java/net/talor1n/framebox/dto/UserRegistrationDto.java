package net.talor1n.framebox.dto;

import jakarta.validation.constraints.*;

public record UserRegistrationDto(

        @NotBlank(message = "Имя не должно быть пустым")
        String firstName,

        @NotBlank(message = "Фамилия не должна быть пустой")
        String lastName,

        @Min(value = 1, message = "Возраст должен быть больше 0")
        @Max(value = 120, message = "Возраст должен быть не больше 120")
        int age,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-]).{8,}$",
                message = "Пароль должен содержать заглавные, строчные буквы, цифры и спецсимволы"
        )
        String password
) {}
