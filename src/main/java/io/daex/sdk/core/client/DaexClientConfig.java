package io.daex.sdk.core.client;

import io.daex.sdk.core.util.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.*;


public class DaexClientConfig {

    /**
     * Singleton instance variable
     */
    private static DaexClientConfig conf;

    /**
     * Underlying property implementation
     */
    private Properties properties;

    /**
     * Default {@link Properties}
     */
    private static final Properties DEFAULT_PROPERTIES;

    private static final String OAUTH_TOKEN_ENABLED = "oauth.token.enabled";
    private static final String OAUTH_ENDPOINT = "oauth.endpoint";
    private static final String OAUTH_CLIENT_ID = "oauth.client.id";
    private static final String OAUTH_CLIENT_SECRET = "oauth.client.secret";

    private static final String HMAC_ENABLED = "hmac.enabled";
    private static final String HMAC_ID = "hmac.id";
    private static final String HMAC_SECRET = "hmac.secret";

    // Initialize DEFAULT_PROPERTIES
    static {
        DEFAULT_PROPERTIES = new Properties();
        DEFAULT_PROPERTIES.put(OAUTH_TOKEN_ENABLED, "false");
        DEFAULT_PROPERTIES.put(OAUTH_ENDPOINT, "https://oauth.daex.io/v1");
        DEFAULT_PROPERTIES.put(OAUTH_CLIENT_ID, "");
        DEFAULT_PROPERTIES.put(OAUTH_CLIENT_SECRET, "");
        DEFAULT_PROPERTIES.put(HMAC_ENABLED, "false");
        DEFAULT_PROPERTIES.put(HMAC_ID, "");
        DEFAULT_PROPERTIES.put(HMAC_SECRET, "");

    }

    /**
     * Private constructor
     */
    private DaexClientConfig() {

        /*
         * Load configuration for default 'sdk_config.properties'
         */
        ResourceLoader resourceLoader = new ResourceLoader("sdk_config.properties");
        properties = new Properties();
        try {
            InputStream inputStream = resourceLoader.getInputStream();
            properties.load(inputStream);

        } catch (IOException e) {
            // We tried reading the config, but it seems like you dont have it. Skipping...
            System.out.println("sdk_config.properties not present. Skipping...");
        } catch (AccessControlException e) {
            System.out.println("Unable to read sdk_config.properties. Skipping...");
        }
        properties = combineDefaultProperties(properties);
    }

    /**
     * Singleton accessor method
     *
     * @return ConfigManager object
     */
    public static DaexClientConfig getInstance() {
        synchronized (DaexClientConfig.class) {
            if (conf == null) {
                conf = new DaexClientConfig();
            }
        }
        return conf;
    }

    /**
     * Combines some {@link Properties} with Default {@link Properties}
     *
     * @param receivedProperties Properties used to combine with Default {@link Properties}
     * @return Combined {@link Properties}
     */
    private static Properties combineDefaultProperties(Properties receivedProperties) {
        Properties combinedProperties = new Properties(DEFAULT_PROPERTIES);
        if ((receivedProperties != null) && (receivedProperties.size() > 0)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            try {
                receivedProperties.store(bos, null);
                combinedProperties.load(new ByteArrayInputStream(bos.toByteArray()));
            } catch (IOException e) {
                // Something failed trying to load the properties. Skipping...
            }
        }
        return combinedProperties;
    }


    public boolean isOauthTokenEnabled() {
        return Boolean.parseBoolean(getValue(OAUTH_TOKEN_ENABLED));
    }

    public String getOauthClientId() {
        return getValue(OAUTH_CLIENT_ID);
    }

    public String getOauthClientSecret() {
        return getValue(OAUTH_CLIENT_SECRET);
    }

    public String getOauthEndpoint() {
        return getValue(OAUTH_ENDPOINT);
    }

    public boolean isHmacEnabled() {
        return Boolean.parseBoolean(getValue(HMAC_ENABLED));
    }

    public String getHmacId() {
        return getValue(HMAC_ID);
    }

    public String getHmacSecret() {
        return getValue(HMAC_SECRET);
    }

    /**
     * Returns a value for the corresponding key
     *
     * @param key String key
     * @return String value
     */
    private String getValue(String key) {
        return properties.getProperty(key);
    }


}
