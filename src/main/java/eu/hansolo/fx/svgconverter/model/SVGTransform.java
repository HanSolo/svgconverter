package eu.hansolo.fx.svgconverter.model;

import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Represents an SVG transform (translate, rotate, scale, skew, matrix).
 */
public abstract class SVGTransform {
    /**
     * Convert this SVG transform to a JavaFX Transform.
     */
    public abstract Transform toJavaFX();
    
    /**
     * Get the transform matrix values [a, b, c, d, e, f].
     */
    public abstract double[] getMatrix();
    
    // Translate transform
    public static class TranslateTransform extends SVGTransform {
        private final double tx;
        private final double ty;


        public TranslateTransform(final double tx, final double ty) {
            this.tx = tx;
            this.ty = ty;
        }


        public double getTx() { return tx; }
        public double getTy() { return ty; }
        
        @Override
        public Transform toJavaFX() {
            return new Translate(tx, ty);
        }
        
        @Override
        public double[] getMatrix() {
            return new double[] { 1, 0, 0, 1, tx, ty };
        }
        
        @Override
        public String toString() {
            return String.format("translate(%.2f, %.2f)", tx, ty);
        }
    }
    
    // Rotate transform
    public static class RotateTransform extends SVGTransform {
        private final double angle;
        private final double cx;
        private final double cy;


        public RotateTransform(final double angle) {
            this(angle, 0, 0);
        }
        public RotateTransform(final double angle, final double cx, final double cy) {
            this.angle = angle;
            this.cx    = cx;
            this.cy    = cy;
        }


        public double getAngle() { return angle; }
        public double getCx() { return cx; }
        public double getCy() { return cy; }
        
        @Override
        public Transform toJavaFX() {
            return new Rotate(angle, cx, cy);
        }
        
        @Override
        public double[] getMatrix() {
            final double rad = Math.toRadians(angle);
            final double cos = Math.cos(rad);
            final double sin = Math.sin(rad);
            final double tx  = cx - cx * cos + cy * sin;
            final double ty  = cy - cx * sin - cy * cos;
            return new double[] { cos, sin, -sin, cos, tx, ty };
        }
        
        @Override
        public String toString() {
            if (cx == 0 && cy == 0) { return String.format("rotate(%.2f)", angle); }
            return String.format("rotate(%.2f, %.2f, %.2f)", angle, cx, cy);
        }
    }
    
    // Scale transform
    public static class ScaleTransform extends SVGTransform {
        private final double sx;
        private final double sy;


        public ScaleTransform(final double s) {
            this(s, s);
        }
        public ScaleTransform(final double sx, final double sy) {
            this.sx = sx;
            this.sy = sy;
        }


        public double getSx() { return sx; }
        public double getSy() { return sy; }
        
        @Override
        public Transform toJavaFX() {
            return new Scale(sx, sy);
        }
        
        @Override
        public double[] getMatrix() {
            return new double[] { sx, 0, 0, sy, 0, 0 };
        }
        
        @Override
        public String toString() {
            if (sx == sy) { return String.format("scale(%.2f)", sx); }
            return String.format("scale(%.2f, %.2f)", sx, sy);
        }
    }
    
    // SkewX transform
    public static class SkewXTransform extends SVGTransform {
        private final double angle;


        public SkewXTransform(final double angle) {
            this.angle = angle;
        }


        public double getAngle() { return angle; }
        
        @Override
        public Transform toJavaFX() {
            final double tan = Math.tan(Math.toRadians(angle));
            return new Affine(1, tan, 0, 0, 1, 0);
        }
        
        @Override
        public double[] getMatrix() {
            final double tan = Math.tan(Math.toRadians(angle));
            return new double[] { 1, 0, tan, 1, 0, 0 };
        }
        
        @Override
        public String toString() {
            return String.format("skewX(%.2f)", angle);
        }
    }
    
    // SkewY transform
    public static class SkewYTransform extends SVGTransform {
        private final double angle;


        public SkewYTransform(final double angle) {
            this.angle = angle;
        }


        public double getAngle() { return angle; }
        
        @Override
        public Transform toJavaFX() {
            final double tan = Math.tan(Math.toRadians(angle));
            return new Affine(1, 0, 0, tan, 1, 0);
        }
        
        @Override
        public double[] getMatrix() {
            final double tan = Math.tan(Math.toRadians(angle));
            return new double[] { 1, tan, 0, 1, 0, 0 };
        }
        
        @Override
        public String toString() {
            return String.format("skewY(%.2f)", angle);
        }
    }
    
    // Matrix transform
    public static class MatrixTransform extends SVGTransform {
        private final double a;
        private final double b;
        private final double c;
        private final double d;
        private final double e;
        private final double f;


        public MatrixTransform(final double a, final double b, final double c, final double d, final double e, final double f) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
        }


        public double getA() { return a; }
        public double getB() { return b; }
        public double getC() { return c; }
        public double getD() { return d; }
        public double getE() { return e; }
        public double getF() { return f; }
        
        @Override
        public Transform toJavaFX() {
            return new Affine(a, c, e, b, d, f);
        }
        
        @Override
        public double[] getMatrix() {
            return new double[] { a, b, c, d, e, f };
        }
        
        @Override
        public String toString() {
            return String.format("matrix(%.2f, %.2f, %.2f, %.2f, %.2f, %.2f)", a, b, c, d, e, f);
        }
    }
}
