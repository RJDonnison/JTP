package org.reujdon.jtp.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for retrieving values from `.properties` files.
 *
 * <p>Supports loading of string, integer, and boolean values with validation and logging.
 * Designed to simplify reading configuration data from properties files with error handling.
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class PropertiesUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * Utility method to load a property from a .properties file.
     *
     * @param filename the name of the properties file
     * @param property the property whose associated string is to be retrieved
     * @return the string corresponding to the given key, or null if the key is not found or an error occurs
     *
     * @see #getInteger(String, String)
     * @see #getBoolean(String, String)
     */
    public static String getString(String filename, String property) {
        File file = validateFile(filename, property);

        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            String prop = properties.getProperty(property);

            if (validateProperty(prop, property, file))
                return null;

            return prop.trim();
        } catch (IOException e) {
            logger.error("Error loading properties file: {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Utility method to load an integer from a .properties file.
     *
     * @param filename the name of the properties file
     * @param property the property whose associated integer is to be retrieved
     * @return the integer corresponding to the given key, or null if the key is not found or an error occurs
     *
     * @see #getString(String, String)
     * @see #getBoolean(String, String)
     */
    public static Integer getInteger(String filename, String property) {
        File file = validateFile(filename, property);

        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            String prop = properties.getProperty(property);

            if (validateProperty(prop, property, file))
                return null;

            return Integer.valueOf(prop.trim());
        } catch (IOException e) {
            logger.warn("Unable to load properties file: {}", file.getAbsolutePath(), e);
        } catch (NumberFormatException e) {
            logger.error("Error parsing integer property: {}", property);
        }

        return null;
    }


    /**
     * Utility method to load a boolean from a .properties file.
     *
     * @param filename the name of the properties file
     * @param property the property whose associated boolean is to be retrieved
     * @return the boolean corresponding to the given key, or null if the key is not found or an error occurs
     *
     * @see #getString(String, String)
     * @see #getInteger(String, String)
     */
    public static Boolean getBoolean(String filename, String property) {
        File file = validateFile(filename, property);

        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            String prop = properties.getProperty(property);

            if (validateProperty(prop, property, file))
                return null;

            return Boolean.parseBoolean(prop.trim());
        } catch (IOException e) {
            logger.warn("Unable to load properties file: {}", file.getAbsolutePath(), e);
        } catch (RuntimeException e) {
            logger.error("Error parsing boolean property: {}", property);
        }

        return null;
    }

    /**
     * Gets the base directory for resolving relative paths.
     * Defaults to the current working directory.
     */
    private static String getBaseDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * Validates and resolves a file path.
     * <p>
     * If the provided filename is not an absolute path, it is resolved relative to the base directory.
     * Logs a warning if either the filename or property is null.
     *
     * @param filename The filename to validate.
     * @param property The property name associated with the file (used for logging).
     * @return A resolved {@link File} object, or {@code null} if the input is invalid.
     */
    private static File validateFile(String filename, String property) {
        if (filename == null || property == null) {
            logger.warn("Null filename or property parameter");
            return null;
        }

        File file = new File(filename);
        if (!file.isAbsolute())
            return new File(getBaseDirectory(), filename);

        return file;
    }

    /**
     * Validates that a property value is not null or empty.
     * <p>
     * Logs debug information if the property is missing or empty.
     *
     * @param prop     The value of the property to validate.
     * @param property The name of the property (used for logging).
     * @param file     The file the property was expected to be found in (used for logging).
     * @return {@code true} if the property is valid (non-null and non-empty), {@code false} otherwise.
     */
    public static boolean validateProperty(String prop, String property, File file) {
        if (prop == null || prop.trim().isEmpty()) {
            logger.debug("Property '{}' not found or empty in file: {}", property, file.getAbsoluteFile());
            return true;
        }

        return false;
    }
}
