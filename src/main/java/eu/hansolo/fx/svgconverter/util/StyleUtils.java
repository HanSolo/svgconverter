package eu.hansolo.fx.svgconverter.util;

import eu.hansolo.fx.svgconverter.model.SVGDocument;
import eu.hansolo.fx.svgconverter.model.SVGElement;
import eu.hansolo.fx.svgconverter.model.SVGGradient;
import eu.hansolo.fx.svgconverter.model.SVGLinearGradient;
import eu.hansolo.fx.svgconverter.model.SVGRadialGradient;
import eu.hansolo.fx.svgconverter.parser.ColorParser;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Utility class for converting SVG styles to JavaFX styles.
 */
public class StyleUtils {
    
    /**
     * Apply SVG element styles to a JavaFX Shape (without gradient support).
     * Use applyStyles(SVGElement, Shape, SVGDocument) for gradient support.
     */
    public static void applyStyles(final SVGElement element, final Shape shape) {
        applyStyles(element, shape, null);
    }
    
    /**
     * Apply SVG element styles to a JavaFX Shape with gradient support.
     */
    public static void applyStyles(final SVGElement element, final Shape shape, final SVGDocument document) {
        // Apply fill
        if (element.hasFill()) {
            final Paint fill = parsePaint(element.getFill(), document, element.getFillOpacity());
            if (fill != null) {
                shape.setFill(fill);
                shape.setOpacity(element.getOpacity());
            }
        } else {
            shape.setFill(null);
        }
        
        // Apply stroke
        if (element.hasStroke()) {
            final Paint stroke = parsePaint(element.getStroke(), document, element.getStrokeOpacity());
            if (stroke != null) {
                shape.setStroke(stroke);
                shape.setStrokeWidth(element.getStrokeWidth());
                shape.setStrokeLineCap(parseLineCap(element.getStrokeLineCap()));
                shape.setStrokeLineJoin(parseLineJoin(element.getStrokeLineJoin()));
                
                // Apply stroke dash array
                if (element.hasStrokeDashArray()) {
                    final Double[] dashArray = new Double[element.getStrokeDashArray().length];
                    for (int i = 0; i < element.getStrokeDashArray().length; i++) {
                        dashArray[i] = element.getStrokeDashArray()[i];
                    }
                    shape.getStrokeDashArray().addAll(dashArray);
                }
            }
        } else {
            shape.setStroke(null);
        }
    }

    /**
     * Parse SVG paint value with gradient reference support.
     * @param paintValue The paint value (color or url(#gradientId))
     * @param document The SVG document containing gradient definitions
     * @param opacity The opacity to apply to the paint
     * @return JavaFX Paint object or null
     */
    public static Paint parsePaint(final String paintValue, final SVGDocument document, final double opacity) {
        if (paintValue == null || paintValue.equals("none")) { return null; }
        
        // Check for gradient reference (url(#id))
        if (ColorParser.isGradientReference(paintValue)) {
            final String gradientId = ColorParser.extractGradientId(paintValue);
            if (gradientId != null && document != null) {
                final SVGGradient gradient = document.getGradient(gradientId);
                if (gradient != null) { return convertGradientToPaint(gradient, opacity); }
            }
            // Gradient reference but not found - return null
            return null;
        }
        
        // Parse as color
        final Color color = ColorParser.parse(paintValue);
        if (color != null && opacity < 1.0) { return color.deriveColor(0, 1, 1, opacity); }
        return color;
    }
    
    /**
     * Convert an SVGGradient to a JavaFX Paint object.
     */
    private static Paint convertGradientToPaint(final SVGGradient gradient, final double opacity) {
        if (gradient instanceof SVGLinearGradient) {
            return convertLinearGradient((SVGLinearGradient) gradient, opacity);
        } else if (gradient instanceof SVGRadialGradient) {
            return convertRadialGradient((SVGRadialGradient) gradient, opacity);
        }
        return null;
    }
    
    /**
     * Convert SVGLinearGradient to JavaFX LinearGradient.
     */
    private static LinearGradient convertLinearGradient(final SVGLinearGradient svgGradient, final double opacity) {
        final List<Stop> stops = new ArrayList<>();
        for (SVGGradient.GradientStop svgStop : svgGradient.getStops()) {
            final Color  color       = svgStop.getColor();
            final double stopOpacity = svgStop.getOpacity() * opacity;
            final Color  stopColor   = color.deriveColor(0, 1, 1, stopOpacity);
            stops.add(new Stop(svgStop.getOffset(), stopColor));
        }
        
        // Handle empty stops
        if (stops.isEmpty()) {
            stops.add(new Stop(0, Color.TRANSPARENT));
            stops.add(new Stop(1, Color.TRANSPARENT));
        }

        final boolean     proportional = svgGradient.isProportional();
        final CycleMethod cycleMethod  = CycleMethod.NO_CYCLE;
        return new LinearGradient(svgGradient.getX1(), svgGradient.getY1(), svgGradient.getX2(), svgGradient.getY2(), proportional, cycleMethod, stops);
    }
    
    /**
     * Convert SVGRadialGradient to JavaFX RadialGradient.
     */
    private static RadialGradient convertRadialGradient(final SVGRadialGradient svgGradient, final double opacity) {
        final List<Stop> stops = new ArrayList<>();
        for (SVGGradient.GradientStop svgStop : svgGradient.getStops()) {
            final Color  color       = svgStop.getColor();
            final double stopOpacity = svgStop.getOpacity() * opacity;
            final Color  stopColor   = color.deriveColor(0, 1, 1, stopOpacity);
            stops.add(new Stop(svgStop.getOffset(), stopColor));
        }
        
        // Handle empty stops
        if (stops.isEmpty()) {
            stops.add(new Stop(0, Color.TRANSPARENT));
            stops.add(new Stop(1, Color.TRANSPARENT));
        }

        final boolean     proportional = svgGradient.isProportional();
        final CycleMethod cycleMethod  = CycleMethod.NO_CYCLE;
        return new RadialGradient(
            0, // focusAngle
            0, // focusDistance
            svgGradient.getCx(),
            svgGradient.getCy(),
            svgGradient.getR(),
            proportional,
            cycleMethod,
            stops
        );
    }
    
    /**
     * Convert SVG line cap to JavaFX StrokeLineCap.
     */
    public static StrokeLineCap parseLineCap(final String lineCap) {
        if (lineCap == null) { return StrokeLineCap.BUTT; }
        switch (lineCap.toLowerCase()) {
            case "round" : return StrokeLineCap.ROUND;
            case "square": return StrokeLineCap.SQUARE;
            case "butt"  :
            default      : return StrokeLineCap.BUTT;
        }
    }
    
    /**
     * Convert SVG line join to JavaFX StrokeLineJoin.
     */
    public static StrokeLineJoin parseLineJoin(final String lineJoin) {
        if (lineJoin == null) { return StrokeLineJoin.MITER; }
        switch (lineJoin.toLowerCase()) {
            case "round": return StrokeLineJoin.ROUND;
            case "bevel": return StrokeLineJoin.BEVEL;
            case "miter":
            default     : return StrokeLineJoin.MITER;
        }
    }
    
    /**
     * Calculate effective opacity combining element opacity with fill/stroke opacity.
     */
    public static double calculateEffectiveOpacity(final double elementOpacity, final double specificOpacity) {
        return elementOpacity * specificOpacity;
    }
    
    /**
     * Apply opacity to a color.
     */
    public static Color applyOpacity(final Color color, final double opacity) {
        if (color == null) { return null; }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity() * opacity);
    }
}
