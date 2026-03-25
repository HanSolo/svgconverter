package eu.hansolo.fx.svgconverter.model;

/**
 * Represents an SVG ellipse element.
 */
public class SVGEllipse extends SVGElement {
    private double cx;
    private double cy;
    private double rx;
    private double ry;


    public SVGEllipse() {
        this(0, 0, 0, 0);
    }
    public SVGEllipse(final double cx, final double cy, final double rx, final double ry) {
        super();
        this.cx = cx;
        this.cy = cy;
        this.rx = rx;
        this.ry = ry;
    }


    @Override
    public String getType() {
        return "ellipse";
    }

    public double getCx() { return cx; }
    public void setCx(final double cx) { this.cx = cx; }

    public double getCy() { return cy; }
    public void setCy(final double cy) { this.cy = cy; }

    public double getRx() { return rx; }
    public void setRx(final double rx) { this.rx = rx; }

    public double getRy() { return ry; }
    public void setRy(final double ry) { this.ry = ry; }
}
