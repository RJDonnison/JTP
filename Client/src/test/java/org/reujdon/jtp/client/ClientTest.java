package org.reujdon.jtp.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

//TODO: fix
class ClientTest {
    @Test
    void testInitializationWithInvalidPortThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Client());
        assertThrows(IllegalArgumentException.class, () -> new Client());
    }

    @Test
    void testInitializationWithInvalidHostThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Client(null));
        assertThrows(IllegalArgumentException.class, () -> new Client(""));
        assertThrows(IllegalArgumentException.class, () -> new Client("     "));
    }

}