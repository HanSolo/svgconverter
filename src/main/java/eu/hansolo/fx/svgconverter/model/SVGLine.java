package eu.hansolo.fx.svgconverter.model;

/**
 * Represents an SVG line element.
 */
public class SVGLine extends SVGElement {
    private double x1;
    private double y1;
    private double x2;
    private double y2;


    public SVGLine() {
        this(0, 0, 0, 0);
    }
    public SVGLine(double x1, double y1, double x2, double y2) {
        super();
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }


    @Override
    public String getType() {
        return "line";
    }

    public double getX1() { return x1; }
    public void setX1(final double x1) { this.x1 = x1; }

    public double getY1() { return y1; }
    public void setY1(final double y1) { this.y1 = y1; }

    public double getX2() { return x2; }
    public void setX2(final double x2) { this.x2 = x2; }

    public double getY2() { return y2; }
    public void setY2(final double y2) { this.y2 = y2; }
}
