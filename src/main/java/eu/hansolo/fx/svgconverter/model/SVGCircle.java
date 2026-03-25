package eu.hansolo.fx.svgconverter.model;

/**
 * Represents an SVG circle element.
 */
public class SVGCircle extends SVGElement {
    private double cx;
    private double cy;
    private double r;


    public SVGCircle() {
        this(0, 0, 0);
    }
    public SVGCircle(final double cx, final double cy, final double r) {
        super();
        this.cx = cx;
        this.cy = cy;
        this.r = r;
    }


    @Override
    public String getType() { return "circle"; }
    
    public double getCx() { return cx; }
    public void setCx(final double cx) { this.cx = cx; }
    
    public double getCy() { return cy; }
    public void setCy(final double cy) { this.cy = cy; }
    
    public double getR() { return r; }
    public void setR(final double r) { this.r = r; }
}
