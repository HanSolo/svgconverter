package eu.hansolo.fx.svgconverter.util;

import eu.hansolo.fx.svgconverter.model.SVGPath;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;


/**
 * Utility class for handling SVG path commands and converting them
 * to JavaFX Path elements or Canvas drawing commands.
 */
public class PathCommandHandler {

    /**
     * Convert SVG path commands to JavaFX Path elements.
     */
    public static void addCommandsToPath(final SVGPath svgPath, final Path fxPath) {
        for (SVGPath.PathCommand command : svgPath.getCommands()) {
            final PathElement element = convertToPathElement(command);
            if (element != null) { fxPath.getElements().add(element); }
        }
    }

    /**
     * Convert a single SVG path command to a JavaFX PathElement.
     */
    public static PathElement convertToPathElement(final SVGPath.PathCommand command) {
        switch (command) {
            case SVGPath.MoveTo                 cmd -> { return new MoveTo(cmd.getX(), cmd.getY()); }
            case SVGPath.LineTo                 cmd -> { return new LineTo(cmd.getX(), cmd.getY()); }
            case SVGPath.HLineTo                cmd -> { return new LineTo(cmd.getX(), 0); /* Y will be set by path context */ }
            case SVGPath.VLineTo                cmd -> { return new LineTo(0, cmd.getY()); /* X will be set by path context */ }
            case SVGPath.CubicCurveTo           cmd -> { return new CubicCurveTo(cmd.getX1(), cmd.getY1(), cmd.getX2(), cmd.getY2(), cmd.getX(), cmd.getY()); }
            case SVGPath.SmoothCubicCurveTo     cmd -> { return new CubicCurveTo(0, 0, cmd.getX2(), cmd.getY2(), cmd.getX(), cmd.getY()); }
            case SVGPath.QuadraticCurveTo       cmd -> { return new QuadCurveTo(cmd.getX1(), cmd.getY1(), cmd.getX(), cmd.getY()); }
            case SVGPath.SmoothQuadraticCurveTo cmd -> { return new QuadCurveTo(0, 0, cmd.getX(), cmd.getY()); }
            case SVGPath.ArcTo                  cmd -> {
                ArcTo arc = new ArcTo();
                arc.setRadiusX(cmd.getRx());
                arc.setRadiusY(cmd.getRy());
                arc.setXAxisRotation(cmd.getRotation());
                arc.setLargeArcFlag(cmd.isLargeArc());
                arc.setSweepFlag(cmd.isSweep());
                arc.setX(cmd.getX());
                arc.setY(cmd.getY());
                return arc;
            }
            case SVGPath.ClosePath            cmd -> { return new ClosePath(); }
            case null, default                    -> { }
        }
        return null;
    }

    /**
     * Draw SVG path commands to a Canvas GraphicsContext.
     */
    public static void drawCommandsToCanvas(final SVGPath svgPath, final GraphicsContext ctx) {
        double currentX = 0;
        double currentY = 0;
        double startX   = 0;
        double startY   = 0;

        ctx.beginPath();
        for (SVGPath.PathCommand command : svgPath.getCommands()) {
            switch (command) {
                case SVGPath.MoveTo cmd -> {
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                    startX   = currentX;
                    startY   = currentY;
                    ctx.moveTo(currentX, currentY);
                }
                case SVGPath.LineTo cmd -> {
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                    ctx.lineTo(currentX, currentY);
                }
                case SVGPath.HLineTo cmd -> {
                    currentX = cmd.getX();
                    ctx.lineTo(currentX, currentY);
                }
                case SVGPath.VLineTo cmd -> {
                    currentY = cmd.getY();
                    ctx.lineTo(currentX, currentY);
                }
                case SVGPath.CubicCurveTo cmd -> {
                    ctx.bezierCurveTo(cmd.getX1(), cmd.getY1(), cmd.getX2(), cmd.getY2(), cmd.getX(), cmd.getY());
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                }
                case SVGPath.QuadraticCurveTo cmd -> {
                    ctx.quadraticCurveTo(cmd.getX1(), cmd.getY1(), cmd.getX(), cmd.getY());
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                }
                case SVGPath.ArcTo cmd -> {
                    // Canvas doesn't have native arc support like SVG
                    // We need to approximate with bezier curves
                    drawArcToCanvas(ctx, currentX, currentY, cmd);
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                }
                case SVGPath.ClosePath closePath -> {
                    ctx.closePath();
                    currentX = startX;
                    currentY = startY;
                }
                case null, default -> {
                }
            }
        }
    }

    /**
     * Draw an SVG arc to canvas (approximation).
     */
    private static void drawArcToCanvas(final GraphicsContext ctx, final double x0, final double y0, final SVGPath.ArcTo arc) {
        SvgArcToBezier.arcTo(ctx, x0, y0, arc.getRx(), arc.getRy(), arc.getRotation(), arc.isLargeArc(), arc.isSweep(), arc.getX(), arc.getY());
    }

    /**
     * Calculate the total length of a path (approximation).
     */
    public static double calculatePathLength(final SVGPath svgPath) {
        double length   = 0;
        double currentX = 0;
        double currentY = 0;

        for (SVGPath.PathCommand command : svgPath.getCommands()) {
            switch (command) {
                case SVGPath.MoveTo cmd -> {
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                }
                case SVGPath.LineTo cmd -> {
                    length += GeometryUtils.distance(currentX, currentY, cmd.getX(), cmd.getY());
                    currentX = cmd.getX();
                    currentY = cmd.getY();
                }
                case SVGPath.HLineTo cmd -> {
                    length += Math.abs(cmd.getX() - currentX);
                    currentX = cmd.getX();
                }
                case SVGPath.VLineTo cmd -> {
                    length += Math.abs(cmd.getY() - currentY);
                    currentY = cmd.getY();
                }
                case null, default -> { }
            }
        }
        return length;
    }
}
