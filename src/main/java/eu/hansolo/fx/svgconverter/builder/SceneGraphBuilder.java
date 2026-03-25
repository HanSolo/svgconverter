package eu.hansolo.fx.svgconverter.builder;

import eu.hansolo.fx.svgconverter.converter.SceneGraphConverter;
import eu.hansolo.fx.svgconverter.model.SVGDocument;
import eu.hansolo.fx.svgconverter.parser.SVGParser;
import java.io.File;
import java.io.InputStream;
import javafx.scene.Group;
import javafx.scene.transform.Affine;

/**
 * Builder for creating JavaFX SceneGraph from SVG files.
 * Provides a fluent API for configuring and building the scene graph.
 */
public class SceneGraphBuilder {
    
    private SVGDocument document;
    private double scale = 1.0;
    private boolean preserveAspectRatio = true;
    private double targetWidth = -1;
    private double targetHeight = -1;
    private SVGParser parser;
    
    public SceneGraphBuilder() {
        this.parser = new SVGParser();
    }
    
    /**
     * Load SVG from file path.
     */
    public SceneGraphBuilder fromFile(String filePath) throws Exception {
        this.document = parser.parse(filePath);
        return this;
    }
    
    /**
     * Load SVG from File object.
     */
    public SceneGraphBuilder fromFile(File file) throws Exception {
        this.document = parser.parse(file);
        return this;
    }
    
    /**
     * Load SVG from InputStream.
     */
    public SceneGraphBuilder fromStream(InputStream inputStream) throws Exception {
        this.document = parser.parse(inputStream);
        return this;
    }
    
    /**
     * Load SVG from string content.
     */
    public SceneGraphBuilder fromString(String svgContent) throws Exception {
        this.document = parser.parseString(svgContent);
        return this;
    }
    
    /**
     * Use an already parsed SVG document.
     */
    public SceneGraphBuilder fromDocument(SVGDocument document) {
        this.document = document;
        return this;
    }
    
    /**
     * Set uniform scale factor.
     */
    public SceneGraphBuilder withScale(double scale) {
        this.scale = scale;
        return this;
    }
    
    /**
     * Set target dimensions. The SVG will be scaled to fit.
     */
    public SceneGraphBuilder withSize(double width, double height) {
        this.targetWidth = width;
        this.targetHeight = height;
        return this;
    }
    
    /**
     * Set whether to preserve aspect ratio when scaling.
     */
    public SceneGraphBuilder preserveAspectRatio(boolean preserve) {
        this.preserveAspectRatio = preserve;
        return this;
    }
    
    /**
     * Build the JavaFX Group containing the SVG scene graph.
     */
    public Group build() {
        if (document == null) {
            throw new IllegalStateException("No SVG document loaded. Use fromFile(), fromStream(), or fromString() first.");
        }
        
        // Convert SVG document to JavaFX scene graph
        SceneGraphConverter converter = new SceneGraphConverter();
        Group root = converter.convert(document);
        
        // Apply scaling if needed
        if (targetWidth > 0 && targetHeight > 0) {
            applyTargetSize(root);
        } else if (scale != 1.0) {
            root.getTransforms().add(new Affine(scale, 0, 0, 0, scale, 0));
        }
        
        return root;
    }
    
    /**
     * Build and return the result with additional metadata.
     */
    public BuildResult buildWithMetadata() {
        Group root = build();
        return new BuildResult(root, document);
    }
    
    private void applyTargetSize(Group root) {
        double sourceWidth = document.getWidth();
        double sourceHeight = document.getHeight();
        
        if (document.hasViewBox()) {
            sourceWidth = document.getViewBox().getWidth();
            sourceHeight = document.getViewBox().getHeight();
        }
        
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            // Can't scale without source dimensions
            return;
        }
        
        double scaleX = targetWidth / sourceWidth;
        double scaleY = targetHeight / sourceHeight;
        
        if (preserveAspectRatio) {
            // Use the smaller scale to fit within bounds
            double uniformScale = Math.min(scaleX, scaleY);
            root.getTransforms().add(new Affine(uniformScale, 0, 0, 0, uniformScale, 0));
        } else {
            // Stretch to fill
            root.getTransforms().add(new Affine(scaleX, 0, 0, 0, scaleY, 0));
        }
    }
    
    /**
     * Result of building the scene graph with metadata.
     */
    public static class BuildResult {
        private final Group root;
        private final SVGDocument document;
        
        public BuildResult(Group root, SVGDocument document) {
            this.root = root;
            this.document = document;
        }
        
        public Group getRoot() {
            return root;
        }
        
        public SVGDocument getDocument() {
            return document;
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

