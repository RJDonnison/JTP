package org.reujdon.jtp.shared.parse;

/**
 * Custom parser to transform between basic types
 *
 * <p>Provides multiple parsing methods to parse between types when the base parser does not suit the use case.</p>
 *
 * @author Reuben Donnison
 * @version 0.2
 */
public class TypeParser {
    /**
     * Parser to transform string to boolean throwing an error if string is an invalid boolean
     *
     * @param input string representation of the boolean to parse
     * @return boolean
     * @throws ParseError when string is not valid boolean
     */
    public static Boolean parseBoolean(String input) {
        if (input == null || input.isBlank())
            throw new ParseError("Input cannot be null or blank");

        if (input.equalsIgnoreCase("true"))
            return true;

        if (input.equalsIgnoreCase("false"))
            return false;

        throw new ParseError("Invalid boolean value: " + input);
    }
}
