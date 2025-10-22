package finalprojectprogramming.project.security.hash;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PepperedPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;
    private final String pepper;

    public PepperedPasswordEncoder(PasswordEncoder delegate, String pepper) {
        this.delegate = delegate;
        this.pepper = pepper == null ? "" : pepper;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(applyPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(applyPepper(rawPassword), encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }

    private String applyPepper(CharSequence rawPassword) {
        return (rawPassword == null ? "" : rawPassword.toString()) + pepper;
    }
}