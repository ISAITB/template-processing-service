package ${package}.process;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Component that is used to handle the actual processing linked to this service.
 *
 * The current sample implementation is trivial and could be omitted. In more complex cases however such a
 * component would be the entry point into the service's potentially complex processing logic.
 */
@Component
public class ProcessHandler {

    /**
     * Lowercase the provided input.
     *
     * @param input The input string.
     * @return The output (null for null input).
     */
    public String lowerCase(String input) {
        String output = null;
        if (input != null) {
            output = input.toLowerCase(Locale.getDefault());
        }
        return output;
    }

    /**
     * Uppercase the provided input.
     *
     * @param input The input string.
     * @return The output (null for null input).
     */
    public String upperCase(String input) {
        String output = null;
        if (input != null) {
            output = input.toUpperCase(Locale.getDefault());
        }
        return output;
    }

}
