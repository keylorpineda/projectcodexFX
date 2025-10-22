package finalprojectprogramming.project.security.hash;

import org.springframework.stereotype.Component;

import finalprojectprogramming.project.exceptions.InvalidPasswordException;

@Component
public class DefaultPasswordPolicy implements PasswordPolicy {

    @Override
    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidPasswordException("La contraseña no puede estar vacía.");
        }
        if (rawPassword.length() < 8) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException("Debe incluir al menos una letra mayúscula.");
        }
        if (!rawPassword.matches(".*[a-z].*")) {
            throw new InvalidPasswordException("Debe incluir al menos una letra minúscula.");
        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Debe incluir al menos un dígito.");
        }
    }
}