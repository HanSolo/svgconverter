package eu.hansolo.fx.svgconverter.model;

/**
 * Represents an SVG radial gradient.
 */
public class SVGRadialGradient extends SVGGradient {
    private double cx;
    private double cy;
    private double r;
    private double fx;
    private double fy;


    public SVGRadialGradient() {
        this(0.5, 0.5, 0.5);
    }
    public SVGRadialGradient(final double cx, final double cy, final double r) {
        super();
        this.cx = cx;
        this.cy = cy;
        this.r  = r;
        this.fx = cx;
        this.fy = cy;
    }


    public double getCx() { return cx; }
    public void setCx(final double cx) { this.cx = cx; }
    
    public double getCy() { return cy; }
    public void setCy(final double cy) { this.cy = cy; }
    
    public double getR() { return r; }
    public void setR(final double r) { this.r = r; }
    
    public double getFx() { return fx; }
    public void setFx(final double fx) { this.fx = fx; }
    
    public double getFy() { return fy; }
    public void setFy(final double fy) { this.fy = fy; }
    
    public double getFocusAngle() {
        return Math.toDegrees(Math.atan2(fy - cy, fx - cx));
    }
    
    public double getFocusDistance() {
        final double dx = fx - cx;
        final double dy = fy - cy;
        return Math.sqrt(dx * dx + dy * dy) / r;
    }
}
