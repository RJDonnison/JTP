package org.reujdon.jtp.shared.json;

/**
 * A runtime exception that indicates an error occurred while parsing or handling JSON data.
 * <p>
 * This exception can be used to wrap lower-level JSON parsing errors such as {@link com.google.gson.JsonParseException},
 * providing additional context or abstraction from the specific JSON library being used.
 *
 * @see com.google.gson.JsonParseException
 */
public class JsonException extends RuntimeException {
    /**
     * Constructs a new {@code JsonException} with the specified detail message.
     *
     * @param message the detail message
     */
    public JsonException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code JsonException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception, typically a {@link com.google.gson.JsonParseException}
     */
    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
