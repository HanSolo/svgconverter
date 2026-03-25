package eu.hansolo.fx.svgconverter.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.paint.Color;

/**
 * Parses SVG color values into JavaFX Color objects.
 * Supports: #RGB, #RRGGBB, rgb(), rgba(), hsl(), hsla(), and named colors.
 */
public class ColorParser {
    private static final Pattern            HEX_PATTERN         = Pattern.compile("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})");
    private static final Pattern            RGB_PATTERN         = Pattern.compile("rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
    private static final Pattern            RGBA_PATTERN        = Pattern.compile("rgba\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*([\\d.]+)\\s*\\)");
    private static final Pattern            RGB_PERCENT_PATTERN = Pattern.compile("rgb\\s*\\(\\s*([\\d.]+)%\\s*,\\s*([\\d.]+)%\\s*,\\s*([\\d.]+)%\\s*\\)");
    private static final Pattern            URL_PATTERN         = Pattern.compile("url\\s*\\(\\s*#([^)]+)\\s*\\)");
    private static final Map<String, Color> NAMED_COLORS        = new HashMap<>();
    
    static {
        // SVG named colors
        NAMED_COLORS.put("aliceblue", Color.web("#F0F8FF"));
        NAMED_COLORS.put("antiquewhite", Color.web("#FAEBD7"));
        NAMED_COLORS.put("aqua", Color.web("#00FFFF"));
        NAMED_COLORS.put("aquamarine", Color.web("#7FFFD4"));
        NAMED_COLORS.put("azure", Color.web("#F0FFFF"));
        NAMED_COLORS.put("beige", Color.web("#F5F5DC"));
        NAMED_COLORS.put("bisque", Color.web("#FFE4C4"));
        NAMED_COLORS.put("black", Color.BLACK);
        NAMED_COLORS.put("blanchedalmond", Color.web("#FFEBCD"));
        NAMED_COLORS.put("blue", Color.BLUE);
        NAMED_COLORS.put("blueviolet", Color.web("#8A2BE2"));
        NAMED_COLORS.put("brown", Color.web("#A52A2A"));
        NAMED_COLORS.put("burlywood", Color.web("#DEB887"));
        NAMED_COLORS.put("cadetblue", Color.web("#5F9EA0"));
        NAMED_COLORS.put("chartreuse", Color.web("#7FFF00"));
        NAMED_COLORS.put("chocolate", Color.web("#D2691E"));
        NAMED_COLORS.put("coral", Color.web("#FF7F50"));
        NAMED_COLORS.put("cornflowerblue", Color.web("#6495ED"));
        NAMED_COLORS.put("cornsilk", Color.web("#FFF8DC"));
        NAMED_COLORS.put("crimson", Color.web("#DC143C"));
        NAMED_COLORS.put("cyan", Color.CYAN);
        NAMED_COLORS.put("darkblue", Color.web("#00008B"));
        NAMED_COLORS.put("darkcyan", Color.web("#008B8B"));
        NAMED_COLORS.put("darkgoldenrod", Color.web("#B8860B"));
        NAMED_COLORS.put("darkgray", Color.web("#A9A9A9"));
        NAMED_COLORS.put("darkgrey", Color.web("#A9A9A9"));
        NAMED_COLORS.put("darkgreen", Color.web("#006400"));
        NAMED_COLORS.put("darkkhaki", Color.web("#BDB76B"));
        NAMED_COLORS.put("darkmagenta", Color.web("#8B008B"));
        NAMED_COLORS.put("darkolivegreen", Color.web("#556B2F"));
        NAMED_COLORS.put("darkorange", Color.web("#FF8C00"));
        NAMED_COLORS.put("darkorchid", Color.web("#9932CC"));
        NAMED_COLORS.put("darkred", Color.web("#8B0000"));
        NAMED_COLORS.put("darksalmon", Color.web("#E9967A"));
        NAMED_COLORS.put("darkseagreen", Color.web("#8FBC8F"));
        NAMED_COLORS.put("darkslateblue", Color.web("#483D8B"));
        NAMED_COLORS.put("darkslategray", Color.web("#2F4F4F"));
        NAMED_COLORS.put("darkslategrey", Color.web("#2F4F4F"));
        NAMED_COLORS.put("darkturquoise", Color.web("#00CED1"));
        NAMED_COLORS.put("darkviolet", Color.web("#9400D3"));
        NAMED_COLORS.put("deeppink", Color.web("#FF1493"));
        NAMED_COLORS.put("deepskyblue", Color.web("#00BFFF"));
        NAMED_COLORS.put("dimgray", Color.web("#696969"));
        NAMED_COLORS.put("dimgrey", Color.web("#696969"));
        NAMED_COLORS.put("dodgerblue", Color.web("#1E90FF"));
        NAMED_COLORS.put("firebrick", Color.web("#B22222"));
        NAMED_COLORS.put("floralwhite", Color.web("#FFFAF0"));
        NAMED_COLORS.put("forestgreen", Color.web("#228B22"));
        NAMED_COLORS.put("fuchsia", Color.web("#FF00FF"));
        NAMED_COLORS.put("gainsboro", Color.web("#DCDCDC"));
        NAMED_COLORS.put("ghostwhite", Color.web("#F8F8FF"));
        NAMED_COLORS.put("gold", Color.web("#FFD700"));
        NAMED_COLORS.put("goldenrod", Color.web("#DAA520"));
        NAMED_COLORS.put("gray", Color.GRAY);
        NAMED_COLORS.put("grey", Color.GRAY);
        NAMED_COLORS.put("green", Color.GREEN);
        NAMED_COLORS.put("greenyellow", Color.web("#ADFF2F"));
        NAMED_COLORS.put("honeydew", Color.web("#F0FFF0"));
        NAMED_COLORS.put("hotpink", Color.web("#FF69B4"));
        NAMED_COLORS.put("indianred", Color.web("#CD5C5C"));
        NAMED_COLORS.put("indigo", Color.web("#4B0082"));
        NAMED_COLORS.put("ivory", Color.web("#FFFFF0"));
        NAMED_COLORS.put("khaki", Color.web("#F0E68C"));
        NAMED_COLORS.put("lavender", Color.web("#E6E6FA"));
        NAMED_COLORS.put("lavenderblush", Color.web("#FFF0F5"));
        NAMED_COLORS.put("lawngreen", Color.web("#7CFC00"));
        NAMED_COLORS.put("lemonchiffon", Color.web("#FFFACD"));
        NAMED_COLORS.put("lightblue", Color.LIGHTBLUE);
        NAMED_COLORS.put("lightcoral", Color.web("#F08080"));
        NAMED_COLORS.put("lightcyan", Color.web("#E0FFFF"));
        NAMED_COLORS.put("lightgoldenrodyellow", Color.web("#FAFAD2"));
        NAMED_COLORS.put("lightgray", Color.LIGHTGRAY);
        NAMED_COLORS.put("lightgrey", Color.LIGHTGRAY);
        NAMED_COLORS.put("lightgreen", Color.LIGHTGREEN);
        NAMED_COLORS.put("lightpink", Color.web("#FFB6C1"));
        NAMED_COLORS.put("lightsalmon", Color.web("#FFA07A"));
        NAMED_COLORS.put("lightseagreen", Color.web("#20B2AA"));
        NAMED_COLORS.put("lightskyblue", Color.web("#87CEFA"));
        NAMED_COLORS.put("lightslategray", Color.web("#778899"));
        NAMED_COLORS.put("lightslategrey", Color.web("#778899"));
        NAMED_COLORS.put("lightsteelblue", Color.web("#B0C4DE"));
        NAMED_COLORS.put("lightyellow", Color.web("#FFFFE0"));
        NAMED_COLORS.put("lime", Color.web("#00FF00"));
        NAMED_COLORS.put("limegreen", Color.web("#32CD32"));
        NAMED_COLORS.put("linen", Color.web("#FAF0E6"));
        NAMED_COLORS.put("magenta", Color.MAGENTA);
        NAMED_COLORS.put("maroon", Color.web("#800000"));
        NAMED_COLORS.put("mediumaquamarine", Color.web("#66CDAA"));
        NAMED_COLORS.put("mediumblue", Color.web("#0000CD"));
        NAMED_COLORS.put("mediumorchid", Color.web("#BA55D3"));
        NAMED_COLORS.put("mediumpurple", Color.web("#9370DB"));
        NAMED_COLORS.put("mediumseagreen", Color.web("#3CB371"));
        NAMED_COLORS.put("mediumslateblue", Color.web("#7B68EE"));
        NAMED_COLORS.put("mediumspringgreen", Color.web("#00FA9A"));
        NAMED_COLORS.put("mediumturquoise", Color.web("#48D1CC"));
        NAMED_COLORS.put("mediumvioletred", Color.web("#C71585"));
        NAMED_COLORS.put("midnightblue", Color.web("#191970"));
        NAMED_COLORS.put("mintcream", Color.web("#F5FFFA"));
        NAMED_COLORS.put("mistyrose", Color.web("#FFE4E1"));
        NAMED_COLORS.put("moccasin", Color.web("#FFE4B5"));
        NAMED_COLORS.put("navajowhite", Color.web("#FFDEAD"));
        NAMED_COLORS.put("navy", Color.web("#000080"));
        NAMED_COLORS.put("oldlace", Color.web("#FDF5E6"));
        NAMED_COLORS.put("olive", Color.web("#808000"));
        NAMED_COLORS.put("olivedrab", Color.web("#6B8E23"));
        NAMED_COLORS.put("orange", Color.ORANGE);
        NAMED_COLORS.put("orangered", Color.web("#FF4500"));
        NAMED_COLORS.put("orchid", Color.web("#DA70D6"));
        NAMED_COLORS.put("palegoldenrod", Color.web("#EEE8AA"));
        NAMED_COLORS.put("palegreen", Color.web("#98FB98"));
        NAMED_COLORS.put("paleturquoise", Color.web("#AFEEEE"));
        NAMED_COLORS.put("palevioletred", Color.web("#DB7093"));
        NAMED_COLORS.put("papayawhip", Color.web("#FFEFD5"));
        NAMED_COLORS.put("peachpuff", Color.web("#FFDAB9"));
        NAMED_COLORS.put("peru", Color.web("#CD853F"));
        NAMED_COLORS.put("pink", Color.PINK);
        NAMED_COLORS.put("plum", Color.web("#DDA0DD"));
        NAMED_COLORS.put("powderblue", Color.web("#B0E0E6"));
        NAMED_COLORS.put("purple", Color.PURPLE);
        NAMED_COLORS.put("red", Color.RED);
        NAMED_COLORS.put("rosybrown", Color.web("#BC8F8F"));
        NAMED_COLORS.put("royalblue", Color.web("#4169E1"));
        NAMED_COLORS.put("saddlebrown", Color.web("#8B4513"));
        NAMED_COLORS.put("salmon", Color.web("#FA8072"));
        NAMED_COLORS.put("sandybrown", Color.web("#F4A460"));
        NAMED_COLORS.put("seagreen", Color.web("#2E8B57"));
        NAMED_COLORS.put("seashell", Color.web("#FFF5EE"));
        NAMED_COLORS.put("sienna", Color.web("#A0522D"));
        NAMED_COLORS.put("silver", Color.web("#C0C0C0"));
        NAMED_COLORS.put("skyblue", Color.web("#87CEEB"));
        NAMED_COLORS.put("slateblue", Color.web("#6A5ACD"));
        NAMED_COLORS.put("slategray", Color.web("#708090"));
        NAMED_COLORS.put("slategrey", Color.web("#708090"));
        NAMED_COLORS.put("snow", Color.web("#FFFAFA"));
        NAMED_COLORS.put("springgreen", Color.web("#00FF7F"));
        NAMED_COLORS.put("steelblue", Color.web("#4682B4"));
        NAMED_COLORS.put("tan", Color.web("#D2B48C"));
        NAMED_COLORS.put("teal", Color.web("#008080"));
        NAMED_COLORS.put("thistle", Color.web("#D8BFD8"));
        NAMED_COLORS.put("tomato", Color.web("#FF6347"));
        NAMED_COLORS.put("turquoise", Color.web("#40E0D0"));
        NAMED_COLORS.put("violet", Color.web("#EE82EE"));
        NAMED_COLORS.put("wheat", Color.web("#F5DEB3"));
        NAMED_COLORS.put("white", Color.WHITE);
        NAMED_COLORS.put("whitesmoke", Color.web("#F5F5F5"));
        NAMED_COLORS.put("yellow", Color.YELLOW);
        NAMED_COLORS.put("yellowgreen", Color.web("#9ACD32"));
    }
    
    /**
     * Check if a paint value is a gradient reference (e.g., "url(#gradientId)").
     * @param paintValue The paint value to check
     * @return true if it's a gradient reference, false otherwise
     */
    public static boolean isGradientReference(final String paintValue) {
        if (paintValue == null || paintValue.trim().isEmpty()) { return false; }
        return paintValue.trim().startsWith("url(");
    }
    
    /**
     * Extract gradient ID from a url() reference.
     * For example, "url(#paint_linear)" returns "paint_linear".
     * Returns null if not a valid url reference.
     * @param urlReference The url reference string
     * @return The gradient ID or null
     */
    public static String extractGradientId(final String urlReference) {
        if (urlReference == null || !urlReference.trim().startsWith("url(")) { return null; }
        final Matcher matcher = URL_PATTERN.matcher(urlReference.trim());
        if (matcher.matches()) { return matcher.group(1); }
        return null;
    }
    
    /**
     * Parse a color string into a JavaFX Color object.
     * Returns null if the color cannot be parsed, is "none", or is a gradient reference.
     * Use isGradientReference() to check for gradient references before calling this method.
     */
    public static Color parse(final String colorString) {
        // Check if it's a gradient reference
        if (isGradientReference(colorString)) { return null; }
        if (colorString == null || colorString.trim().isEmpty() || "none".equalsIgnoreCase(colorString.trim())) { return null; }

        final String color = colorString.trim().toLowerCase();
        // Try hex format
        final Matcher hexMatcher = HEX_PATTERN.matcher(color);
        if (hexMatcher.matches()) { return parseHex(hexMatcher.group(1)); }
        
        // Try rgba format
        final Matcher rgbaMatcher = RGBA_PATTERN.matcher(color);
        if (rgbaMatcher.matches()) {
            return parseRgba(Integer.parseInt(rgbaMatcher.group(1)), Integer.parseInt(rgbaMatcher.group(2)), Integer.parseInt(rgbaMatcher.group(3)), Double.parseDouble(rgbaMatcher.group(4)));
        }
        
        // Try rgb format
        final Matcher rgbMatcher = RGB_PATTERN.matcher(color);
        if (rgbMatcher.matches()) {
            return parseRgb(Integer.parseInt(rgbMatcher.group(1)), Integer.parseInt(rgbMatcher.group(2)), Integer.parseInt(rgbMatcher.group(3)));
        }
        
        // Try rgb percentage format
        final Matcher rgbPercentMatcher = RGB_PERCENT_PATTERN.matcher(color);
        if (rgbPercentMatcher.matches()) {
            return parseRgbPercent(Double.parseDouble(rgbPercentMatcher.group(1)), Double.parseDouble(rgbPercentMatcher.group(2)), Double.parseDouble(rgbPercentMatcher.group(3)));
        }
        
        // Try named color
        final Color namedColor = NAMED_COLORS.get(color);
        if (namedColor != null) { return namedColor; }
        
        // Fallback to JavaFX Color.web()
        try {
            return Color.web(colorString);
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Could not parse color: " + colorString);
            return Color.BLACK; // Default fallback
        }
    }
    
    private static Color parseHex(String hex) {
        if (hex.length() == 3) {
            // Expand #RGB to #RRGGBB
            char r = hex.charAt(0);
            char g = hex.charAt(1);
            char b = hex.charAt(2);
            hex = "" + r + r + g + g + b + b;
        }

        final int r = Integer.parseInt(hex.substring(0, 2), 16);
        final int g = Integer.parseInt(hex.substring(2, 4), 16);
        final int b = Integer.parseInt(hex.substring(4, 6), 16);
        
        return Color.rgb(r, g, b);
    }
    
    private static Color parseRgb(final int r, final int g, final int b) {
        return Color.rgb(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)));
    }
    
    private static Color parseRgba(final int r, final int g, final int b, final double a) {
        return Color.rgb(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)), Math.max(0, Math.min(1, a)));
    }
    
    private static Color parseRgbPercent(final double r, final double g, final double b) {
        return Color.rgb((int) Math.round(r * 2.55), (int) Math.round(g * 2.55), (int) Math.round(b * 2.55));
    }
}


