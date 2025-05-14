package org.reujdon.jtp.shared.env;

/**
 * System-based implementation of the {@link EnvProvider} interface.
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class SystemEnvProvider implements EnvProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEnv(String key) {
        return System.getenv(key);
    }
}
