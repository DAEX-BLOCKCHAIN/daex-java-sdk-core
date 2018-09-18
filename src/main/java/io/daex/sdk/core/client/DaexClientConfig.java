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

    private static final String RSA_ENABLED = "rsa.enabled";
    private static final String TAPI_RSA_ID = "tApi.rsa.id";
    private static final String TAPI_RSA_PRIVATEKEY = "tApi.rsa.privateKey";
    private static final String MAPI_RSA_ID = "mApi.rsa.id";
    private static final String MAPI_RSA_PRIVATEKEY = "mApi.rsa.privateKey";
    private static final String TCAPI_RSA_ID = "tcApi.rsa.id";
    private static final String TCAPI_RSA_PRIVATEKEY = "tcApi.rsa.privateKey";

    // Initialize DEFAULT_PROPERTIES
    static {
        DEFAULT_PROPERTIES = new Properties();
        DEFAULT_PROPERTIES.put(RSA_ENABLED, "false");
        DEFAULT_PROPERTIES.put(TAPI_RSA_ID, "");
        DEFAULT_PROPERTIES.put(TAPI_RSA_PRIVATEKEY, "");
        DEFAULT_PROPERTIES.put(MAPI_RSA_ID, "");
        DEFAULT_PROPERTIES.put(MAPI_RSA_PRIVATEKEY, "");
        DEFAULT_PROPERTIES.put(TCAPI_RSA_ID, "");
        DEFAULT_PROPERTIES.put(TCAPI_RSA_PRIVATEKEY, "");
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

    public boolean isRSAEnabled() {
        return Boolean.parseBoolean(getValue(RSA_ENABLED));
    }

    public String getTApiRSAId() {
        return getValue(TAPI_RSA_ID);
    }

    public String getTApiRSAPrivateKey() {
        return getValue(TAPI_RSA_PRIVATEKEY);
    }

    public String getMApiRSAId() {
        return getValue(MAPI_RSA_ID);
    }

    public String getMApiRSAPrivateKey() {
        return getValue(MAPI_RSA_PRIVATEKEY);
    }

    public String getTcApiRSAId() {
        return getValue(TCAPI_RSA_ID);
    }

    public String getTcApiRSAPrivateKey() {
        return getValue(TCAPI_RSA_PRIVATEKEY);
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
