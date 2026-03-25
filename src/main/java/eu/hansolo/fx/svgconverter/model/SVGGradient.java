package eu.hansolo.fx.svgconverter.model;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for SVG gradients (linear and radial).
 */
public abstract class SVGGradient {
    protected String             id;
    protected List<GradientStop> stops;
    protected String             gradientUnits;
    protected SVGTransform       gradientTransform;


    public SVGGradient() {
        this.id                = "";
        this.stops             = new ArrayList<>();
        this.gradientUnits     = "objectBoundingBox";
        this.gradientTransform = null;
    }


    public String getId() {
        return id;
    }
    public void setId(final String id) {
        this.id = id;
    }

    public List<GradientStop> getStops() {
        return stops;
    }
    public void addStop(final GradientStop stop) {
        this.stops.add(stop);
    }

    public String getGradientUnits() {
        return gradientUnits;
    }
    public void setGradientUnits(final String gradientUnits) {
        this.gradientUnits = gradientUnits;
    }

    public SVGTransform getGradientTransform() {
        return gradientTransform;
    }
    public void setGradientTransform(final SVGTransform gradientTransform) {
        this.gradientTransform = gradientTransform;
    }

    public boolean isProportional() {
        return "objectBoundingBox".equals(gradientUnits);
    }

    /**
     * Represents a gradient stop with offset and color.
     */
    public static class GradientStop {
        private double offset;
        private Color  color;
        private double opacity;

        public GradientStop(final double offset, final Color color) {
            this(offset, color, 1.0);
        }
        public GradientStop(final double offset, final Color color, final double opacity) {
            this.offset  = Math.max(0, Math.min(1, offset));
            this.color   = color;
            this.opacity = Math.max(0, Math.min(1, opacity));
        }


        public double getOffset() {
            return offset;
        }

        public Color getColor() {
            return color;
        }

        public double getOpacity() {
            return opacity;
        }

        public Color getColorWithOpacity() {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity() * opacity);
        }
    }
}
