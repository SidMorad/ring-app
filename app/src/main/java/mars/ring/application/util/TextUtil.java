package mars.ring.application.util;

import java.util.Locale;

/**
 * Text utility class for common text usages.
 */
public class TextUtil {

    public static String format(String text, Object... args) {
        return String.format(Locale.ENGLISH, text, args);
    }

}
