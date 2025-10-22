package finalprojectprogramming.project.security.hash;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    public PasswordHashService(
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy
    ) {
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    public String encode(String rawPassword) {
        passwordPolicy.validate(rawPassword);
         String material = rawPassword == null ? "" : rawPassword;
        return passwordEncoder.encode(material);
    }

    public boolean matches(String rawPassword, String storedEncodedPassword) {
        if (storedEncodedPassword == null || storedEncodedPassword.isBlank()) {
            return false;
        }
         String material = rawPassword == null ? "" : rawPassword;
        return passwordEncoder.matches(material, storedEncodedPassword);
    }
}