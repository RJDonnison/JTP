package org.reujdon.jtp.shared.parse;

/**
 * A runtime exception that indicates an error occurred while parsing basic types using {@link TypeParser}.
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class ParseError extends RuntimeException {
    /**
     * Constructs a new {@code ParseError} with the specified detail message.
     *
     * @param message the detail message
     */
    public ParseError(String message) {
        super(message);
    }
}
