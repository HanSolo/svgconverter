package eu.hansolo.fx.svgconverter.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents an SVG group element (g).
 * Groups can contain other elements including nested groups.
 */
public class SVGGroup extends SVGElement {
    private List<SVGElement> children;


    public SVGGroup() {
        super();
        this.children = new ArrayList<>();
    }


    @Override
    public String getType() {
        return "g";
    }

    public List<SVGElement> getChildren() {
        return children;
    }

    public void addChild(final SVGElement element) {
        this.children.add(element);
    }

    public void removeChild(final SVGElement element) {
        this.children.remove(element);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public int getChildCount() {
        return children.size();
    }
}


