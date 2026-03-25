package eu.hansolo.fx.svgconverter.converter;

import eu.hansolo.fx.svgconverter.model.SVGCircle;
import eu.hansolo.fx.svgconverter.model.SVGDocument;
import eu.hansolo.fx.svgconverter.model.SVGElement;
import eu.hansolo.fx.svgconverter.model.SVGEllipse;
import eu.hansolo.fx.svgconverter.model.SVGGradient;
import eu.hansolo.fx.svgconverter.model.SVGGroup;
import eu.hansolo.fx.svgconverter.model.SVGLine;
import eu.hansolo.fx.svgconverter.model.SVGLinearGradient;
import eu.hansolo.fx.svgconverter.model.SVGPolygon;
import eu.hansolo.fx.svgconverter.model.SVGPolyline;
import eu.hansolo.fx.svgconverter.model.SVGRadialGradient;
import eu.hansolo.fx.svgconverter.model.SVGRect;
import eu.hansolo.fx.svgconverter.model.SVGTransform;
import eu.hansolo.fx.svgconverter.parser.ColorParser;
import eu.hansolo.fx.svgconverter.util.SvgArcToBezier;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;


/**
 * Converts SVG elements to JavaFX Canvas drawing commands.
 */
public class CanvasConverter {
    private final Canvas          canvas;
    private final GraphicsContext ctx;
    private       SVGDocument     document;


    public CanvasConverter(final Canvas canvas) {
        this.canvas = canvas;
        this.ctx    = canvas.getGraphicsContext2D();
    }


    /**
     * Convert and draw an SVG document to the canvas.
     */
    public void convert(final SVGDocument document) {
        this.document = document;

        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.save();
        ctx.setStroke(Color.TRANSPARENT);
        ctx.setLineWidth(0);
        if (document.getRoot() != null) { drawElement(document.getRoot()); }
        ctx.restore();
    }

    /**
     * Draw a single SVG element to the canvas.
     */
    public void drawElement(final SVGElement element) {
        if (element == null) { return; }

        ctx.save();
        applyTransforms(element);
        applyCommonAttributes(element);
        switch (element.getType()) {
            case "g"        -> drawGroup((SVGGroup) element);
            case "rect"     -> drawRect((SVGRect) element);
            case "circle"   -> drawCircle((SVGCircle) element);
            case "ellipse"  -> drawEllipse((SVGEllipse) element);
            case "line"     -> drawLine((SVGLine) element);
            case "path"     -> drawPath((eu.hansolo.fx.svgconverter.model.SVGPath) element);
            case "polygon"  -> drawPolygon((SVGPolygon) element);
            case "polyline" -> drawPolyline((SVGPolyline) element);
            default         -> System.err.println("Warning: Unsupported element type for canvas: " + element.getType());
        }
        ctx.restore();
    }

    private void drawGroup(final SVGGroup group) {
        for (SVGElement child : group.getChildren()) {
            drawElement(child);
        }
    }

    private void drawRect(final SVGRect rect) {
        final double x      = rect.getX();
        final double y      = rect.getY();
        final double width  = rect.getWidth();
        final double height = rect.getHeight();

        if (rect.hasRoundedCorners()) {
            final double arcWidth  = rect.getRx() * 2;
            final double arcHeight = rect.getRy() * 2;
            if (hasFill())   { ctx.fillRoundRect(x, y, width, height, arcWidth, arcHeight); }
            if (hasStroke()) { ctx.strokeRoundRect(x, y, width, height, arcWidth, arcHeight); }
        } else {
            if (hasFill())   { ctx.fillRect(x, y, width, height); }
            if (hasStroke()) { ctx.strokeRect(x, y, width, height); }
        }
    }

    private void drawCircle(final SVGCircle circle) {
        final double cx = circle.getCx();
        final double cy = circle.getCy();
        final double r  = circle.getR();

        if (hasFill())   { ctx.fillOval(cx - r, cy - r, r * 2, r * 2); }
        if (hasStroke()) { ctx.strokeOval(cx - r, cy - r, r * 2, r * 2); }
    }

    private void drawEllipse(final SVGEllipse ellipse) {
        final double cx = ellipse.getCx();
        final double cy = ellipse.getCy();
        final double rx = ellipse.getRx();
        final double ry = ellipse.getRy();

        if (hasFill())   { ctx.fillOval(cx - rx, cy - ry, rx * 2, ry * 2); }
        if (hasStroke()) { ctx.strokeOval(cx - rx, cy - ry, rx * 2, ry * 2); }
    }

    private void drawLine(final SVGLine line) {
        if (hasStroke()) { ctx.strokeLine(line.getX1(), line.getY1(), line.getX2(), line.getY2()); }
    }

