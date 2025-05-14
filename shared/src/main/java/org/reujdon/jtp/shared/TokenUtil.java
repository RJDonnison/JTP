package org.reujdon.jtp.shared;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating secure random session tokens.
 *
 * <p>This class provides a method to create cryptographically secure,
 * URL-safe tokens suitable for session identification or similar use cases.
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class TokenUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Generates a secure random session token.
     *
     * @return a secure, URL-safe session token
     */
    public static String generateSessionToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
