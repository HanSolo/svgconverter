package eu.hansolo.fx.svgconverter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an SVG polyline element.
 */
public class SVGPolyline extends SVGElement {
    private List<Double> points;


    public SVGPolyline() {
        this(new ArrayList<>());
    }
    public SVGPolyline(List<Double> points) {
        super();
        this.points = new ArrayList<>(points);
    }


    @Override
    public String getType() {
        return "polyline";
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
