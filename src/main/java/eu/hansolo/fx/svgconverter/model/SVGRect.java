package eu.hansolo.fx.svgconverter.model;

/**
 * Represents an SVG rectangle element.
 */
public class SVGRect extends SVGElement {
    private double x;
    private double y;
    private double width;
    private double height;
    private double rx;
    private double ry;


    public SVGRect() {
        this(0, 0, 0, 0, 0, 0);
    }
    public SVGRect(double x, double y, double width, double height) {
        this(x, y, width, height, 0, 0);
    }
    public SVGRect(final double x, final double y, final double width, final double height, final double rx, final double ry) {
        super();
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.rx     = rx;
        this.ry     = ry;
    }


    @Override
    public String getType() {
        return "rect";
    }
    
    public double getX() { return x; }
    public void setX(final double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(final double y) { this.y = y; }
    
    public double getWidth() { return width; }
    public void setWidth(final double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(final double height) { this.height = height; }
    
    public double getRx() { return rx; }
    public void setRx(final double rx) { this.rx = rx; }
    
    public double getRy() { return ry; }
    public void setRy(final double ry) { this.ry = ry; }
    
    public boolean hasRoundedCorners() {
        return rx > 0 || ry > 0;
    }
}


