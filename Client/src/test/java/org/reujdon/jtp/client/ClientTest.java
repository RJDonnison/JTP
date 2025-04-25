package org.reujdon.jtp.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    @Test
    void testInitializationWithInvalidPortThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Client(-1));
        assertThrows(IllegalArgumentException.class, () -> new Client(65537));
    }

    @Test
    void testInitializationWithInvalidHostThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Client(null));
        assertThrows(IllegalArgumentException.class, () -> new Client(""));
        assertThrows(IllegalArgumentException.class, () -> new Client("     "));
    }

}