package finalprojectprogramming.project.configs;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.UserRepository;

@Configuration
public class AdminUserInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Bean
    public ApplicationRunner adminProvisioningRunner(
            AdminUserProperties properties,
            UserRepository userRepository) {
        return args -> {
            if (!properties.isEnabled()) {
                LOGGER.info("Admin auto-provisioning is disabled");
                return;
            }

            String email = properties.getEmail();

            if (!StringUtils.hasText(email)) {
                LOGGER.warn("Admin provisioning skipped because admin email is missing");
                return;
            }

            Optional<User> existingUser = userRepository.findByEmail(email);

            LocalDateTime now = LocalDateTime.now();

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                boolean updated = false;

                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    updated = true;
                }

                if (!Boolean.TRUE.equals(user.getActive())) {
                    user.setActive(true);
                    user.setDeletedAt(null);
                    updated = true;
                }

                if (StringUtils.hasText(properties.getName())
                        && !Objects.equals(user.getName(), properties.getName())) {
                    user.setName(properties.getName());
                    updated = true;
                }

                if (!Objects.equals(user.getEmail(), email)) {
                    user.setEmail(email);
                }

                if (updated) {
                    user.setUpdatedAt(now);
                    userRepository.save(user);
                    LOGGER.info("Updated existing administrator account with email={} (id={})", email, user.getId());
                } else {
                    LOGGER.info("Administrator account with email={} already up-to-date", email);
                }
                return;
            }

            User admin = User.builder()
                    .email(email)
                    .name(StringUtils.hasText(properties.getName()) ? properties.getName() : "Administrator")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            User saved = userRepository.save(admin);
            LOGGER.info("Administrator account provisioned with email={} (id={})", email, saved.getId());
        };
    }
}