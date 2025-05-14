package org.reujdon.jtp.shared.env;

/**
 * Abstraction layer for env operations providing consistent access across implementations.
 *
 * <p>This interface defines standard methods for reading env data
 * regardless of the underlying env processing library.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public interface EnvProvider {
    /**
     * Gets the value associated with the specified key.
     *
     * @param key the key to look up
     * @return the string value, or null if not found
     */
    String getEnv(String key);
}
