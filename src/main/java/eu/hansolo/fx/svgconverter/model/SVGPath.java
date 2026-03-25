package eu.hansolo.fx.svgconverter.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents an SVG path element with path data.
 */
public class SVGPath extends SVGElement {
    private String            pathData;
    private List<PathCommand> commands;


    public SVGPath() {
        this("");
    }
    public SVGPath(final String pathData) {
        super();
        this.pathData = pathData;
        this.commands = new ArrayList<>();
    }


    @Override
    public String getType() {
        return "path";
    }

    public String getPathData() {
        return pathData;
    }
    public void setPathData(final String pathData) {
        this.pathData = pathData;
    }

    public List<PathCommand> getCommands() {
        return commands;
    }
    public void setCommands(final List<PathCommand> commands) {
        this.commands = commands;
    }
    public void addCommand(final PathCommand command) {
        this.commands.add(command);
    }

    /**
     * Base class for path commands.
     */
    public static abstract class PathCommand {
        protected boolean relative;


        public PathCommand(final boolean relative) {
            this.relative = relative;
        }


        public boolean isRelative() {
            return relative;
        }

        public abstract String getCommandType();
    }


    /**
     * MoveTo command (M/m).
     */
    public static class MoveTo extends PathCommand {
        private double x;
        private double y;


        public MoveTo(final double x, final double y, final boolean relative) {
            super(relative);
            this.x = x;
            this.y = y;
        }


        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "MoveTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f)", relative ? "m" : "M", x, y);
        }
    }


    /**
     * LineTo command (L/l).
     */
    public static class LineTo extends PathCommand {
        private double x;
        private double y;


        public LineTo(final double x, final double y, final boolean relative) {
            super(relative);
            this.x = x;
            this.y = y;
        }


        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "LineTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f)", relative ? "l" : "L", x, y);
        }
    }


    /**
     * Horizontal LineTo command (H/h).
     */
    public static class HLineTo extends PathCommand {
        private double x;


        public HLineTo(final double x, final boolean relative) {
            super(relative);
            this.x = x;
        }


        public double getX() { return x; }

        @Override
        public String getCommandType() { return "HLineTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f)", relative ? "h" : "H", x);
        }
    }


    /**
     * Vertical LineTo command (V/v).
     */
    public static class VLineTo extends PathCommand {
        private double y;


        public VLineTo(final double y, final boolean relative) {
            super(relative);
            this.y = y;
        }


        public double getY() { return y; }

        @Override
        public String getCommandType() { return "VLineTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f)", relative ? "v" : "V", y);
        }
    }


    /**
     * Cubic Bezier curve command (C/c).
     */
    public static class CubicCurveTo extends PathCommand {
        private double x1;
        private double y1;
        private double x2;
        private double y2;
        private double x;
        private double y;


        public CubicCurveTo(final double x1, final double y1, final double x2, final double y2, final double x, final double y, final boolean relative) {
            super(relative);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x  = x;
            this.y  = y;
        }


        public double getX1() { return x1; }
        public double getY1() { return y1; }
        public double getX2() { return x2; }
        public double getY2() { return y2; }
        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "CubicCurveTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)", relative ? "c" : "C", x1, y1, x2, y2, x, y);
        }
    }


    /**
     * Smooth Cubic Bezier curve command (S/s).
     */
    public static class SmoothCubicCurveTo extends PathCommand {
        private double x2;
        private double y2;
        private double x;
        private double y;


        public SmoothCubicCurveTo(final double x2, final double y2, final double x, final double y, final boolean relative) {
            super(relative);
            this.x2 = x2;
            this.y2 = y2;
            this.x  = x;
            this.y  = y;
        }


        public double getX2() { return x2; }
        public double getY2() { return y2; }
        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "SmoothCubicCurveTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f, %.2f, %.2f)", relative ? "s" : "S", x2, y2, x, y);
        }
    }


    /**
     * Quadratic Bezier curve command (Q/q).
     */
    public static class QuadraticCurveTo extends PathCommand {
        private double x1;
        private double y1;
        private double x;
        private double y;


        public QuadraticCurveTo(final double x1, final double y1, final double x, final double y, final boolean relative) {
            super(relative);
            this.x1 = x1;
            this.y1 = y1;
            this.x  = x;
            this.y  = y;
        }


        public double getX1() { return x1; }
        public double getY1() { return y1; }
        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "QuadraticCurveTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f, %.2f, %.2f)", relative ? "q" : "Q", x1, y1, x, y);
        }
    }


    /**
     * Smooth Quadratic Bezier curve command (T/t).
     */
    public static class SmoothQuadraticCurveTo extends PathCommand {
        private double x;
        private double y;


        public SmoothQuadraticCurveTo(final double x, final double y, final boolean relative) {
            super(relative);
            this.x = x;
            this.y = y;
        }


        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "SmoothQuadraticCurveTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f)", relative ? "t" : "T", x, y);
        }
    }


    /**
     * Elliptical Arc command (A/a).
     */
    public static class ArcTo extends PathCommand {
        private double  rx;
        private double  ry;
        private double  rotation;
        private double  x;
        private double  y;
        private boolean largeArc;
        private boolean sweep;


        public ArcTo(final double rx, final double ry, final double rotation, final boolean largeArc, final boolean sweep, final double x, final double y, final boolean relative) {
            super(relative);
            this.rx       = rx;
            this.ry       = ry;
            this.rotation = rotation;
            this.largeArc = largeArc;
            this.sweep    = sweep;
            this.x        = x;
            this.y        = y;
        }


        public double getRx() { return rx; }
        public double getRy() { return ry; }
        public double getRotation() { return rotation; }
        public boolean isLargeArc() { return largeArc; }
        public boolean isSweep() { return sweep; }
        public double getX() { return x; }
        public double getY() { return y; }

        @Override
        public String getCommandType() { return "ArcTo"; }

        @Override
        public String toString() {
            return String.format("%s(%.2f, %.2f, %.2f, %d, %d, %.2f, %.2f)", relative ? "a" : "A", rx, ry, rotation, largeArc ? 1 : 0, sweep ? 1 : 0, x, y);
        }
    }


    /**
     * ClosePath command (Z/z).
     */
    public static class ClosePath extends PathCommand {

        public ClosePath() {
            super(false);
        }

        @Override
        public String getCommandType() { return "ClosePath"; }

        @Override
        public String toString() {
            return "Z";
        }
    }
}
