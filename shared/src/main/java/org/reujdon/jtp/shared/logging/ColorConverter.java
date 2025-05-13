package org.reujdon.jtp.shared.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

import java.util.HashMap;
import java.util.Map;

public class ColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    // Define color codes
    private static final Map<String, String> COLORS = new HashMap<>();
    static {
        COLORS.put("RED", ANSIConstants.RED_FG);
        COLORS.put("GREEN", ANSIConstants.GREEN_FG);
        COLORS.put("YELLOW", ANSIConstants.YELLOW_FG);
        COLORS.put("BLUE", ANSIConstants.BLUE_FG);
        COLORS.put("MAGENTA", ANSIConstants.MAGENTA_FG);
        COLORS.put("CYAN", ANSIConstants.CYAN_FG);
        COLORS.put("GRAY", "90");
        COLORS.put("BLACK", ANSIConstants.BLACK_FG);
        COLORS.put("WHITE", ANSIConstants.WHITE_FG);
        COLORS.put("BOLD", ANSIConstants.BOLD);
    }

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        String color = getFirstOption();

        if (color != null && COLORS.containsKey(color.toUpperCase()))
            return COLORS.get(color.toUpperCase());

        return ANSIConstants.DEFAULT_FG;
    }
}
