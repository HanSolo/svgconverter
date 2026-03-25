package eu.hansolo.fx.svgconverter.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Base class for all SVG elements.
 * Contains common attributes like id, fill, stroke, transforms, etc.
 */
public abstract class SVGElement {
    protected String              id;
    protected String              fill;
    protected String              stroke;
    protected double              strokeWidth;
    protected String              strokeLineCap;
    protected String              strokeLineJoin;
    protected double[]            strokeDashArray;
    protected double              opacity;
    protected double              fillOpacity;
    protected double              strokeOpacity;
    protected List<SVGTransform>  transforms;
    protected Map<String, String> attributes;
    protected String              styleString;


    public SVGElement() {
        this.id              = "";
        this.fill            = "black";
        this.stroke          = "none";
        this.strokeWidth     = 1.0;
        this.strokeLineCap   = "butt";
        this.strokeLineJoin  = "miter";
        this.strokeDashArray = null;
        this.opacity         = 1.0;
        this.fillOpacity     = 1.0;
        this.strokeOpacity   = 1.0;
        this.transforms      = new ArrayList<>();
        this.attributes      = new HashMap<>();
        this.styleString     = "";
    }


    public abstract String getType();

    // Getters and setters
    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }

    public String getFill() { return fill; }
    public void setFill(final String fill) { this.fill = fill; }

    public String getStroke() { return stroke; }
    public void setStroke(final String stroke) { this.stroke = stroke; }

    public double getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(final double strokeWidth) { this.strokeWidth = strokeWidth; }

    public String getStrokeLineCap() { return strokeLineCap; }
    public void setStrokeLineCap(final String strokeLineCap) { this.strokeLineCap = strokeLineCap; }

    public String getStrokeLineJoin() { return strokeLineJoin; }
    public void setStrokeLineJoin(final String strokeLineJoin) { this.strokeLineJoin = strokeLineJoin; }

    public double[] getStrokeDashArray() { return strokeDashArray; }
    public void setStrokeDashArray(final double[] strokeDashArray) { this.strokeDashArray = strokeDashArray; }
    public boolean hasStrokeDashArray() { return strokeDashArray != null && strokeDashArray.length > 0; }

    public double getOpacity() { return opacity; }
    public void setOpacity(final double opacity) { this.opacity = Math.max(0, Math.min(1, opacity)); }

    public double getFillOpacity() { return fillOpacity; }
    public void setFillOpacity(final double fillOpacity) { this.fillOpacity = Math.max(0, Math.min(1, fillOpacity)); }

    public double getStrokeOpacity() { return strokeOpacity; }
    public void setStrokeOpacity(final double strokeOpacity) { this.strokeOpacity = Math.max(0, Math.min(1, strokeOpacity)); }

    public List<SVGTransform> getTransforms() { return transforms; }
    public void addTransform(final SVGTransform transform) { this.transforms.add(transform); }
    public boolean hasTransforms() { return !transforms.isEmpty(); }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttribute(final String key, final String value) { this.attributes.put(key, value); }
    public String getAttribute(final String key) { return attributes.get(key); }

    public String getStyleString() { return styleString; }
    public void setStyleString(final String styleString) { this.styleString = styleString; }

    public boolean hasFill() { return fill != null && !fill.equals("none"); }
    public boolean hasStroke() { return stroke != null && !stroke.equals("none"); }

    /**
     * Check if fill is a gradient reference (e.g., "url(#gradientId)").
     */
    public boolean isFillGradient() {
        return fill != null && fill.trim().startsWith("url(");
    }

    /**
     * Check if stroke is a gradient reference (e.g., "url(#gradientId)").
     */
    public boolean isStrokeGradient() {
        return stroke != null && stroke.trim().startsWith("url(");
    }

    /**
     * Extract gradient ID from a url() reference.
     * For example, "url(#paint_linear)" returns "paint_linear".
     * Returns null if not a valid url reference.
     */
    public static String extractGradientId(final String urlReference) {
        if (urlReference == null || !urlReference.trim().startsWith("url(")) { return null; }

        final String trimmed = urlReference.trim();
        final int    start   = trimmed.indexOf('#');
        final int    end     = trimmed.indexOf(')', start);

        if (start != -1 && end != -1 && end > start) { return trimmed.substring(start + 1, end); }
        return null;
    }

    /**
     * Get the gradient ID from fill if it's a gradient reference.
     */
    public String getFillGradientId() {
        return extractGradientId(fill);
    }

    /**
     * Get the gradient ID from stroke if it's a gradient reference.
     */
    public String getStrokeGradientId() {
        return extractGradientId(stroke);
    }
}
