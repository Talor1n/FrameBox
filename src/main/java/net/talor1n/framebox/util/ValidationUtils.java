package net.talor1n.framebox.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.talor1n.framebox.exception.ValidationException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ValidationUtils {
    private static final Validator validator = getValidator();
    private ValidationUtils(){}

    public static <T> void validationWithError(T entity) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        List<String> fieldOrder = Arrays.stream(entity.getClass().getDeclaredFields())
                .map(Field::getName)
                .toList();

        if (!violations.isEmpty()) {
            throw new ValidationException(violations.stream()
                    .sorted(Comparator.comparingInt(v ->
                            fieldOrder.indexOf(v.getPropertyPath().toString())))
                    .toList());
        }

    }

    private static Validator getValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
