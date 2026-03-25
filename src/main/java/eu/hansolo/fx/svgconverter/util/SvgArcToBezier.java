package eu.hansolo.fx.svgconverter.util;

import javafx.scene.canvas.GraphicsContext;


/**
 * Converts SVG elliptical arc commands to JavaFX Canvas cubic Bézier curves,
 * drawn directly via GraphicsContext.bezierCurveTo().
 *
 * An SVG arc command is: A rx ry x-rotation large-arc-flag sweep-flag x y
 *
 * The algorithm follows the SVG specification's recommended approach:
 *   https://www.w3.org/TR/SVG/implnote.html#ArcConversionEndpointToCenter
 *
 * Each arc is split into at most 90-degree segments and each segment is
 * approximated by a cubic Bézier curve drawn with gc.bezierCurveTo().
 */
public class SvgArcToBezier {

    // -------------------------------------------------------------------------
    // Core conversion — draws directly onto a JavaFX GraphicsContext
    // -------------------------------------------------------------------------

    /**
     * Converts a single SVG elliptical arc and appends it to the current path
     * on the given GraphicsContext as one or more cubic Bézier segments via
     * gc.bezierCurveTo().
     *
     * The current path position must already be at (x1, y1) — call
     * gc.moveTo(x1, y1) or gc.lineTo(x1, y1) before this method if needed.
     *
     * @param ctx           The JavaFX Canvas GraphicsContext to draw into
     * @param x1            Start x (current path position)
     * @param y1            Start y (current path position)
     * @param rx            Ellipse x-axis radius
     * @param ry            Ellipse y-axis radius
     * @param xAxisRotation X-axis rotation in degrees
     * @param largeArcFlag  True → use the larger of the two possible arcs
     * @param sweepFlag     True → draw in the positive-angle direction
     * @param x2            End x coordinate
     * @param y2            End y coordinate
     */
    public static void arcTo(final GraphicsContext ctx,
                             final double x1, final double y1,
                             double rx, double ry,
                             final double xAxisRotation,
                             final boolean largeArcFlag,
                             final boolean sweepFlag,
                             final double x2, final double y2) {

        // Trivial: start == end, nothing to draw
        if (Math.abs(x1 - x2) < 1e-10 && Math.abs(y1 - y2) < 1e-10) return;

        rx = Math.abs(rx);
        ry = Math.abs(ry);

        // Degenerate: zero radii → straight line expressed as a Bézier
        if (rx < 1e-10 || ry < 1e-10) {
            ctx.bezierCurveTo(
            x1 + (x2 - x1) / 3.0, y1 + (y2 - y1) / 3.0,
            x1 + 2 * (x2 - x1) / 3.0, y1 + 2 * (y2 - y1) / 3.0,
            x2, y2);
            return;
        }

        final double phi    = Math.toRadians(xAxisRotation);
        final double cosPhi = Math.cos(phi);
        final double sinPhi = Math.sin(phi);

        // Step 1: Endpoint → rotated midpoint frame (SVG spec §B.2.2)
        final double dx  = (x1 - x2) / 2.0;
        final double dy  = (y1 - y2) / 2.0;
        final double x1p =  cosPhi * dx + sinPhi * dy;
        final double y1p = -sinPhi * dx + cosPhi * dy;

        // Step 2: Radius clamping — ensure radii are large enough (§B.2.5)
        final double x1pSq = x1p * x1p;
        final double y1pSq = y1p * y1p;
        double rxSq  = rx * rx;
        double rySq  = ry * ry;

        final double lambda = (x1pSq / rxSq) + (y1pSq / rySq);
        if (lambda > 1.0) {
            final double sqrtLambda = Math.sqrt(lambda);
            rx  *= sqrtLambda;  ry  *= sqrtLambda;
            rxSq = rx * rx;     rySq = ry * ry;
        }

        // Step 2 cont.: Compute center in rotated frame (cx', cy')
        final double num = Math.max(0.0, rxSq * rySq - rxSq * y1pSq - rySq * x1pSq);
        final double den = rxSq * y1pSq + rySq * x1pSq;
        double sq  = (den == 0.0) ? 0.0 : Math.sqrt(num / den);
        if (largeArcFlag == sweepFlag) { sq = -sq; }

        final double cxp =  sq * rx * y1p / ry;
        final double cyp = -sq * ry * x1p / rx;

        // Step 3: Map center back to the original (unrotated) frame
        final double mx = (x1 + x2) / 2.0;
        final double my = (y1 + y2) / 2.0;
        final double cx = cosPhi * cxp - sinPhi * cyp + mx;
        final double cy = sinPhi * cxp + cosPhi * cyp + my;

        // Step 4: Start angle (θ₁) and signed angular span (Δθ)
        final double theta1 = angle(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry);
        double dTheta = angle((x1p - cxp) / rx,   (y1p - cyp) / ry, (-x1p - cxp) / rx,  (-y1p - cyp) / ry);

        // Clamp Δθ to the correct sweep direction
        if (!sweepFlag && dTheta > 0) dTheta -= 2 * Math.PI;
        if ( sweepFlag && dTheta < 0) dTheta += 2 * Math.PI;

        // Step 5: Split into ≤90° segments; emit each via gc.bezierCurveTo()
        final int    segments     = Math.max(1, (int) Math.ceil(Math.abs(dTheta) / (Math.PI / 2.0)));
        final double dThetaPerSeg = dTheta / segments;

        // Track the precise start of each segment to avoid floating-point drift
        final double[] current = { x1, y1 };

        for (int i = 0 ; i < segments ; i++) {
            final double segTheta = theta1 + i * dThetaPerSeg;
            drawArcSegment(ctx, cx, cy, rx, ry, cosPhi, sinPhi, segTheta, dThetaPerSeg, current);
        }
    }

