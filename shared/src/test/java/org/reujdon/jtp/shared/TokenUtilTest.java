package org.reujdon.jtp.shared;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TokenUtilTest {
    // Regex for URL-safe Base64 without padding
    private static final Pattern BASE64_URL_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private static final Base64.Decoder base64Decoder = Base64.getUrlDecoder();

    @Test
    void generatedTokenShouldNotBeNull() {
        String token = TokenUtil.generateSessionToken();
        assertNotNull(token, "Generated token should not be null");
    }

    @Test
    void generatedTokenShouldHaveCorrectLength() {
        String token = TokenUtil.generateSessionToken();
        // 32 bytes encoded in Base64 without padding should be 43 chars
        assertEquals(43, token.length(),
                "Token should be 43 characters long (32 bytes encoded in URL-safe Base64 without padding)");
    }

    @Test
    void generatedTokenShouldBeUrlSafe() {
        String token = TokenUtil.generateSessionToken();
        assertTrue(BASE64_URL_PATTERN.matcher(token).matches(),
                "Token should only contain URL-safe Base64 characters");
    }

    @Test
    void generatedTokenShouldBeValidBase64() {
        String token = TokenUtil.generateSessionToken();
        assertDoesNotThrow(() -> base64Decoder.decode(token),
                "Token should be valid Base64 that can be decoded");
    }

    @RepeatedTest(10)
    void generatedTokensShouldBeUnique() {
        String token1 = TokenUtil.generateSessionToken();
        String token2 = TokenUtil.generateSessionToken();
        assertNotEquals(token1, token2,
                "Subsequent tokens should be different (with high probability)");
    }

    @Test
    void decodedTokenShouldHaveCorrectByteLength() {
        String token = TokenUtil.generateSessionToken();
        byte[] decoded = base64Decoder.decode(token);
        assertEquals(32, decoded.length,
                "Decoded token should be 32 bytes long");
    }
}