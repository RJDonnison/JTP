package org.reujdon.jtp.server;

import org.reujdon.jtp.shared.ConfigLoader;
import org.reujdon.jtp.shared.PropertiesUtil;
import org.reujdon.jtp.shared.env.EnvProvider;
import org.reujdon.jtp.shared.env.SystemEnvProvider;
import org.reujdon.jtp.shared.parse.ParseError;
import org.reujdon.jtp.shared.parse.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server configuration loader for JTP.
 *
 * <h2>Environment Variables</h2>
 * The following environment variables can be used:
 * <ul>
 *     <li>SERVER_PORT – (int) Port number the server should listen on</li>
 *     <li>SERVER_KEYSTORE_PATH – (String) Path to the SSL keystore file</li>
 *     <li>SERVER_KEYSTORE_PASSWORD – (String) Password to access the keystore</li>
 *     <li>SERVER_AUTHENTICATION – (boolean, optional) Whether authentication is required</li>
 * </ul>
 *
 * <h2>Properties File Keys</h2>
 * The following keys are read from the properties file (e.g., <code>server.properties</code>):
 * <ul>
 *     <li>server.port – (int) Port number the server should listen on</li>
 *     <li>server.path – (String) Path to the SSL keystore file</li>
 *     <li>server.password – (String) Password to access the keystore</li>
 *     <li>server.authenticate– (boolean, optional) Whether authentication is required</li>
 * </ul>
 *
 * <p>If a value is defined in both the environment and the properties file, the environment value takes precedence.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see ConfigLoader
 */
class JTPServerConfig implements ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(JTPServerConfig.class);
    private final EnvProvider env;

    // Constants for environment variable keys
    private static final String ENV_PORT = "SERVER_PORT";
    private static final String ENV_KEYSTORE_PATH = "SERVER_KEYSTORE_PATH";
    private static final String ENV_KEYSTORE_PASSWORD = "SERVER_KEYSTORE_PASSWORD";
    private static final String ENV_AUTHENTICATION = "SERVER_AUTHENTICATION";
    private static final String DEFAULT_CONFIG_FILE = "server.properties";

    /**
     * Server port (-1 indicates unset)
     */
    public int port = -1;

    /**
     * Path to SSL keystore
     */
    public String keystorePath;

    /**
     * Password for SSL keystore
     */
    public String keystorePassword;

    /**
     * Authentication requirement flag (null indicates unset)
     */
    public Boolean authenticate = null;

    public JTPServerConfig() {
        this(new SystemEnvProvider()); // default
    }

    public JTPServerConfig(EnvProvider envProvider) {
        this.env = envProvider;
    }

    /**
     * Loads configuration from environment variables.
     *
     * <p>Reads the following environment variables:</p>
     * <ul>
     *   <li>SERVER_PORT - Server port</li>
     *   <li>SERVER_KEYSTORE_PATH - SSL keystore path</li>
     *   <li>SERVER_KEYSTORE_PASSWORD - SSL keystore password</li>
     *   <li>SERVER_AUTHENTICATION - Authentication requirement flag</li>
     * </ul>
     *
     * @throws IllegalArgumentException for invalid numeric values
     */
    @Override
    public void loadFromEnvVars() {
        String envPort = env.getEnv(ENV_PORT);
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid PORT in env vars", e);
            }
        }

        String envAuthenticate = env.getEnv(ENV_AUTHENTICATION);
        System.out.println(envAuthenticate);
        if (envAuthenticate != null) {
            try {
                authenticate = TypeParser.parseBoolean(envAuthenticate);
            } catch (ParseError e) {
                throw new IllegalArgumentException("Invalid AUTHENTICATION in env vars", e);
            }
        }


        String envKeystorePath = env.getEnv(ENV_KEYSTORE_PATH);
        if (envKeystorePath != null)
            keystorePath = envKeystorePath;

        String envKeystorePassword = env.getEnv(ENV_KEYSTORE_PASSWORD);
        if (envKeystorePassword != null)
            keystorePassword = envKeystorePassword;
    }

    /**
     * Loads configuration from config properties file.
     *
     * <p>Reads the following config variables:</p>
     * <ul>
     *   <li>server.port - Server port</li>
     *   <li>server.path - SSL keystore path</li>
     *   <li>server.password - SSL keystore password</li>
     *   <li>server.authenticate - Authentication requirement flag</li>
     * </ul>
     *
     * @throws IllegalArgumentException for invalid numeric values
     */
    @Override
    public void loadFromPropertiesFile(String configFile) {
        if (port == -1)
            port = PropertiesUtil.getInteger(configFile, "server.port");

        if (authenticate == null)
            authenticate = PropertiesUtil.getBoolean(configFile, "server.authenticate");

        if (keystorePath == null)
            keystorePath = PropertiesUtil.getString(configFile, "server.path");

        if (keystorePassword == null)
            keystorePassword = PropertiesUtil.getString(configFile, "server.password");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMissingConfig() {
        return port == -1 || keystorePath == null || keystorePassword == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateConfig() {
        if (port < 0 || port > 65536)
            throw new IllegalArgumentException("PORT must be between 0 and 65536 and set via " + ENV_PORT + " or properties file");

        if (authenticate == null) {
            logger.warn("Server authenticate not set using default false");
            authenticate = false;
        }

        if (keystorePath == null || keystorePath.isBlank())
            logger.warn("Keystore path not set");

        if (keystorePath != null && keystorePassword == null)
            throw new IllegalArgumentException("Truststore password must be set via " + ENV_KEYSTORE_PASSWORD + " or properties file");
    }
}
