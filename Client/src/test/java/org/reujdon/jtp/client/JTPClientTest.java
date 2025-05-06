package org.reujdon.jtp.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

//TODO: fix
class JTPClientTest {
    @Test
    void testInitializationWithInvalidPortThrows() {
        assertThrows(IllegalArgumentException.class, () -> new JTPClient());
        assertThrows(IllegalArgumentException.class, () -> new JTPClient());
    }

    @Test
    void testInitializationWithInvalidHostThrows() {
        assertThrows(IllegalArgumentException.class, () -> new JTPClient(null));
        assertThrows(IllegalArgumentException.class, () -> new JTPClient(""));
        assertThrows(IllegalArgumentException.class, () -> new JTPClient("     "));
    }

}