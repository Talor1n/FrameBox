package net.talor1n.framebox.service;

import net.talor1n.framebox.dto.UserRegistrationDto;
import net.talor1n.framebox.entity.User;
import net.talor1n.framebox.exception.UserAlreadyExistsException;
import net.talor1n.framebox.exception.ValidationException;
import net.talor1n.framebox.repository.UserRepository;
import net.talor1n.framebox.util.ValidationUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User register(UserRegistrationDto dto) throws ValidationException, UserAlreadyExistsException {
        ValidationUtils.validationWithError(dto);

        if (userRepository.existsByName(dto.firstName(), dto.lastName())) {
            throw new UserAlreadyExistsException();
        }

        String hash = BCrypt.hashpw(dto.password(), BCrypt.gensalt());

        User user = User.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .age(dto.age())
                .passwordHash(hash)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> login(String firstName, String lastName, String password) {
        return userRepository.findByName(firstName.trim(), lastName.trim())
                .filter(user -> BCrypt.checkpw(password.trim(), user.getPasswordHash()));
    }
}
