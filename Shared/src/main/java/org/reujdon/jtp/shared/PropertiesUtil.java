package org.reujdon.jtp.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
     */
    public static String getString(String filename, String property) {
        Properties properties = new Properties();

        // Ensure the filename has the .properties extension
        if (!filename.endsWith(".properties"))
            filename += ".properties";

        try (FileInputStream fis = new FileInputStream(filename)) {
            properties.load(fis);
            return properties.getProperty(property);
        } catch (IOException e) {
            logger.error("Error loading properties file: {}", filename);
        }

        return null;
    }

    /**
     * Utility method to load an integer from a .properties file.
     *
     * @param filename the name of the properties file
     * @param property the property whose associated integer is to be retrieved
     * @return the integer corresponding to the given key, or null if the key is not found or an error occurs
     *
     * @see #getString(String, String)
     */
    public static Integer getInteger(String filename, String property) {
        Properties properties = new Properties();

        if (!filename.endsWith(".properties"))
            filename += ".properties";

        try (FileInputStream fis = new FileInputStream(filename)) {
            properties.load(fis);
            return Integer.valueOf(properties.getProperty(property));
        } catch (IOException e) {
            logger.warn("Unable to load properties file: {}", filename);
        } catch (NumberFormatException e) {
            logger.error("Error parsing property: {}", property);
        }

        return null;
    }
}
