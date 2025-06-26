package org.reujdon.jtp.client;

import org.reujdon.jtp.shared.ConfigLoader;
import org.reujdon.jtp.shared.PropertiesUtil;
import org.reujdon.jtp.shared.env.EnvProvider;
import org.reujdon.jtp.shared.env.SystemEnvProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration loader for JTP client settings.
 *
 * <h2>Environment Variables</h2>
 * <ul>
 *   <li>CLIENT_HOST – (String) Hostname or IP address of the server</li>
 *   <li>CLIENT_PORT – (int) Port number of the server</li>
 *   <li>CLIENT_TRUSTSTORE_PATH – (String) Path to the truststore</li>
 *   <li>CLIENT_TRUSTSTORE_PASSWORD – (String) Password for the truststore</li>
 *   <li>CLIENT_API_KEY – (String) API key for authentication with the server</li>
 *   <li>CLIENT_SHUTDOWN_TIMEOUT – (int, optional) Maximum time (in ms) to wait for responses during shutdown (default: 5000)</li>
 * </ul>
 *
 * <h2>Properties File Keys</h2>
 * These keys can be specified in a file like <code>client.properties</code>:
 * <ul>
 *   <li>client.host – Hostname or IP of the server</li>
 *   <li>client.port – Server port</li>
 *   <li>client.truststore – Path to truststore file</li>
 *   <li>client.truststorePassword – Truststore password</li>
 *   <li>client.apiKey – API key for authentication</li>
 *   <li>client.shutdownTimeout – Max wait time (ms) for pending commands before shutdown</li>
 * </ul>
 *
 * @author Reuben Donnison
 * @version 0.2
 * @see ConfigLoader
 */
class JTPClientConfig implements ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(JTPClientConfig.class);
    private final EnvProvider env;

    private static final String ENV_HOST = "CLIENT_HOST";
    private static final String ENV_PORT = "CLIENT_PORT";
    private static final String ENV_TRUSTSTORE_PATH = "CLIENT_TRUSTSTORE_PATH";
    private static final String ENV_TRUSTSTORE_PASSWORD = "CLIENT_TRUSTSTORE_PASSWORD";
    private static final String ENV_API_KEY = "CLIENT_API_KEY";
    private static final String ENV_SHUTDOWN_TIMEOUT = "CLIENT_SHUTDOWN_TIMEOUT";
    private static final String DEFAULT_CONFIG_FILE = "client.properties";

    /**
     * Server hostname configuration
     */
    public String host;

    /**
     * Server port configuration (-1 indicates unset)
     */
    public int port = -1;

    /**
     * Graceful shutdown timeout in ms (-1 indicates unset)
     */
    public int shutdownTimeout = -1;

    /**
     * API key for authentication
     */
    public String apiKey;

    /**
     * Path to SSL truststore
     */
    public String truststorePath;

    /**
     * Password for SSL truststore
     */
    public String truststorePassword;

    public JTPClientConfig() {
        this(new SystemEnvProvider()); // default
    }

    public JTPClientConfig(EnvProvider envProvider) {
        this.env = envProvider;
    }

    /**
     * Loads configuration from environment variables.
     *
     * <p>Reads the following environment variables:</p>
     * <ul>
     *   <li>CLIENT_HOST - Server hostname</li>
     *   <li>CLIENT_PORT - Server port</li>
     *   <li>CLIENT_API_KEY - Authentication key</li>
     *   <li>CLIENT_TRUSTSTORE_PATH - SSL truststore path</li>
     *   <li>CLIENT_TRUSTSTORE_PASSWORD - SSL truststore password</li>
     *   <li>CLIENT_SHUTDOWN_TIMEOUT - Graceful shutdown timeout</li>
     * </ul>
     *
     * @throws IllegalArgumentException for invalid numeric values
     */
    @Override
    public void loadFromEnvVars() {
        String envHost = env.getEnv(ENV_HOST);
        System.out.println(envHost);
        System.out.println(System.getenv(ENV_HOST));
        if (envHost != null && !envHost.isBlank())
            host = envHost.trim();

        String envApiKey = env.getEnv(ENV_API_KEY);
        if (envApiKey != null && !envApiKey.isBlank())
            apiKey = envApiKey.trim();

        String envPort = env.getEnv(ENV_PORT);
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid PORT in env vars", e);
            }
        }

        String envTimeout = env.getEnv(ENV_SHUTDOWN_TIMEOUT);
        if (envTimeout != null) {
            try {
                shutdownTimeout = Integer.parseInt(envTimeout);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid SHUTDOWN_TIMEOUT in env vars", e);
            }
        }

        String envTruststorePath = env.getEnv(ENV_TRUSTSTORE_PATH);
        if (envTruststorePath != null)
            truststorePath = envTruststorePath;

        String envTruststorePassword = env.getEnv(ENV_TRUSTSTORE_PASSWORD);
        if (envTruststorePassword != null)
            truststorePassword = envTruststorePassword;
    }

    /**
     * Loads configuration from config properties file.
     *
     * <p>Reads the following config variables:</p>
     * <ul>
     *   <li>client.host - Server hostname</li>
     *   <li>client.port - Server port</li>
     *   <li>client.apiKey - Authentication key</li>
     *   <li>client.path - SSL truststore path</li>
     *   <li>client.password - SSL truststore password</li>
     *   <li>client.shutdownTimeout - Graceful shutdown timeout</li>
     * </ul>
     *
     * @throws IllegalArgumentException for invalid numeric values
     */
    @Override
    public void loadFromPropertiesFile(String configFile) {
        if (host == null)
            host = PropertiesUtil.getString(configFile, "client.host");

        if (port == -1)
            port = PropertiesUtil.getInteger(configFile, "client.port");

        if (apiKey == null)
            apiKey = PropertiesUtil.getString(configFile, "client.apiKey");

        if (shutdownTimeout == -1)
            shutdownTimeout = PropertiesUtil.getInteger(configFile, "client.shutdownTimeout");

        if (truststorePath == null)
            truststorePath = PropertiesUtil.getString(configFile, "client.path");

        if (truststorePassword == null)
            truststorePassword = PropertiesUtil.getString(configFile, "client.password");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMissingConfig() {
        return host == null || port == -1 || apiKey == null || truststorePath == null || truststorePassword == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateConfig() {
        if (host == null || host.isBlank())
            throw new IllegalArgumentException("Host must be set via " + ENV_HOST + " or properties file");

        if (port < 0 || port > 65536)
            throw new IllegalArgumentException("PORT must be between 0 and 65536 and set via " + ENV_PORT + " or properties file");

        if (apiKey == null || apiKey.isBlank())
            throw new IllegalArgumentException("API key must be set via " + ENV_API_KEY + " or properties file");

        if (shutdownTimeout < 0 ) {
            logger.warn("Client shutdown timeout not set using default 5000ms");
            shutdownTimeout = 5000;
        }

        if (truststorePath == null || truststorePath.isBlank())
            logger.warn("Truststore path not set");

        if (truststorePath != null && truststorePassword == null)
            throw new IllegalArgumentException("Truststore password must be set via " + ENV_TRUSTSTORE_PASSWORD + " or properties file");
    }
}
