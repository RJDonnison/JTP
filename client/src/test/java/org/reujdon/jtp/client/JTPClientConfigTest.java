package org.reujdon.jtp.client;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.reujdon.jtp.shared.PropertiesUtil;
import org.reujdon.jtp.shared.env.EnvProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JTPClientConfigTest {

    @Test
    void testLoadFromEnvVarsValidValues() {
        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv("CLIENT_HOST")).thenReturn("localhost");
        when(mockEnv.getEnv("CLIENT_PORT")).thenReturn("8080");
        when(mockEnv.getEnv("CLIENT_API_KEY")).thenReturn("abc123");
        when(mockEnv.getEnv("CLIENT_SHUTDOWN_TIMEOUT")).thenReturn("3000");
        when(mockEnv.getEnv("CLIENT_TRUSTSTORE_PATH")).thenReturn("/path/to/truststore");
        when(mockEnv.getEnv("CLIENT_TRUSTSTORE_PASSWORD")).thenReturn("secret");

        JTPClientConfig config = new JTPClientConfig(mockEnv);
        config.loadFromEnvVars();

        assertEquals("localhost", config.host);
        assertEquals(8080, config.port);
        assertEquals("abc123", config.apiKey);
        assertEquals(3000, config.shutdownTimeout);
        assertEquals("/path/to/truststore", config.truststorePath);
        assertEquals("secret", config.truststorePassword);
    }

    @Test
    void testLoadFromEnvVarsInvalidPortThrowsException() {
        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv("CLIENT_PORT")).thenReturn("invalid");

        JTPClientConfig config = new JTPClientConfig(mockEnv);

        assertThrows(IllegalArgumentException.class, config::loadFromEnvVars);

    }

    @Test
    void testLoadFromEnvVarsInvalidTimeoutThrowsException() {
        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv("CLIENT_SHUTDOWN_TIMEOUT")).thenReturn("invalid");

        JTPClientConfig config = new JTPClientConfig(mockEnv);

        assertThrows(IllegalArgumentException.class, config::loadFromEnvVars);
    }

    @Test
    void testLoadFromPropertiesFileWithDefaults() {
        String configFile = "client.properties";

        try (MockedStatic<PropertiesUtil> propsMock = mockStatic(PropertiesUtil.class)) {
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.host")).thenReturn("filehost");
            propsMock.when(() -> PropertiesUtil.getInteger(configFile, "client.port")).thenReturn(1234);
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.apiKey")).thenReturn("filekey");
            propsMock.when(() -> PropertiesUtil.getInteger(configFile, "client.shutdownTimeout")).thenReturn(4500);
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.path")).thenReturn("/file/path");
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.password")).thenReturn("filepass");

            JTPClientConfig config = new JTPClientConfig();
            config.loadFromPropertiesFile(configFile);

            assertEquals("filehost", config.host);
            assertEquals(1234, config.port);
            assertEquals("filekey", config.apiKey);
            assertEquals(4500, config.shutdownTimeout);
            assertEquals("/file/path", config.truststorePath);
            assertEquals("filepass", config.truststorePassword);
        }
    }

    @Test
    void testHasMissingConfigDetectsMissingFields() {
        JTPClientConfig config = new JTPClientConfig();

        assertTrue(config.hasMissingConfig());
        config.host = "x";
        config.port = 1;
        config.apiKey = "y";
        config.truststorePath = "z";
        config.truststorePassword = "p";
        assertFalse(config.hasMissingConfig());
    }

    @Test
    void testValidateConfigMissingHostThrows() {
        JTPClientConfig config = new JTPClientConfig();

        config.port = 1234;
        config.apiKey = "key";
        config.shutdownTimeout = 1000;
        config.truststorePath = "/truststore";
        config.truststorePassword = "pass";

        assertThrows(IllegalArgumentException.class, config::validateConfig);
    }

    @Test
    void testValidateConfigInvalidPortThrows() {
        JTPClientConfig config = new JTPClientConfig();

        config.host = "localhost";
        config.port = 70000; // invalid
        config.apiKey = "key";
        config.shutdownTimeout = 1000;
        config.truststorePath = "/truststore";
        config.truststorePassword = "pass";

        assertThrows(IllegalArgumentException.class, config::validateConfig);
    }

    @Test
    void testValidateConfigMissingApiKeyThrows() {
        JTPClientConfig config = new JTPClientConfig();

        config.host = "localhost";
        config.port = 1234;
        config.shutdownTimeout = 1000;
        config.truststorePath = "/truststore";
        config.truststorePassword = "pass";

        assertThrows(IllegalArgumentException.class, config::validateConfig);
    }

    @Test
    void testValidateConfigSetsDefaultTimeout() {
        JTPClientConfig config = new JTPClientConfig();

        config.host = "localhost";
        config.port = 1234;
        config.apiKey = "key";
        config.shutdownTimeout = -1;
        config.truststorePath = "/truststore";
        config.truststorePassword = "pass";

        config.validateConfig();

        assertEquals(5000, config.shutdownTimeout);
    }

    @Test
    void testValidateConfigMissingTruststorePasswordThrowsWhenPathSet() {
        JTPClientConfig config = new JTPClientConfig();

        config.host = "localhost";
        config.port = 1234;
        config.apiKey = "key";
        config.shutdownTimeout = 1000;

        assertDoesNotThrow(config::validateConfig);

        config.truststorePath = "/truststore";

        assertThrows(IllegalArgumentException.class, config::validateConfig);
    }

    @Test
    void testLoadConfigFallsBackToPropertiesFileWhenEnvMissing() {
        String configFile = "client.properties";

        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv(anyString())).thenReturn(null);

        JTPClientConfig config = new JTPClientConfig(mockEnv);

        try (MockedStatic<PropertiesUtil> propsMock = mockStatic(PropertiesUtil.class)) {
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.host")).thenReturn("propHost");
            propsMock.when(() -> PropertiesUtil.getInteger(configFile, "client.port")).thenReturn(9999);
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.apiKey")).thenReturn("propKey");
            propsMock.when(() -> PropertiesUtil.getInteger(configFile, "client.shutdownTimeout")).thenReturn(6000);
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.path")).thenReturn("/prop/path");
            propsMock.when(() -> PropertiesUtil.getString(configFile, "client.password")).thenReturn("propPass");

            config.loadConfig(configFile, "unusedDefaultFile");

            assertEquals("propHost", config.host);
            assertEquals(9999, config.port);
            assertEquals("propKey", config.apiKey);
            assertEquals(6000, config.shutdownTimeout);
            assertEquals("/prop/path", config.truststorePath);
            assertEquals("propPass", config.truststorePassword);
        }
    }
}
