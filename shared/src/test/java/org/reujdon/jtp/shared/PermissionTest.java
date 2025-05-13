package org.reujdon.jtp.shared;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionTest {

    @ParameterizedTest(name = "{0}.hasPermission({1}) == {2}")
    @CsvSource({
            // NONE
            "NONE, NONE, true",
            "NONE, READ, false",
            "NONE, WRITE, false",
            "NONE, FULL, false",

            // READ
            "READ, NONE, true",
            "READ, READ, true",
            "READ, WRITE, false",
            "READ, FULL, false",

            // WRITE
            "WRITE, NONE, true",
            "WRITE, READ, false",
            "WRITE, WRITE, true",
            "WRITE, FULL, false",

            // FULL
            "FULL, NONE, true",
            "FULL, READ, true",
            "FULL, WRITE, true",
            "FULL, FULL, true"
    })
    void testHasPermission(Permission base, Permission target, boolean expected) {
        assertEquals(expected, base.hasPermission(target));
    }
}