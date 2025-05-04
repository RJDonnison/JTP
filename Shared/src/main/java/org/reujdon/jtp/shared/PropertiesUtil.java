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
     * @param filename the name of the properties file (without the .properties extension)
     * @param property the property whose associated value is to be retrieved
     * @return the value corresponding to the given key, or null if the key is not found or an error occurs
     */
    public static String getProperty(String filename, String property) {
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
}