    private void drawPath(final eu.hansolo.fx.svgconverter.model.SVGPath svgPath) {
        double currentX      = 0;
        double currentY      = 0;
        double subpathStartX = 0;
        double subpathStartY = 0;

        ctx.beginPath();
        for (eu.hansolo.fx.svgconverter.model.SVGPath.PathCommand cmd : svgPath.getCommands()) {
            switch (cmd) {
                case eu.hansolo.fx.svgconverter.model.SVGPath.MoveTo moveTo -> {
                    currentX      = moveTo.isRelative() ? currentX + moveTo.getX() : moveTo.getX();
                    currentY      = moveTo.isRelative() ? currentY + moveTo.getY() : moveTo.getY();
                    subpathStartX = currentX;
                    subpathStartY = currentY;
                    ctx.moveTo(currentX, currentY);
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.LineTo lineTo -> {
                    currentX = lineTo.isRelative() ? currentX + lineTo.getX() : lineTo.getX();
                    currentY = lineTo.isRelative() ? currentY + lineTo.getY() : lineTo.getY();
                    ctx.lineTo(currentX, currentY);
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.HLineTo hLineTo -> {
                    currentX = hLineTo.isRelative() ? currentX + hLineTo.getX() : hLineTo.getX();
                    ctx.lineTo(currentX, currentY);
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.VLineTo vLineTo -> {
                    currentY = vLineTo.isRelative() ? currentY + vLineTo.getY() : vLineTo.getY();
                    ctx.lineTo(currentX, currentY);
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.CubicCurveTo cubic -> {
                    final double x1 = cubic.isRelative() ? currentX + cubic.getX1() : cubic.getX1();
                    final double y1 = cubic.isRelative() ? currentY + cubic.getY1() : cubic.getY1();
                    final double x2 = cubic.isRelative() ? currentX + cubic.getX2() : cubic.getX2();
                    final double y2 = cubic.isRelative() ? currentY + cubic.getY2() : cubic.getY2();
                    currentX        = cubic.isRelative() ? currentX + cubic.getX() : cubic.getX();
                    currentY        = cubic.isRelative() ? currentY + cubic.getY() : cubic.getY();
                    ctx.bezierCurveTo(x1, y1, x2, y2, currentX, currentY);
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.QuadraticCurveTo quad -> {
                    final double x1 = quad.isRelative() ? currentX + quad.getX1() : quad.getX1();
                    final double y1 = quad.isRelative() ? currentY + quad.getY1() : quad.getY1();
                    currentX        = quad.isRelative() ? currentX + quad.getX() : quad.getX();
                    currentY        = quad.isRelative() ? currentY + quad.getY() : quad.getY();
                    ctx.quadraticCurveTo(x1, y1, currentX, currentY);
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.ArcTo arc -> {
                    final double x = arc.isRelative() ? currentX + arc.getX() : arc.getX();
                    final double y = arc.isRelative() ? currentY + arc.getY() : arc.getY();
                    SvgArcToBezier.arcTo(ctx, currentX, currentY, arc.getRx(), arc.getRy(), arc.getRotation(), arc.isLargeArc(), arc.isSweep(), x, y);
                    currentX = x;
                    currentY = y;
                }
                case eu.hansolo.fx.svgconverter.model.SVGPath.ClosePath closePath -> {
                    ctx.closePath();
                    currentX = subpathStartX;
                    currentY = subpathStartY;
                }
                case null, default -> { }
            }
        }
        if (hasFill())   { ctx.fill(); }
        if (hasStroke()) { ctx.stroke(); }
    }

    private void drawPolygon(final SVGPolygon polygon) {
        final List<Double> points = polygon.getPoints();
        if (points.size() < 2) { return; }

        final double[] xPoints = new double[points.size() / 2];
        final double[] yPoints = new double[points.size() / 2];

        for (int i = 0; i < points.size() / 2; i++) {
            xPoints[i] = points.get(i * 2);
            yPoints[i] = points.get(i * 2 + 1);
        }

        if (hasFill())   { ctx.fillPolygon(xPoints, yPoints, xPoints.length); }
        if (hasStroke()) { ctx.strokePolygon(xPoints, yPoints, xPoints.length); }
    }

    private void drawPolyline(final SVGPolyline polyline) {
        final List<Double> points = polyline.getPoints();
        if (points.size() < 2) { return; }

        final double[] xPoints = new double[points.size() / 2];
        final double[] yPoints = new double[points.size() / 2];

        for (int i = 0; i < points.size() / 2; i++) {
            xPoints[i] = points.get(i * 2);
            yPoints[i] = points.get(i * 2 + 1);
        }

        if (hasStroke()) { ctx.strokePolyline(xPoints, yPoints, xPoints.length); }
    }

    private void applyCommonAttributes(SVGElement element) {
        // Fill
        if (element.hasFill()) {
            Paint fillPaint = resolvePaint(element.getFill(), element.getFillOpacity());
            ctx.setFill(fillPaint != null ? fillPaint : Color.TRANSPARENT);
        } else {
            ctx.setFill(Color.TRANSPARENT);
        }

        // Stroke
        if (element.hasStroke()) {
            final Paint strokePaint = resolvePaint(element.getStroke(), element.getStrokeOpacity());
            if (strokePaint != null) {
                ctx.setStroke(strokePaint);
                ctx.setLineWidth(element.getStrokeWidth());
            } else {
                // Stroke value exists but couldn't be resolved - treat as no stroke
                ctx.setStroke(Color.TRANSPARENT);
                ctx.setLineWidth(0);
            }

            // Stroke line cap
            switch (element.getStrokeLineCap().toLowerCase()) {
                case "round"  -> ctx.setLineCap(StrokeLineCap.ROUND);
                case "square" -> ctx.setLineCap(StrokeLineCap.SQUARE);
                default       -> ctx.setLineCap(StrokeLineCap.BUTT);
            }

            // Stroke line join
            switch (element.getStrokeLineJoin().toLowerCase()) {
                case "round" -> ctx.setLineJoin(StrokeLineJoin.ROUND);
                case "bevel" -> ctx.setLineJoin(StrokeLineJoin.BEVEL);
                default      -> ctx.setLineJoin(StrokeLineJoin.MITER);
            }

            // Stroke dash array
            if (element.hasStrokeDashArray()) {
                ctx.setLineDashes(element.getStrokeDashArray());
            } else {
                ctx.setLineDashes(null);
            }
        } else {
            // No stroke - set to transparent to prevent drawing stroke
            ctx.setStroke(Color.TRANSPARENT);
            ctx.setLineWidth(0);
        }

        // Global alpha (opacity)
        ctx.setGlobalAlpha(element.getOpacity());
    }

    private void applyTransforms(final SVGElement element) {
        if (!element.hasTransforms()) { return; }

        for (SVGTransform transform : element.getTransforms()) {
            switch (transform) {
                case SVGTransform.TranslateTransform t -> ctx.translate(t.getTx(), t.getTy());
                case SVGTransform.RotateTransform    r -> {
                    ctx.translate(r.getCx(), r.getCy());
                    ctx.rotate(r.getAngle());
                    ctx.translate(-r.getCx(), -r.getCy());
                }
                case SVGTransform.ScaleTransform     s -> ctx.scale(s.getSx(), s.getSy());
                case SVGTransform.MatrixTransform    m -> ctx.transform(new Affine(m.getA(), m.getC(), m.getE(), m.getB(), m.getD(), m.getF()));
                case SVGTransform.SkewXTransform skewX -> {
                    final double tan = Math.tan(Math.toRadians(skewX.getAngle()));
                    ctx.transform(new Affine(1, tan, 0, 0, 1, 0));
                }
                case SVGTransform.SkewYTransform skewY -> {
                    final double tan = Math.tan(Math.toRadians(skewY.getAngle()));
                    ctx.transform(new Affine(1, 0, 0, tan, 1, 0));
                }
                case null, default                     -> { }
            }
        }
    }

    private boolean hasFill() {
        return ctx.getFill() != null && ctx.getFill() != Color.TRANSPARENT;
    }

    private boolean hasStroke() {
        return ctx.getStroke() != null && ctx.getLineWidth() > 0;
    }

    /**
     * Resolve a paint value (color or gradient reference) to a JavaFX Paint object.
     *
     * @param paintValue The paint value (color string or url(#gradientId))
     * @param opacity    The opacity to apply
     *
     * @return JavaFX Paint object or null if not resolvable
     */
    private Paint resolvePaint(final String paintValue, final double opacity) {
        if (paintValue == null || paintValue.trim().isEmpty()) { return null; }

        // CIt's a gradient reference
        if (ColorParser.isGradientReference(paintValue)) {
            final String gradientId = ColorParser.extractGradientId(paintValue);
            if (gradientId != null && document != null) {
                final SVGGradient gradient = document.getGradient(gradientId);
                if (gradient != null) { return convertGradientToPaint(gradient, opacity); }
            }
            // Gradient reference but not found - return transparent
            return Color.TRANSPARENT;
        }

        // It's a color
        final Color color = ColorParser.parse(paintValue);
        if (color != null) { return color.deriveColor(0, 1, 1, opacity); }

        return null;
    }

    /**
     * Convert an SVGGradient to a JavaFX Paint object.
     *
     * @param gradient The SVG gradient
     * @param opacity  The opacity to apply to gradient stops
     *
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