    /**
     * Draws one arc segment (≤90°) as a single gc.bezierCurveTo() call and
     * updates current[0]/current[1] to the segment's end point.
     *
     * @param current  Two-element array {startX, startY}; updated to {endX, endY}
     */
    private static void drawArcSegment(final GraphicsContext ctx,
                                       final double cx, final double cy,
                                       final double rx, final double ry,
                                       final double cosPhi, final double sinPhi,
                                       final double theta, final double dTheta,
                                       final double[] current) {

        // α: tangent-length factor — gives exact tangent match at both endpoints.
        // For θ = π/2 this reduces to the classic 4/3·(√2−1) ≈ 0.5523.
        final double tanHalf = Math.tan(dTheta / 2.0);
        final double alpha   = Math.sin(dTheta) * (Math.sqrt(4 + 3 * tanHalf * tanHalf) - 1) / 3.0;

        final double cosT1 = Math.cos(theta);
        final double sinT1 = Math.sin(theta);
        final double cosT2 = Math.cos(theta + dTheta);
        final double sinT2 = Math.sin(theta + dTheta);

        // Tangent (derivative) of the ellipse at θ, in original frame
        final double dx1 = -rx * cosPhi * sinT1 - ry * sinPhi * cosT1;
        final double dy1 = -rx * sinPhi * sinT1 + ry * cosPhi * cosT1;

        // End point on the ellipse at θ+Δθ
        final double endX = cx + cosPhi * rx * cosT2 - sinPhi * ry * sinT2;
        final double endY = cy + sinPhi * rx * cosT2 + cosPhi * ry * sinT2;

        // Tangent at θ+Δθ
        final double dx2 = -rx * cosPhi * sinT2 - ry * sinPhi * cosT2;
        final double dy2 = -rx * sinPhi * sinT2 + ry * cosPhi * cosT2;

        // Bézier control points
        final double cp1x = current[0] + alpha * dx1;
        final double cp1y = current[1] + alpha * dy1;
        final double cp2x = endX       - alpha * dx2;
        final double cp2y = endY       - alpha * dy2;

        // Emit the cubic Bézier segment to the JavaFX Canvas
        ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, endX, endY);

        // Advance the current-point tracker
        current[0] = endX;
        current[1] = endY;
    }

    /**
     * Signed angle from vector (ux, uy) to vector (vx, vy), in (-π, π].
     */
    private static double angle(final double ux, final double uy, final double vx, final double vy) {
        final double dot  = ux * vx + uy * vy;
        final double lenU = Math.sqrt(ux * ux + uy * uy);
        final double lenV = Math.sqrt(vx * vx + vy * vy);
        final double cosA = Math.max(-1.0, Math.min(1.0, dot / (lenU * lenV)));
        return (ux * vy - uy * vx < 0) ? -Math.acos(cosA) : Math.acos(cosA);
    }
}
