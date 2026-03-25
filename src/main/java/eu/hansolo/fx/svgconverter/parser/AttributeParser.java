package eu.hansolo.fx.svgconverter.parser;

import eu.hansolo.fx.svgconverter.model.SVGElement;
import eu.hansolo.fx.svgconverter.model.SVGTransform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Parses SVG style and presentation attributes.
 * Handles both inline style attributes and individual presentation attributes.
 */
public enum AttributeParser {
    INSTANCE;

    /**
     * Parse and apply attributes to an SVG element.
     */
    public void parseAttributes(final SVGElement element, final Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) { return; }

        // Parse style attribute first (can be overridden by presentation attributes)
        if (attributes.containsKey("style")) { parseStyleAttribute(element, attributes.get("style")); }

        // Parse individual presentation attributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            final String name  = entry.getKey();
            final String value = entry.getValue();

            if (value == null || value.trim().isEmpty()) { continue; }

            switch (name) {
                case "id"               -> element.setId(value);
                case "fill"             -> element.setFill(value);
                case "stroke"           -> element.setStroke(value);
                case "stroke-width"     -> element.setStrokeWidth(parseDouble(value, 1.0));
                case "stroke-linecap"   -> element.setStrokeLineCap(value);
                case "stroke-linejoin"  -> element.setStrokeLineJoin(value);
                case "stroke-dasharray" -> element.setStrokeDashArray(parseDashArray(value));
                case "opacity"          -> element.setOpacity(parseDouble(value, 1.0));
                case "fill-opacity"     -> element.setFillOpacity(parseDouble(value, 1.0));
                case "stroke-opacity"   -> element.setStrokeOpacity(parseDouble(value, 1.0));
                case "transform"        -> parseTransform(element, value);
            }

            // Store all attributes for reference
            element.setAttribute(name, value);
        }
    }

    /**
     * Parse CSS style attribute (e.g., "fill:red;stroke:blue;stroke-width:2").
     */
    private void parseStyleAttribute(final SVGElement element, final String style) {
        element.setStyleString(style);

        final Map<String, String> styleMap = parseStyleString(style);

        for (Map.Entry<String, String> entry : styleMap.entrySet()) {
            final String property = entry.getKey();
            final String value    = entry.getValue();

            switch (property) {
                case "fill"             -> element.setFill(value);
                case "stroke"           -> element.setStroke(value);
                case "stroke-width"     -> element.setStrokeWidth(parseDouble(value, 1.0));
                case "stroke-linecap"   -> element.setStrokeLineCap(value);
                case "stroke-linejoin"  -> element.setStrokeLineJoin(value);
                case "stroke-dasharray" -> element.setStrokeDashArray(parseDashArray(value));
                case "opacity"          -> element.setOpacity(parseDouble(value, 1.0));
                case "fill-opacity"     -> element.setFillOpacity(parseDouble(value, 1.0));
                case "stroke-opacity"   -> element.setStrokeOpacity(parseDouble(value, 1.0));
            }
        }
    }

    /**
     * Parse style string into property-value map.
     */
    private Map<String, String> parseStyleString(final String style) {
        final Map<String, String> styleMap = new HashMap<>();

        if (style == null || style.trim().isEmpty()) { return styleMap; }

        final String[] declarations = style.split(";");
        for (String declaration : declarations) {
            String[] parts = declaration.split(":", 2);
            if (parts.length == 2) {
                String property = parts[0].trim();
                String value    = parts[1].trim();
                styleMap.put(property, value);
            }
        }
        return styleMap;
    }

    /**
     * Parse transform attribute.
     */
    private void parseTransform(final SVGElement element, final String transformString) {
        try {
            final List<SVGTransform> transforms = TransformParser.parse(transformString);
            transforms.forEach(transform -> element.addTransform(transform));
        } catch (Exception e) {
            System.err.println("Failed to parse transform: " + transformString);
        }
    }

    /**
     * Parse stroke-dasharray attribute.
     */
    private double[] parseDashArray(final String dashArray) {
        if (dashArray == null || dashArray.trim().isEmpty() || dashArray.equals("none")) { return null; }

        final String[] parts  = dashArray.trim().split("[,\\s]+");
        final double[] result = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return result;
    }

    /**
     * Parse double value with fallback.
     */
    private double parseDouble(String value, final double defaultValue) {
        if (value == null || value.trim().isEmpty()) { return defaultValue; }

        // Remove units (px, pt, em, etc.)
        value = value.replaceAll("[a-zA-Z%]+$", "").trim();

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse ViewBox attribute (minX minY width height).
     */
    public double[] parseViewBox(final String viewBox) {
        if (viewBox == null || viewBox.trim().isEmpty()) { return null; }

        final String[] parts = viewBox.trim().split("[,\\s]+");
        if (parts.length != 4) { return null; }

        try {
            return new double[] { Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]) };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse points attribute for polygon/polyline (x1,y1 x2,y2 ...).
     */
    public double[] parsePoints(final String points) {
        if (points == null || points.trim().isEmpty()) { return new double[0]; }

        final String[] parts  = points.trim().split("[,\\s]+");
        final double[] result = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }
}
