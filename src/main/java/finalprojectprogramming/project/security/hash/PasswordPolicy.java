package finalprojectprogramming.project.security.hash;

public interface PasswordPolicy {
    void validate(String rawPassword);
}