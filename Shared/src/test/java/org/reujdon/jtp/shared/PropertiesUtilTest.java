package org.reujdon.jtp.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PropertiesUtilTest {
    @Test
    void shouldGetStringPropertyFromFile(@TempDir Path tempDir) throws IOException {
        // Create a test properties file
        File propFile = tempDir.resolve("test.properties").toFile();
        writeProperties(propFile, "test.key", "test.value", "another.key", "another.value");

        // Test existing property
        assertEquals("test.value", PropertiesUtil.getString(propFile.getAbsolutePath(), "test.key"));

        // Test another property
        assertEquals("another.value", PropertiesUtil.getString(propFile.getAbsolutePath(), "another.key"));

        // Test non-existent property
        assertNull(PropertiesUtil.getString(propFile.getAbsolutePath(), "nonexistent.key"));
    }

    @Test
    void shouldGetStringPropertyFromFileWithoutExtension(@TempDir Path tempDir) throws IOException {
        File propFile = tempDir.resolve("test").toFile();
        writeProperties(propFile, "test.key", "test.value");

        assertEquals("test.value", PropertiesUtil.getString(propFile.getAbsolutePath(), "test.key"));
    }

    @Test
    void shouldReturnNullForNonExistentFile() {
        assertNull(PropertiesUtil.getString("nonexistent.properties", "any.key"));
    }

    @Test
    void shouldGetIntegerPropertyFromFile(@TempDir Path tempDir) throws IOException {
        File propFile = tempDir.resolve("numbers.properties").toFile();
        writeProperties(propFile, "valid.int", "42", "invalid.int", "not.a.number");

        // Test valid integer
        assertEquals(42, PropertiesUtil.getInteger(propFile.getAbsolutePath(), "valid.int"));

        // Test invalid integer
        assertNull(PropertiesUtil.getInteger(propFile.getAbsolutePath(), "invalid.int"));

        // Test non-existent property
        assertNull(PropertiesUtil.getInteger(propFile.getAbsolutePath(), "nonexistent.key"));
    }

    @Test
    void shouldGetIntegerPropertyFromFileWithoutExtension(@TempDir Path tempDir) throws IOException {
        File propFile = tempDir.resolve("numbers").toFile();
        writeProperties(propFile, "port", "8080");

        assertEquals(8080, PropertiesUtil.getInteger(propFile.getAbsolutePath(), "port"));
    }

    @Test
    void shouldReturnNullForNonExistentFileWhenGettingInteger() {
        assertNull(PropertiesUtil.getInteger("nonexistent.properties", "any.key"));
    }

    @Test
    void shouldHandleFileWithEmptyValues(@TempDir Path tempDir) throws IOException {
        File propFile = tempDir.resolve("empty.properties").toFile();
        writeProperties(propFile, "empty.value", "", "null.value", null);

        assertNull(PropertiesUtil.getString(propFile.getAbsolutePath(), "empty.value"));
        assertNull(PropertiesUtil.getString(propFile.getAbsolutePath(), "null.value"));
    }

    private void writeProperties(File file, String... keyValues) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < keyValues.length; i += 2) {
                String value = keyValues[i+1] != null ? keyValues[i+1] : "";
                writer.write(keyValues[i] + "=" + value + "\n");
            }
        }
    }
}