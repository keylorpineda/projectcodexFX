package com.municipal.configs;

import com.municipal.exceptions.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Centralized access point for application level configuration values.
 */
public final class AppConfig {

    private static final String CONFIG_PATH = "/config/application.properties";

    private static AppConfig instance;

    private final Properties properties = new Properties();

    private AppConfig() {
        try (InputStream inputStream = AppConfig.class.getResourceAsStream(CONFIG_PATH)) {
            if (inputStream == null) {
                throw new ConfigurationException("Configuration file not found at " + CONFIG_PATH);
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new ConfigurationException("Unable to load configuration from " + CONFIG_PATH, exception);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getApiBaseUrl() {
        return getRequiredProperty("api.base-url");
    }

    public String getAzureClientId() {
        return getRequiredProperty("azure.client-id");
    }

    public String getAzureAuthority() {
        return getRequiredProperty("azure.authority");
    }

    public String getAzureScope() {
        return getRequiredProperty("azure.scope");
    }

    public String getAzureRedirectUri() {
        return getRequiredProperty("azure.redirect-uri");
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (Objects.isNull(value) || value.isBlank()) {
            throw new ConfigurationException("Missing required configuration property: " + key);
        }
        return value.trim();
    }
}
