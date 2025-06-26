package org.reujdon.jtp.shared.parse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TypeParserTest {
    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", "True", "tRuE"})
    void parseBooleanShouldReturnTrueWhenValidTrueString(String input) {
        assertTrue(TypeParser.parseBoolean(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "FALSE", "False", "fAlSe"})
    void parseBooleanShouldReturnFalseWhenValidFalseString(String input) {
        assertFalse(TypeParser.parseBoolean(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "\t", "\n", "\r"})
    void parseBooleanShouldThrowWhenNullOrBlank(String input) {
        assertThrows(ParseError.class, () -> TypeParser.parseBoolean(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "no", "1", "0", "t", "f", "truth", "falsey"})
    void parseBooleanShouldThrowWhenInvalidValue(String input) {
        assertThrows(ParseError.class, () -> TypeParser.parseBoolean(input));
    }

    @Test
    void parseBooleanShouldIncludeOriginalValueInErrorMessage() {
        String invalidValue = "maybe";
        ParseError exception = assertThrows(ParseError.class,
                () -> TypeParser.parseBoolean(invalidValue));

        assertTrue(exception.getMessage().contains(invalidValue));
    }
}