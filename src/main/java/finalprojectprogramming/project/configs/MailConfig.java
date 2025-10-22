package finalprojectprogramming.project.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mail.from")
public class MailConfig {

    /**
     * Email address that will appear as the sender in all outbound notifications.
     */
    private String address;

    /**
     * Friendly name displayed next to the sender email.
     */
    private String name;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
