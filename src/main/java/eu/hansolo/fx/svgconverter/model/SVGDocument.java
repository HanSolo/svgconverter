package eu.hansolo.fx.svgconverter.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents the root SVG document with viewBox and dimensions.
 */
public class SVGDocument {
    private double                           width;
    private double                           height;
    private ViewBox                          viewBox;
    private SVGElement                       root;
    private String                           title;
    private String                           description;
    private Map<String, SVGGradient>         gradients;
    private Map<String, Map<String, String>> cssClasses;


    public SVGDocument() {
        this.width       = 0;
        this.height      = 0;
        this.viewBox     = null;
        this.root        = new SVGGroup();
        this.title       = "";
        this.description = "";
        this.gradients   = new HashMap<>();
        this.cssClasses  = new HashMap<>();
    }


    public double getWidth() {
        return width;
    }
    public void setWidth(final double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }
    public void setHeight(final double height) {
        this.height = height;
    }

    public ViewBox getViewBox() {
        return viewBox;
    }
    public void setViewBox(final ViewBox viewBox) {
        this.viewBox = viewBox;
    }

    public boolean hasViewBox() {
        return viewBox != null;
    }

    public SVGElement getRoot() {
        return root;
    }
    public void setRoot(final SVGElement root) {
        this.root = root;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(final String description) {
        this.description = description;
    }

    public Map<String, SVGGradient> getGradients() {
        return gradients;
    }
    public void setGradients(final Map<String, SVGGradient> gradients) {
        this.gradients = gradients;
    }

    public void addGradient(final String id, final SVGGradient gradient) {
        this.gradients.put(id, gradient);
    }

    public SVGGradient getGradient(final String id) {
        return gradients.get(id);
    }

    public boolean hasGradient(final String id) {
        return gradients.containsKey(id);
    }

    public Map<String, Map<String, String>> getCssClasses() {
        return cssClasses;
    }
    public void setCssClasses(final Map<String, Map<String, String>> cssClasses) {
        this.cssClasses = cssClasses;
    }

    public void addCssClass(final String className, final Map<String, String> properties) {
        this.cssClasses.put(className, properties);
    }
    public Map<String, String> getCssClass(final String className) {
        return cssClasses.get(className);
    }

    public boolean hasCssClass(final String className) {
        return cssClasses.containsKey(className);
    }

    /**
     * Represents the SVG viewBox attribute.
     */
    public static class ViewBox {
        private double minX;
        private double minY;
        private double width;
        private double height;


        public ViewBox(final double minX, final double minY, final double width, final double height) {
            this.minX   = minX;
            this.minY   = minY;
            this.width  = width;
            this.height = height;
        }

        public double getMinX() {
            return minX;
        }
        public double getMinY() {
            return minY;
        }

        public double getWidth() {
            return width;
        }
        public double getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return String.format("ViewBox[%.2f %.2f %.2f %.2f]", minX, minY, width, height);
        }
    }
}
