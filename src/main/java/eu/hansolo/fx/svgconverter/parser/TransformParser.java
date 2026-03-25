package eu.hansolo.fx.svgconverter.parser;

import eu.hansolo.fx.svgconverter.model.SVGTransform;
import eu.hansolo.fx.svgconverter.model.SVGTransform.MatrixTransform;
import eu.hansolo.fx.svgconverter.model.SVGTransform.RotateTransform;
import eu.hansolo.fx.svgconverter.model.SVGTransform.ScaleTransform;
import eu.hansolo.fx.svgconverter.model.SVGTransform.SkewXTransform;
import eu.hansolo.fx.svgconverter.model.SVGTransform.SkewYTransform;
import eu.hansolo.fx.svgconverter.model.SVGTransform.TranslateTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SVG transform attribute strings into SVGTransform objects.
 * Supports: translate, rotate, scale, skewX, skewY, matrix.
 */
public class TransformParser {
    private static final Pattern TRANSFORM_PATTERN = Pattern.compile("(translate|rotate|scale|skewX|skewY|matrix)\\s*\\(([^)]*)\\)");
    private static final Pattern NUMBER_PATTERN    = Pattern.compile("-?\\d*\\.?\\d+(?:[eE][+-]?\\d+)?");
    
    /**
     * Parse a transform attribute string into a list of SVGTransform objects.
     */
    public static List<SVGTransform> parse(final String transformString) {
        final List<SVGTransform> transforms = new ArrayList<>();
        if (transformString == null || transformString.trim().isEmpty()) { return transforms; }
        final Matcher matcher = TRANSFORM_PATTERN.matcher(transformString);
        
        while (matcher.find()) {
            final String       type    = matcher.group(1);
            final String       params  = matcher.group(2);
            final List<Double> numbers = parseNumbers(params);

            final SVGTransform transform = parseTransform(type, numbers);
            if (transform != null) { transforms.add(transform); }
        }
        return transforms;
    }
    
    private static List<Double> parseNumbers(final String params) {
        final List<Double> numbers = new ArrayList<>();
        final Matcher      matcher = NUMBER_PATTERN.matcher(params);
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        return numbers;
    }
    
    private static SVGTransform parseTransform(final String type, final List<Double> numbers) {
        try {
            return switch (type) {
                case "translate" -> parseTranslate(numbers);
                case "rotate"    -> parseRotate(numbers);
                case "scale"     -> parseScale(numbers);
                case "skewX"     -> parseSkewX(numbers);
                case "skewY"     -> parseSkewY(numbers);
                case "matrix"    -> parseMatrix(numbers);
                default          -> null;
            };
        } catch (Exception e) {
            System.err.println("Warning: Could not parse transform: " + type + " with params: " + numbers);
            return null;
        }
    }
    
    /**
     * Parse translate transform.
     * translate(tx [ty]) - if ty is not provided, it defaults to 0
     */
    private static SVGTransform parseTranslate(final List<Double> numbers) {
        if (numbers.isEmpty()) { return new TranslateTransform(0, 0); }
        final double tx = numbers.get(0);
        final double ty = numbers.size() > 1 ? numbers.get(1) : 0;
        return new TranslateTransform(tx, ty);
    }
    
    /**
     * Parse rotate transform.
     * rotate(angle [cx cy]) - if cx, cy not provided, rotation is around origin
     */
    private static SVGTransform parseRotate(final List<Double> numbers) {
        if (numbers.isEmpty()) { return new RotateTransform(0); }
        final double angle = numbers.get(0);
        if (numbers.size() >= 3) {
            final double cx = numbers.get(1);
            final double cy = numbers.get(2);
            return new RotateTransform(angle, cx, cy);
        }
        return new RotateTransform(angle);
    }
    
    /**
     * Parse scale transform.
     * scale(sx [sy]) - if sy is not provided, it defaults to sx (uniform scaling)
     */
    private static SVGTransform parseScale(final List<Double> numbers) {
        if (numbers.isEmpty()) { return new ScaleTransform(1, 1); }
        final double sx = numbers.get(0);
        final double sy = numbers.size() > 1 ? numbers.get(1) : sx;
        return new ScaleTransform(sx, sy);
    }
    
    /**
     * Parse skewX transform.
     * skewX(angle)
     */
    private static SVGTransform parseSkewX(final List<Double> numbers) {
        if (numbers.isEmpty()) { return new SkewXTransform(0); }
        return new SkewXTransform(numbers.get(0));
    }
    
    /**
     * Parse skewY transform.
     * skewY(angle)
     */
    private static SVGTransform parseSkewY(final List<Double> numbers) {
        if (numbers.isEmpty()) { return new SkewYTransform(0); }
        return new SkewYTransform(numbers.get(0));
    }
    
    /**
     * Parse matrix transform.
     * matrix(a b c d e f)
     */
    private static SVGTransform parseMatrix(final List<Double> numbers) {
        if (numbers.size() < 6) {
            System.err.println("Warning: Matrix transform requires 6 parameters, got " + numbers.size());
            return new MatrixTransform(1, 0, 0, 1, 0, 0); // Identity matrix
        }
        return new MatrixTransform(
            numbers.get(0), // a
            numbers.get(1), // b
            numbers.get(2), // c
            numbers.get(3), // d
            numbers.get(4), // e
            numbers.get(5)  // f
        );
    }
    
    /**
     * Compose multiple transforms into a single matrix transform.
     * This is useful for optimization.
     */
    public static SVGTransform compose(List<SVGTransform> transforms) {
        if (transforms.isEmpty()) { return new MatrixTransform(1, 0, 0, 1, 0, 0); }
        if (transforms.size() == 1) { return transforms.get(0); }
        
        // Start with identity matrix
        double[] result = {1, 0, 0, 1, 0, 0};
        
        // Multiply matrices in order
        for (SVGTransform transform : transforms) {
            final double[] matrix = transform.getMatrix();
            result = multiplyMatrices(result, matrix);
        }
        return new MatrixTransform(result[0], result[1], result[2], result[3], result[4], result[5]);
    }
    
    /**
     * Multiply two 2D transformation matrices.
     * Matrix format: [a, b, c, d, e, f]
     * Represents: | a c e |
     *             | b d f |
     *             | 0 0 1 |
     */
    private static double[] multiplyMatrices(final double[] m1, final double[] m2) {
        return new double[] {
            m1[0] * m2[0] + m1[2] * m2[1],           // a
            m1[1] * m2[0] + m1[3] * m2[1],           // b
            m1[0] * m2[2] + m1[2] * m2[3],           // c
            m1[1] * m2[2] + m1[3] * m2[3],           // d
            m1[0] * m2[4] + m1[2] * m2[5] + m1[4],   // e
            m1[1] * m2[4] + m1[3] * m2[5] + m1[5]    // f
        };
    }
}
