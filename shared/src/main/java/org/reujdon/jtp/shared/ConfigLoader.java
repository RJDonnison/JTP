package org.reujdon.jtp.shared;

/**
 * Configuration loading interface for JTP components.
 *
 * <p>Defines standard methods for loading and validating configuration from multiple sources.
 * Implementations should handle both environment variables and properties files.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public interface ConfigLoader {
    /**
     * Loads configuration from environment variables.
     * <p>Implementation should handle all supported environment variables.</p>
     */
    void loadFromEnvVars();

    /**
     * Loads configuration from properties file.
     *
     * @param configFile path to properties file
     * @throws IllegalArgumentException if file is invalid or contains malformed values
     */
    void loadFromPropertiesFile(String configFile);

    /**
     * Verifies required configuration is present.
     *
     * @return true if any mandatory configuration is missing
     */
    boolean hasMissingConfig();

    /**
     * Validates loaded configuration values.
     *
     * @throws IllegalArgumentException if any configuration is invalid
     */
    void validateConfig();

    /**
     * Comprehensive configuration loader with fallback logic.
     *
     * <p>Loading order:</p>
     * <ol>
     *   <li>Environment variables (highest priority)</li>
     *   <li>Properties file (fallback)</li>
     * </ol>
     *
     * @param configFile path to properties file (null for default)
     * @param defaultConfigFile default properties file path
     * @see #loadFromEnvVars()
     * @see #loadFromPropertiesFile(String)
     */
    default void loadConfig(String configFile, String defaultConfigFile) {
        loadFromEnvVars();

        if (hasMissingConfig()) {
            if (configFile == null)
                configFile = defaultConfigFile;
            loadFromPropertiesFile(configFile);
        }

        validateConfig();
    }
}
