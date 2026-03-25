package eu.hansolo.fx.svgconverter.builder;

import eu.hansolo.fx.svgconverter.converter.CanvasConverter;
import eu.hansolo.fx.svgconverter.model.SVGDocument;
import eu.hansolo.fx.svgconverter.parser.SVGParser;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.InputStream;


/**
 * Builder for creating JavaFX Canvas from SVG files.
 * Provides a fluent API for configuring and building the canvas.
 */
public class CanvasBuilder {
    private SVGDocument document;
    private double      width               = 400;
    private double      height              = 400;
    private Color       backgroundColor     = Color.TRANSPARENT;
    private boolean     preserveAspectRatio = true;
    private boolean     autoSize            = false;
    private SVGParser   parser;


    public CanvasBuilder() {
        this.parser = new SVGParser();
    }


    /**
     * Load SVG from file path.
     */
    public CanvasBuilder fromFile(final String filePath) throws Exception {
        this.document = parser.parse(filePath);
        if (autoSize) { updateSizeFromDocument(); }
        return this;
    }

    /**
     * Load SVG from File object.
     */
    public CanvasBuilder fromFile(final File file) throws Exception {
        this.document = parser.parse(file);
        if (autoSize) { updateSizeFromDocument(); }
        return this;
    }

    /**
     * Load SVG from InputStream.
     */
    public CanvasBuilder fromStream(final InputStream inputStream) throws Exception {
        this.document = parser.parse(inputStream);
        if (autoSize) { updateSizeFromDocument(); }
        return this;
    }

    /**
     * Load SVG from string content.
     */
    public CanvasBuilder fromString(final String svgContent) throws Exception {
        this.document = parser.parseString(svgContent);
        if (autoSize) { updateSizeFromDocument(); }
        return this;
    }

    /**
     * Use an already parsed SVG document.
     */
    public CanvasBuilder fromDocument(final SVGDocument document) {
        this.document = document;
        if (autoSize) { updateSizeFromDocument(); }
        return this;
    }

    /**
     * Set canvas dimensions.
     */
    public CanvasBuilder withSize(final double width, final double height) {
        this.width    = width;
        this.height   = height;
        this.autoSize = false;
        return this;
    }

    /**
     * Set canvas width.
     */
    public CanvasBuilder withWidth(final double width) {
        this.width = width;
        return this;
    }

    /**
     * Set canvas height.
     */
    public CanvasBuilder withHeight(final double height) {
        this.height = height;
        return this;
    }

    /**
     * Automatically size canvas to match SVG dimensions.
     */
    public CanvasBuilder autoSize() {
        this.autoSize = true;
        if (document != null) { updateSizeFromDocument(); }
        return this;
    }

    /**
     * Set background color for the canvas.
     */
    public CanvasBuilder withBackgroundColor(final Color color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * Set whether to preserve aspect ratio when scaling.
     */
    public CanvasBuilder preserveAspectRatio(final boolean preserve) {
        this.preserveAspectRatio = preserve;
        return this;
    }

    /**
     * Build the JavaFX Canvas with the SVG rendered on it.
     */
    public Canvas build() {
        if (document == null) { throw new IllegalStateException("No SVG document loaded. Use fromFile(), fromStream(), or fromString() first."); }

        final Canvas          canvas = new Canvas(width, height);
        final GraphicsContext ctx    = canvas.getGraphicsContext2D();
        if (backgroundColor != Color.TRANSPARENT) {
            ctx.setFill(backgroundColor);
            ctx.fillRect(0, 0, width, height);
        }

        applyScaling(ctx);

        final CanvasConverter converter = new CanvasConverter(canvas);
        converter.convert(document);

        return canvas;
    }

    /**
     * Build and return the result with additional metadata.
     */
    public BuildResult buildWithMetadata() {
        final Canvas canvas = build();
        return new BuildResult(canvas, document);
    }

    private void updateSizeFromDocument() {
        if (document.getWidth() > 0) {this.width = document.getWidth(); }
        if (document.getHeight() > 0) { this.height = document.getHeight(); }
        if (document.hasViewBox()) {
            this.width  = document.getViewBox().getWidth();
            this.height = document.getViewBox().getHeight();
        }
    }

    private void applyScaling(final GraphicsContext ctx) {
        double sourceWidth  = document.getWidth();
        double sourceHeight = document.getHeight();

        if (document.hasViewBox()) {
            sourceWidth  = document.getViewBox().getWidth();
            sourceHeight = document.getViewBox().getHeight();
        }

        if (sourceWidth <= 0 || sourceHeight <= 0) { return; }

        final double scaleX = width / sourceWidth;
        final double scaleY = height / sourceHeight;

        if (preserveAspectRatio) {
            // Use the smaller scale to fit within bounds
            final double uniformScale = Math.min(scaleX, scaleY);
            ctx.scale(uniformScale, uniformScale);

            // Center the content
            final double offsetX = (width - sourceWidth * uniformScale) / 2.0 / uniformScale;
            final double offsetY = (height - sourceHeight * uniformScale) / 2.0 / uniformScale;
            ctx.translate(offsetX, offsetY);
        } else {
            // Stretch to fill
            ctx.scale(scaleX, scaleY);
        }
    }

    /**
     * Result of building the canvas with metadata.
     */
    public static class BuildResult {
        private final Canvas      canvas;
        private final SVGDocument document;


        public BuildResult(final Canvas canvas, final SVGDocument document) {
            this.canvas   = canvas;
            this.document = document;
        }


        public Canvas getCanvas() {
            return canvas;
        }

        public SVGDocument getDocument() {
            return document;
        }

        public double getCanvasWidth() {
            return canvas.getWidth();
        }

        public double getCanvasHeight() {
            return canvas.getHeight();
        }

        public double getSourceWidth() {
            return document.getWidth();
        }

        public double getSourceHeight() {
            return document.getHeight();
        }

        public boolean hasViewBox() {
            return document.hasViewBox();
        }
    }
}
