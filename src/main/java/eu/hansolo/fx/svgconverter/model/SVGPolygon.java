package eu.hansolo.fx.svgconverter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an SVG polygon element.
 */
public class SVGPolygon extends SVGElement {
    private List<Double> points;


    public SVGPolygon() {
        this(new ArrayList<>());
    }
    public SVGPolygon(final List<Double> points) {
        super();
        this.points = new ArrayList<>(points);
    }


    @Override
    public String getType() {
        return "polygon";
    }
    
    public List<Double> getPoints() {
        return points;
    }
    public void setPoints(final List<Double> points) {
        this.points = new ArrayList<>(points);
    }
    
    public void addPoint(final double x, final double y) {
        points.add(x);
        points.add(y);
    }
    
    public double[] getPointsArray() {
        return points.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
