package org.reujdon.jtp.server;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.reujdon.jtp.shared.PropertiesUtil;
import org.reujdon.jtp.shared.env.EnvProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JTPServerConfigTest {
    @Test
    void testLoadFromEnvVarsValidValues() {
        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv("SERVER_PORT")).thenReturn("8080");
        when(mockEnv.getEnv("SERVER_AUTHENTICATION")).thenReturn("true");
        when(mockEnv.getEnv("SERVER_KEYSTORE_PATH")).thenReturn("/path/to/keystore");
        when(mockEnv.getEnv("SERVER_KEYSTORE_PASSWORD")).thenReturn("secret");

        JTPServerConfig config = new JTPServerConfig(mockEnv);
        config.loadFromEnvVars();

        assertEquals(8080, config.port);
        assertTrue(config.authenticate);
        assertEquals("/path/to/keystore", config.keystorePath);
        assertEquals("secret", config.keystorePassword);
    }

    @Test
    void testLoadFromEnvVarsInvalidPortThrowsException() {
        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv("SERVER_PORT")).thenReturn("invalid");

        JTPServerConfig config = new JTPServerConfig(mockEnv);

        assertThrows(IllegalArgumentException.class, config::loadFromEnvVars);

    }

    @Test
    void testLoadFromEnvVarsInvalidAuthenticateThrowsException() {
        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv("SERVER_AUTHENTICATION")).thenReturn("invalid");

        JTPServerConfig config = new JTPServerConfig(mockEnv);

        assertThrows(IllegalArgumentException.class, config::loadFromEnvVars);
    }

    @Test
    void testLoadFromPropertiesFileWithDefaults() {
        String configFile = "server.properties";

        try (MockedStatic<PropertiesUtil> propsMock = mockStatic(PropertiesUtil.class)) {
            propsMock.when(() -> PropertiesUtil.getInteger(configFile, "server.port")).thenReturn(1234);
            propsMock.when(() -> PropertiesUtil.getBoolean(configFile, "server.authenticate")).thenReturn(false);
            propsMock.when(() -> PropertiesUtil.getString(configFile, "server.path")).thenReturn("/file/path");
            propsMock.when(() -> PropertiesUtil.getString(configFile, "server.password")).thenReturn("filepass");

            JTPServerConfig config = new JTPServerConfig();
            config.loadFromPropertiesFile(configFile);

            assertEquals(1234, config.port);
            assertFalse(config.authenticate);
            assertEquals("/file/path", config.keystorePath);
            assertEquals("filepass", config.keystorePassword);
        }
    }

    @Test
    void testHasMissingConfigDetectsMissingFields() {
        JTPServerConfig config = new JTPServerConfig();

        assertTrue(config.hasMissingConfig());
        config.port = 1;
        config.authenticate = true;
        config.keystorePath = "z";
        config.keystorePassword = "p";
        assertFalse(config.hasMissingConfig());
    }

    @Test
    void testValidateConfigInvalidPortThrows() {
        JTPServerConfig config = new JTPServerConfig();

        config.port = 70000; // invalid
        config.authenticate = true;
        config.keystorePath = "/truststore";
        config.keystorePassword = "pass";

        assertThrows(IllegalArgumentException.class, config::validateConfig);
    }

    @Test
    void testValidateConfigSetsDefaultAuthenticate() {
        JTPServerConfig config = new JTPServerConfig();

        config.port = 1234;
        config.keystorePath = "/keystore";
        config.keystorePassword = "pass";

        config.validateConfig();

        assertFalse(config.authenticate);
    }

    @Test
    void testValidateConfigMissingKeystorePasswordThrowsWhenPathSet() {
        JTPServerConfig config = new JTPServerConfig();

        config.port = 1234;
        config.authenticate = true;

        assertDoesNotThrow(config::validateConfig);

        config.keystorePath = "/keystore";

        assertThrows(IllegalArgumentException.class, config::validateConfig);
    }

    @Test
    void testLoadConfigFallsBackToPropertiesFileWhenEnvMissing() {
        String configFile = "client.properties";

        EnvProvider mockEnv = mock(EnvProvider.class);
        when(mockEnv.getEnv(anyString())).thenReturn(null);

        JTPServerConfig config = new JTPServerConfig(mockEnv);

        try (MockedStatic<PropertiesUtil> propsMock = mockStatic(PropertiesUtil.class)) {
            propsMock.when(() -> PropertiesUtil.getInteger(configFile, "server.port")).thenReturn(9999);
            propsMock.when(() -> PropertiesUtil.getBoolean(configFile, "server.authenticate")).thenReturn(false);
            propsMock.when(() -> PropertiesUtil.getString(configFile, "server.path")).thenReturn("/prop/path");
            propsMock.when(() -> PropertiesUtil.getString(configFile, "server.password")).thenReturn("propPass");

            config.loadConfig(configFile, "unusedDefaultFile");

            assertEquals(9999, config.port);
            assertFalse(config.authenticate);
            assertEquals("/prop/path", config.keystorePath);
            assertEquals("propPass", config.keystorePassword);
        }
    }
}