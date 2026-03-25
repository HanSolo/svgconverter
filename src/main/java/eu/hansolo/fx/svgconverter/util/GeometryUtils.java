package eu.hansolo.fx.svgconverter.util;

import javafx.geometry.Point2D;

/**
 * Utility class for geometry calculations used in SVG conversion.
 */
public class GeometryUtils {
    
    /**
     * Calculate the distance between two points.
     */
    public static double distance(final double x1, final double y1, final double x2, final double y2) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate the distance between two points.
     */
    public static double distance(final Point2D p1, final Point2D p2) {
        return distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    /**
     * Calculate the angle in radians between two points.
     */
    public static double angle(final double x1, final double y1, final double x2, final double y2) {
        return Math.atan2(y2 - y1, x2 - x1);
    }
    
    /**
     * Calculate the angle in radians between two points.
     */
    public static double angle(final Point2D p1, final Point2D p2) {
        return angle(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    /**
     * Convert degrees to radians.
     */
    public static double toRadians(final double degrees) {
        return Math.toRadians(degrees);
    }
    
    /**
     * Convert radians to degrees.
     */
    public static double toDegrees(final double radians) {
        return Math.toDegrees(radians);
    }
    
    /**
     * Clamp a value between min and max.
     */
    public static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Linear interpolation between two values.
     */
    public static double lerp(final double start, final double end, final double t) {
        return start + (end - start) * t;
    }
    
    /**
     * Calculate the midpoint between two points.
     */
    public static Point2D midpoint(final double x1, final double y1, final double x2, final double y2) {
        return new Point2D((x1 + x2) / 2.0, (y1 + y2) / 2.0);
    }
    
    /**
     * Calculate the midpoint between two points.
     */
    public static Point2D midpoint(final Point2D p1, final Point2D p2) {
        return midpoint(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    /**
     * Rotate a point around the origin.
     */
    public static Point2D rotate(final double x, final double y, final double angle) {
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        return new Point2D(x * cos - y * sin, x * sin + y * cos);
    }
    
    /**
     * Rotate a point around another point.
     */
    public static Point2D rotate(final double x, final double y, final double cx, final double cy, final double angle) {
        // Translate to origin
        final double tx = x - cx;
        final double ty = y - cy;
        
        // Rotate
        final Point2D rotated = rotate(tx, ty, angle);
        
        // Translate back
        return new Point2D(rotated.getX() + cx, rotated.getY() + cy);
    }
    
    /**
     * Calculate a point on a cubic Bezier curve.
     * @param t Parameter from 0 to 1
     */
    public static Point2D cubicBezier(final double t, final double x0, final double y0, final double x1, final double y1, final double x2, final double y2, final double x3, final double y3) {
        final double t2  = t * t;
        final double t3  = t2 * t;
        final double mt  = 1 - t;
        final double mt2 = mt * mt;
        final double mt3 = mt2 * mt;

        final double x = mt3 * x0 + 3 * mt2 * t * x1 + 3 * mt * t2 * x2 + t3 * x3;
        final double y = mt3 * y0 + 3 * mt2 * t * y1 + 3 * mt * t2 * y2 + t3 * y3;
        
        return new Point2D(x, y);
    }
    
    /**
     * Calculate a point on a quadratic Bezier curve.
     * @param t Parameter from 0 to 1
     */
    public static Point2D quadraticBezier(final double t, final double x0, final double y0, final double x1, final double y1, final double x2, final double y2) {
        final double t2  = t * t;
        final double mt  = 1 - t;
        final double mt2 = mt * mt;

        final double x = mt2 * x0 + 2 * mt * t * x1 + t2 * x2;
        final double y = mt2 * y0 + 2 * mt * t * y1 + t2 * y2;
        
        return new Point2D(x, y);
    }
    
    /**
     * Calculate the bounding box of a set of points.
     */
    public static double[] boundingBox(final double... coords) {
        if (coords.length < 2) { return new double[]{0, 0, 0, 0}; }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (int i = 0; i < coords.length; i += 2) {
            double x = coords[i];
            double y = coords[i + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        
        return new double[]{minX, minY, maxX - minX, maxY - minY};
    }
}

