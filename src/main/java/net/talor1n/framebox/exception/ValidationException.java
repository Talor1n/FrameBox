package net.talor1n.framebox.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ValidationException extends Exception {
    private final List<? extends ConstraintViolation<?>> violations;

    public ValidationException(List<? extends ConstraintViolation<?>> violations) {
      super("Validation failed:\n" + formatViolations(violations));
      this.violations = violations;
    }

    private static String formatViolations(List<? extends ConstraintViolation<?>> violations) {
      return violations.stream()
              .map(v -> " - " + v.getPropertyPath() + ": " + v.getMessage())
              .collect(Collectors.joining("\n"));
    }
}