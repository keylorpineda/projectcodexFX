package com.municipal.reservationsfx.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    private AppConfig() {
    }

    public static void load() throws IOException {
        if (!PROPERTIES.isEmpty()) {
            return;
        }
        try (InputStream inputStream = AppConfig.class.getResourceAsStream("/config/application.properties")) {
            if (inputStream == null) {
                throw new IOException("Configuration file application.properties not found");
            }
            PROPERTIES.load(inputStream);
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}
