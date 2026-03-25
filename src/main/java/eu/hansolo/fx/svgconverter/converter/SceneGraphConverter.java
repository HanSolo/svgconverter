package eu.hansolo.fx.svgconverter.converter;

import eu.hansolo.fx.svgconverter.model.*;
import eu.hansolo.fx.svgconverter.parser.ColorParser;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;

/**
 * Converts SVG elements to JavaFX SceneGraph nodes.
 */
public class SceneGraphConverter {
    private SVGDocument document;
    
    /**
     * Convert an SVG document to a JavaFX Group.
     */
    public Group convert(final SVGDocument document) {
        this.document = document;
        final Group root = new Group();
        
        if (document.getRoot() != null) {
            final Node node = convertElement(document.getRoot());
            if (node != null) { root.getChildren().add(node); }
        }
        return root;
    }
    
    /**
     * Convert a single SVG element to a JavaFX Node.
     */
    public Node convertElement(final SVGElement element) {
        if (element == null) { return null; }
        Node node = null;
        switch (element.getType()) {
            case "g"        -> node = convertGroup((SVGGroup) element);
            case "rect"     -> node = convertRect((SVGRect) element);
            case "circle"   -> node = convertCircle((SVGCircle) element);
            case "ellipse"  -> node = convertEllipse((SVGEllipse) element);
            case "line"     -> node = convertLine((SVGLine) element);
            case "path"     -> node = convertPath((eu.hansolo.fx.svgconverter.model.SVGPath) element);
            case "polygon"  -> node = convertPolygon((SVGPolygon) element);
            case "polyline" -> node = convertPolyline((SVGPolyline) element);
            default         -> System.err.println("Warning: Unsupported element type: " + element.getType());
        }
        if (node != null) { applyCommonAttributes(node, element); }
        return node;
    }
    
    private Group convertGroup(final SVGGroup group) {
        final Group fxGroup = new Group();
        
        for (SVGElement child : group.getChildren()) {
            final Node childNode = convertElement(child);
            if (childNode != null) { fxGroup.getChildren().add(childNode); }
        }
        return fxGroup;
    }
    
    private Rectangle convertRect(final SVGRect rect) {
        final Rectangle fxRect = new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        if (rect.hasRoundedCorners()) {
            fxRect.setArcWidth(rect.getRx() * 2);
            fxRect.setArcHeight(rect.getRy() * 2);
        }
        return fxRect;
    }
    
    private Circle convertCircle(final SVGCircle circle) {
        return new Circle(circle.getCx(), circle.getCy(), circle.getR());
    }
    
    private Ellipse convertEllipse(final SVGEllipse ellipse) {
        return new Ellipse(ellipse.getCx(), ellipse.getCy(), ellipse.getRx(), ellipse.getRy());
    }
    
    private Line convertLine(final SVGLine line) {
        return new Line(line.getX1(), line.getY1(), line.getX2(), line.getY2());
    }
    
