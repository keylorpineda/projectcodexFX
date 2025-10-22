package finalprojectprogramming.project.services.auth;

public record AzureUserInfo(String id, String email, String displayName) {
    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }

    public boolean hasDisplayName() {
        return displayName != null && !displayName.isBlank();
    }
}