package finalprojectprogramming.project.models.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailTypeTest {

    @Test
    void values_and_valueOf_cover_enum() {
        // Ensure enum class is initialized and all constants are accessible
        EmailType[] all = EmailType.values();
        assertThat(all).isNotEmpty();
        // Pick a few constants to touch valueOf path
        assertThat(EmailType.valueOf("RESERVATION_CONFIRMATION")).isEqualTo(EmailType.RESERVATION_CONFIRMATION);
        assertThat(EmailType.valueOf("PASSWORD_RESET")).isEqualTo(EmailType.PASSWORD_RESET);
        assertThat(EmailType.valueOf("CHECK_IN_CONFIRMED")).isEqualTo(EmailType.CHECK_IN_CONFIRMED);
    }
}