    private Path convertPath(final eu.hansolo.fx.svgconverter.model.SVGPath svgPath) {
        Path   fxPath       = new Path();
        double currentX     = 0;
        double currentY     = 0;
        double lastControlX = 0;
        double lastControlY = 0;
        
        for (eu.hansolo.fx.svgconverter.model.SVGPath.PathCommand cmd : svgPath.getCommands()) {
            switch (cmd) {
                case eu.hansolo.fx.svgconverter.model.SVGPath.MoveTo         moveTo -> {
                    final double x = moveTo.isRelative() ? currentX + moveTo.getX() : moveTo.getX();
                    final double y = moveTo.isRelative() ? currentY + moveTo.getY() : moveTo.getY();
                    fxPath.getElements().add(new MoveTo(x, y));
                    currentX = x;
                    currentY = y;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.LineTo         lineTo -> {
                    final double x = lineTo.isRelative() ? currentX + lineTo.getX() : lineTo.getX();
                    final double y = lineTo.isRelative() ? currentY + lineTo.getY() : lineTo.getY();
                    fxPath.getElements().add(new LineTo(x, y));
                    currentX = x;
                    currentY = y;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.HLineTo       hLineTo -> {
                    final double x = hLineTo.isRelative() ? currentX + hLineTo.getX() : hLineTo.getX();
                    fxPath.getElements().add(new LineTo(x, currentY));
                    currentX = x;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.VLineTo       vLineTo -> {
                    final double y = vLineTo.isRelative() ? currentY + vLineTo.getY() : vLineTo.getY();
                    fxPath.getElements().add(new LineTo(currentX, y));
                    currentY = y;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.CubicCurveTo    cubic -> {
                    final double x1 = cubic.isRelative() ? currentX + cubic.getX1() : cubic.getX1();
                    final double y1 = cubic.isRelative() ? currentY + cubic.getY1() : cubic.getY1();
                    final double x2 = cubic.isRelative() ? currentX + cubic.getX2() : cubic.getX2();
                    final double y2 = cubic.isRelative() ? currentY + cubic.getY2() : cubic.getY2();
                    final double x  = cubic.isRelative() ? currentX + cubic.getX() : cubic.getX();
                    final double y  = cubic.isRelative() ? currentY + cubic.getY() : cubic.getY();
                    fxPath.getElements().add(new CubicCurveTo(x1, y1, x2, y2, x, y));
                    currentX     = x;
                    currentY     = y;
                    lastControlX = x2;
                    lastControlY = y2;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.QuadraticCurveTo quad -> {
                    final double x1 = quad.isRelative() ? currentX + quad.getX1() : quad.getX1();
                    final double y1 = quad.isRelative() ? currentY + quad.getY1() : quad.getY1();
                    final double x  = quad.isRelative() ? currentX + quad.getX() : quad.getX();
                    final double y  = quad.isRelative() ? currentY + quad.getY() : quad.getY();
                    fxPath.getElements().add(new QuadCurveTo(x1, y1, x, y));
                    currentX     = x;
                    currentY     = y;
                    lastControlX = x1;
                    lastControlY = y1;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.ArcTo            arc -> {
                    final double x     = arc.isRelative() ? currentX + arc.getX() : arc.getX();
                    final double y     = arc.isRelative() ? currentY + arc.getY() : arc.getY();
                    ArcTo  fxArc = new ArcTo(arc.getRx(), arc.getRy(), arc.getRotation(), x, y, arc.isLargeArc(), arc.isSweep());
                    fxPath.getElements().add(fxArc);
                    currentX = x;
                    currentY = y;

                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.ClosePath  closePath -> fxPath.getElements().add(new ClosePath());
                case null, default                                                 -> { }
            }
        }
        return fxPath;
    }
    
    private Polygon convertPolygon(final SVGPolygon polygon) {
        final Polygon fxPolygon = new Polygon();
        fxPolygon.getPoints().addAll(polygon.getPoints());
        return fxPolygon;
    }
    
    private Polyline convertPolyline(final SVGPolyline polyline) {
        final Polyline fxPolyline = new Polyline();
        fxPolyline.getPoints().addAll(polyline.getPoints());
        return fxPolyline;
    }
    
    private void applyCommonAttributes(final Node node, final SVGElement element) {
        if (node instanceof Shape) {
            final Shape shape = (Shape) node;
            
            // Fill
            if (element.hasFill()) {
                final Paint fillPaint = resolvePaint(element.getFill(), element.getFillOpacity());
                shape.setFill(fillPaint);
            } else {
                shape.setFill(null);
            }
            
            // Stroke
            if (element.hasStroke()) {
                final Paint strokePaint = resolvePaint(element.getStroke(), element.getStrokeOpacity());
                if (strokePaint != null) { shape.setStroke(strokePaint); }
                shape.setStrokeWidth(element.getStrokeWidth());
                
                // Stroke line cap
                switch (element.getStrokeLineCap().toLowerCase()) {
                    case "round"  -> shape.setStrokeLineCap(StrokeLineCap.ROUND);
                    case "square" -> shape.setStrokeLineCap(StrokeLineCap.SQUARE);
                    default       -> shape.setStrokeLineCap(StrokeLineCap.BUTT);
                }
                
                // Stroke line join
                switch (element.getStrokeLineJoin().toLowerCase()) {
                    case "round" -> shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    case "bevel" -> shape.setStrokeLineJoin(StrokeLineJoin.BEVEL);
                    default      -> shape.setStrokeLineJoin(StrokeLineJoin.MITER);
                }
                
                // Stroke dash array
                if (element.hasStrokeDashArray()) {
                    for (double dash : element.getStrokeDashArray()) {
                        shape.getStrokeDashArray().add(dash);
                    }
                }
            } else {
                shape.setStroke(null);
            }
        }
        
        // Opacity
        node.setOpacity(element.getOpacity());
        
        // Transforms
        if (element.hasTransforms()) {
            for (SVGTransform transform : element.getTransforms()) {
                final Transform fxTransform = convertTransform(transform);
                if (fxTransform != null) { node.getTransforms().add(fxTransform); }
            }
        }
        
        // ID
        if (element.getId() != null && !element.getId().isEmpty()) {
            node.setId(element.getId());
        }
    }
    
    private Transform convertTransform(SVGTransform transform) {
        return switch (transform) {
            case SVGTransform.TranslateTransform t -> new Translate(t.getTx(), t.getTy());
            case SVGTransform.RotateTransform    r -> new Rotate(r.getAngle(), r.getCx(), r.getCy());
            case SVGTransform.ScaleTransform     s -> new Scale(s.getSx(), s.getSy());
            case SVGTransform.SkewXTransform skewX -> new Shear(Math.tan(Math.toRadians(skewX.getAngle())), 0);
            case SVGTransform.SkewYTransform skewY -> new Shear(0, Math.tan(Math.toRadians(skewY.getAngle())));
            case SVGTransform.MatrixTransform    m -> new Affine(m.getA(), m.getC(), m.getE(), m.getB(), m.getD(), m.getF());
            case null, default                     -> null;
        };

    }
    
    /**
     * Resolve a paint value (color or gradient reference) to a JavaFX Paint object.
     * @param paintValue The paint value (color string or url(#gradientId))
     * @param opacity The opacity to apply
     * @return JavaFX Paint object or null if not resolvable
     */
    private Paint resolvePaint(final String paintValue, final double opacity) {
        if (paintValue == null || paintValue.trim().isEmpty()) { return null; }
        
        // Check if it's a gradient reference
        if (ColorParser.isGradientReference(paintValue)) {
            final String gradientId = ColorParser.extractGradientId(paintValue);
            if (gradientId != null && document != null) {
                final SVGGradient gradient = document.getGradient(gradientId);
                if (gradient != null) { return convertGradientToPaint(gradient, opacity); }
            }
            // Gradient reference but not found - return null (transparent)
            return null;
        }
        
        // It's a color
        final Color color = ColorParser.parse(paintValue);
        if (color != null) { return color.deriveColor(0, 1, 1, opacity); }
        return null;
    }
    
    /**
     * Convert an SVGGradient to a JavaFX Paint object.
     * @param gradient The SVG gradient
     * @param opacity The opacity to apply to gradient stops
     * @return JavaFX LinearGradient or RadialGradient
     */
    private Paint convertGradientToPaint(final SVGGradient gradient, final double opacity) {
        return switch (gradient) {
            case SVGLinearGradient svgLinearGradient -> convertLinearGradient(svgLinearGradient, opacity);
            case SVGRadialGradient svgRadialGradient -> convertRadialGradient(svgRadialGradient, opacity);
            case null, default                       -> null;
        };
    }
    
    /**
     * Convert SVGLinearGradient to JavaFX LinearGradient.
     */
    private LinearGradient convertLinearGradient(final SVGLinearGradient svgGradient, final double opacity) {
        final List<Stop> stops = new ArrayList<>();
        for (SVGGradient.GradientStop svgStop : svgGradient.getStops()) {
            final Color  color       = svgStop.getColor();
            final double stopOpacity = svgStop.getOpacity() * opacity;
            final Color  stopColor   = color.deriveColor(0, 1, 1, stopOpacity);
            stops.add(new Stop(svgStop.getOffset(), stopColor));
        }

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
    private RadialGradient convertRadialGradient(final SVGRadialGradient svgGradient, final double opacity) {
        final List<Stop> stops = new ArrayList<>();
        for (SVGGradient.GradientStop svgStop : svgGradient.getStops()) {
            final Color  color       = svgStop.getColor();
            final double stopOpacity = svgStop.getOpacity() * opacity;
            final Color  stopColor   = color.deriveColor(0, 1, 1, stopOpacity);
            stops.add(new Stop(svgStop.getOffset(), stopColor));
        }

        if (stops.isEmpty()) {
            stops.add(new Stop(0, Color.TRANSPARENT));
            stops.add(new Stop(1, Color.TRANSPARENT));
        }

        final boolean     proportional  = svgGradient.isProportional();
        final CycleMethod cycleMethod   = CycleMethod.NO_CYCLE;
        final double      focusAngle    = svgGradient.getFocusAngle();
        final double      focusDistance = svgGradient.getFocusDistance();
        
        return new RadialGradient(focusAngle, focusDistance, svgGradient.getCx(), svgGradient.getCy(), svgGradient.getR(), proportional, cycleMethod, stops);
    }
}
