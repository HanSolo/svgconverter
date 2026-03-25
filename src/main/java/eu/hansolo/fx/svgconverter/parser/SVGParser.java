package eu.hansolo.fx.svgconverter.parser;

import eu.hansolo.fx.svgconverter.model.SVGCircle;
import eu.hansolo.fx.svgconverter.model.SVGDocument;
import eu.hansolo.fx.svgconverter.model.SVGDocument.ViewBox;
import eu.hansolo.fx.svgconverter.model.SVGElement;
import eu.hansolo.fx.svgconverter.model.SVGEllipse;
import eu.hansolo.fx.svgconverter.model.SVGGradient;
import eu.hansolo.fx.svgconverter.model.SVGGradient.GradientStop;
import eu.hansolo.fx.svgconverter.model.SVGGroup;
import eu.hansolo.fx.svgconverter.model.SVGLine;
import eu.hansolo.fx.svgconverter.model.SVGLinearGradient;
import eu.hansolo.fx.svgconverter.model.SVGPath;
import eu.hansolo.fx.svgconverter.model.SVGPolygon;
import eu.hansolo.fx.svgconverter.model.SVGPolyline;
import eu.hansolo.fx.svgconverter.model.SVGRadialGradient;
import eu.hansolo.fx.svgconverter.model.SVGRect;
import eu.hansolo.fx.svgconverter.model.SVGTransform;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main parser for SVG files.
 * Parses SVG XML and creates SVGDocument with all elements.
 */
public class SVGParser {
    private final AttributeParser                  attributeParser;
    private final PathDataParser                   pathDataParser;
    private final Map<String, SVGGradient>         gradients;
    private final Map<String, Map<String, String>> cssClasses;


    public SVGParser() {
        this.attributeParser = AttributeParser.INSTANCE;
        this.pathDataParser  = new PathDataParser();
        this.gradients       = new HashMap<>();
        this.cssClasses      = new HashMap<>();
    }

    /**
     * Parse SVG from file path.
     */
    public SVGDocument parse(final String filePath) throws Exception { return parse(new File(filePath)); }

    /**
     * Parse SVG from File object.
     */
    public SVGDocument parse(final File file) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder        builder = factory.newDocumentBuilder();
        final Document               doc     = builder.parse(file);
        return parseDocument(doc);
    }

    /**
     * Parse SVG from InputStream.
     */
    public SVGDocument parse(final InputStream inputStream) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder        builder = factory.newDocumentBuilder();
        final Document               doc     = builder.parse(inputStream);
        return parseDocument(doc);
    }

    /**
     * Parse SVG from XML string.
     */
    public SVGDocument parseString(final String svgContent) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder        builder = factory.newDocumentBuilder();
        final Document               doc     = builder.parse(new java.io.ByteArrayInputStream(svgContent.getBytes("UTF-8")));
        return parseDocument(doc);
    }

    private SVGDocument parseDocument(final Document doc) {
        final SVGDocument svgDoc = new SVGDocument();
        gradients.clear();
        cssClasses.clear();

        final Element root = doc.getDocumentElement();
        if (!"svg".equals(root.getLocalName())) { throw new IllegalArgumentException("Root element must be <svg>"); }

        // Parse SVG attributes
        parseSVGAttributes(svgDoc, root);

        // Parse style elements first (for CSS classes)
        final NodeList styleList = root.getElementsByTagName("style");
        for (int i = 0; i < styleList.getLength(); i++) {
            parseStyleElement((Element) styleList.item(i));
        }

        // Parse defs section (for gradients, etc.)
        final NodeList defsList = root.getElementsByTagName("defs");
        for (int i = 0; i < defsList.getLength(); i++) {
            parseDefs((Element) defsList.item(i));
        }

        // Parse child elements
        final SVGGroup rootGroup = new SVGGroup();
        parseChildren(root, rootGroup);
        svgDoc.setRoot(rootGroup);

        // Store gradients and CSS classes in the document
        svgDoc.setGradients(new HashMap<>(gradients));
        svgDoc.setCssClasses(new HashMap<>(cssClasses));

        return svgDoc;
    }

    private void parseSVGAttributes(final SVGDocument svgDoc, final Element svgElement) {
        // Parse width and height
        final String width  = svgElement.getAttribute("width");
        final String height = svgElement.getAttribute("height");

        if (!width.isEmpty())  { svgDoc.setWidth(parseLength(width)); }
        if (!height.isEmpty()) { svgDoc.setHeight(parseLength(height)); }

        // Parse viewBox
        final String viewBox = svgElement.getAttribute("viewBox");
        if (!viewBox.isEmpty()) {
            double[] vb = attributeParser.parseViewBox(viewBox);
            if (vb != null) { svgDoc.setViewBox(new ViewBox(vb[0], vb[1], vb[2], vb[3])); }
        }

        // Parse title and description
        final NodeList titleList = svgElement.getElementsByTagName("title");
        if (titleList.getLength() > 0) { svgDoc.setTitle(titleList.item(0).getTextContent()); }

        final NodeList descList = svgElement.getElementsByTagName("desc");
        if (descList.getLength() > 0) { svgDoc.setDescription(descList.item(0).getTextContent()); }
    }

    private void parseDefs(final Element defsElement) {
        final NodeList children = defsElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element element = (Element) node;
                final String  tagName = element.getLocalName();

                if ("linearGradient".equals(tagName)) {
                    parseLinearGradient(element);
                } else if ("radialGradient".equals(tagName)) {
                    parseRadialGradient(element);
                }
            }
        }
    }

    private void parseChildren(final Element parent, final SVGGroup group) {
        final NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element    element    = (Element) node;
                final SVGElement svgElement = parseElement(element);
                if (svgElement != null) { group.addChild(svgElement); }
            }
        }
    }

    private SVGElement parseElement(final Element element) {
        final String tagName = element.getLocalName();

        final SVGElement svgElement = switch (tagName) {
            case "g"                     -> parseGroup(element);
            case "path"                  -> parsePath(element);
            case "rect"                  -> parseRect(element);
            case "circle"                -> parseCircle(element);
            case "ellipse"               -> parseEllipse(element);
            case "line"                  -> parseLine(element);
            case "polygon"               -> parsePolygon(element);
            case "polyline"              -> parsePolyline(element);
            case "title", "desc", "defs" -> null; // Skip metadata elements
            default                      -> {
                System.err.println("Warning: Unsupported element: " + tagName);
                yield null;
            }
        };

        if (svgElement != null) { applyAttributes(svgElement, element); }
        return svgElement;
    }

    private SVGGroup parseGroup(final Element element) {
        final SVGGroup group = new SVGGroup();
        parseChildren(element, group);
        return group;
    }

    private SVGPath parsePath(final Element element) {
        final String d = element.getAttribute("d");
        if (d.isEmpty()) { return null; }

        final SVGPath                   path     = new SVGPath();
        final List<SVGPath.PathCommand> commands = pathDataParser.parse(d);
        commands.forEach(command -> path.addCommand(command));
        return path;
    }

    private SVGRect parseRect(final Element element) {
        final double x      = parseLength(element.getAttribute("x"));
        final double y      = parseLength(element.getAttribute("y"));
        final double width  = parseLength(element.getAttribute("width"));
        final double height = parseLength(element.getAttribute("height"));
        final double rx     = parseLength(element.getAttribute("rx"));
        final double ry     = parseLength(element.getAttribute("ry"));

        final SVGRect rect = new SVGRect(x, y, width, height);
        rect.setRx(rx);
        rect.setRy(ry);
        return rect;
    }

    private SVGCircle parseCircle(final Element element) {
        final double cx = parseLength(element.getAttribute("cx"));
        final double cy = parseLength(element.getAttribute("cy"));
        final double r  = parseLength(element.getAttribute("r"));
        return new SVGCircle(cx, cy, r);
    }

    private SVGEllipse parseEllipse(final Element element) {
        final double cx = parseLength(element.getAttribute("cx"));
        final double cy = parseLength(element.getAttribute("cy"));
        final double rx = parseLength(element.getAttribute("rx"));
        final double ry = parseLength(element.getAttribute("ry"));
        return new SVGEllipse(cx, cy, rx, ry);
    }

    private SVGLine parseLine(final Element element) {
        final double x1 = parseLength(element.getAttribute("x1"));
        final double y1 = parseLength(element.getAttribute("y1"));
        final double x2 = parseLength(element.getAttribute("x2"));
        final double y2 = parseLength(element.getAttribute("y2"));
        return new SVGLine(x1, y1, x2, y2);
    }

    private SVGPolygon parsePolygon(final Element element) {
        final String points = element.getAttribute("points");
        if (points.isEmpty()) { return null; }

        final double[]     pointArray = attributeParser.parsePoints(points);
        final List<Double> pointList  = new ArrayList<>();
        for (double point : pointArray) { pointList.add(point); }
        return new SVGPolygon(pointList);
    }

    private SVGPolyline parsePolyline(final Element element) {
        final String points = element.getAttribute("points");
        if (points.isEmpty()) { return null; }

        final double[]     pointArray = attributeParser.parsePoints(points);
        final List<Double> pointList  = new ArrayList<>();
        for (double point : pointArray) { pointList.add(point); }
        return new SVGPolyline(pointList);
    }

    private void parseLinearGradient(final Element element) {
        final String id = element.getAttribute("id");
        if (id.isEmpty()) { return; }

        // Parse gradient coordinates (default values for objectBoundingBox)
        final double x1 = parsePercentage(element.getAttribute("x1"), 0.0);
        final double y1 = parsePercentage(element.getAttribute("y1"), 0.0);
        final double x2 = parsePercentage(element.getAttribute("x2"), 1.0);
        final double y2 = parsePercentage(element.getAttribute("y2"), 0.0);

        final SVGLinearGradient gradient = new SVGLinearGradient(x1, y1, x2, y2);
        gradient.setId(id);

        // Parse gradientUnits attribute
        final String gradientUnits = element.getAttribute("gradientUnits");
        if (!gradientUnits.isEmpty()) { gradient.setGradientUnits(gradientUnits); }

        // Parse gradientTransform attribute
        final String transformAttr = element.getAttribute("gradientTransform");
        if (!transformAttr.isEmpty()) {
            final List<SVGTransform> transforms = TransformParser.parse(transformAttr);
            if (!transforms.isEmpty()) {
                // Compose all transforms into a single transform
                gradient.setGradientTransform(TransformParser.compose(transforms));
            }
        }

        // Parse gradient stops
        parseGradientStops(element, gradient);

        // Store gradient
        gradients.put(id, gradient);
    }

    private void parseRadialGradient(final Element element) {
        final String id = element.getAttribute("id");
        if (id.isEmpty()) { return; }

        // Parse gradient coordinates (default values for objectBoundingBox)
        final double cx = parsePercentage(element.getAttribute("cx"), 0.5);
        final double cy = parsePercentage(element.getAttribute("cy"), 0.5);
        final double r  = parsePercentage(element.getAttribute("r"), 0.5);
        final double fx = parsePercentage(element.getAttribute("fx"), cx);
        final double fy = parsePercentage(element.getAttribute("fy"), cy);

        final SVGRadialGradient gradient = new SVGRadialGradient(cx, cy, r);
        gradient.setFx(fx);
        gradient.setFy(fy);
        gradient.setId(id);

        // Parse gradientUnits attribute
        final String gradientUnits = element.getAttribute("gradientUnits");
        if (!gradientUnits.isEmpty()) { gradient.setGradientUnits(gradientUnits); }

        // Parse gradientTransform attribute
        final String transformAttr = element.getAttribute("gradientTransform");
        if (!transformAttr.isEmpty()) {
            final List<SVGTransform> transforms = TransformParser.parse(transformAttr);
            if (!transforms.isEmpty()) {
                // Compose all transforms into a single transform
                gradient.setGradientTransform(TransformParser.compose(transforms));
            }
        }

        // Parse gradient stops
        parseGradientStops(element, gradient);

        // Store gradient
        gradients.put(id, gradient);
    }

    private void parseGradientStops(final Element gradientElement, final SVGGradient gradient) {
        final NodeList stopNodes = gradientElement.getElementsByTagName("stop");

        for (int i = 0; i < stopNodes.getLength(); i++) {
            final Node node = stopNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element stopElement = (Element) node;

                // Parse offset (required attribute)
                final String offsetStr = stopElement.getAttribute("offset");
                if (offsetStr.isEmpty()) { continue; /* Skip stops without offset */ }

                final double offset = parsePercentage(offsetStr, 0.0);

                // Parse stop-color (can be in style or as attribute)
                final Color color = parseStopColor(stopElement);

                // Parse stop-opacity (can be in style or as attribute)
                final double opacity = parseStopOpacity(stopElement);

                // Create and add gradient stop
                final GradientStop stop = new GradientStop(offset, color, opacity);
                gradient.addStop(stop);
            }
        }
    }

    private Color parseStopColor(final Element stopElement) {
        // Try to get color from stop-color attribute
        String stopColor = stopElement.getAttribute("stop-color");

        // If not found, try to parse from style attribute
        if (stopColor.isEmpty()) {
            final String style = stopElement.getAttribute("style");
            if (!style.isEmpty()) { stopColor = extractStyleProperty(style, "stop-color"); }
        }

        // Parse color or use black as default
        if (!stopColor.isEmpty()) {
            final Color color = ColorParser.parse(stopColor);
            return color != null ? color : Color.BLACK;
        }
        return Color.BLACK;
    }

    private double parseStopOpacity(final Element stopElement) {
        // Try to get opacity from stop-opacity attribute
        String stopOpacity = stopElement.getAttribute("stop-opacity");

        // If not found, try to parse from style attribute
        if (stopOpacity.isEmpty()) {
            final String style = stopElement.getAttribute("style");
            if (!style.isEmpty()) { stopOpacity = extractStyleProperty(style, "stop-opacity"); }
        }

        // Parse opacity or use 1.0 as default
        if (!stopOpacity.isEmpty()) {
            try {
                return Math.max(0.0, Math.min(1.0, Double.parseDouble(stopOpacity)));
            } catch (NumberFormatException e) {
                return 1.0;
            }
        }
        return 1.0;
    }

    private String extractStyleProperty(final String style, final String property) {
        // Parse CSS-style properties from style attribute
        final String[] declarations = style.split(";");
        for (String declaration : declarations) {
            final String[] parts = declaration.split(":");
            if (parts.length == 2) {
                final String prop  = parts[0].trim();
                final String value = parts[1].trim();
                if (prop.equals(property)) { return value; }
            }
        }
        return "";
    }

    private void parseStyleElement(final Element styleElement) {
        final String cssContent = styleElement.getTextContent();
        if (cssContent == null || cssContent.trim().isEmpty()) { return; }
        parseCssRules(cssContent); // Parse CSS rules
    }

    private void parseCssRules(final String cssContent) {
        // Simple CSS parser for class selectors
        // Matches patterns like: .cls-1 { fill: #efc3ab; stroke: #000; }
        final String[] rules = cssContent.split("}");

        for (String rule : rules) {
            rule = rule.trim();
            if (rule.isEmpty()) { continue; }

            int braceIndex = rule.indexOf('{');
            if (braceIndex == -1) { continue; }

            final String selector     = rule.substring(0, braceIndex).trim();
            final String declarations = rule.substring(braceIndex + 1).trim();

            // Only handle class selectors (starting with .)
            if (selector.startsWith(".")) {
                final String              className  = selector.substring(1).trim();
                final Map<String, String> properties = parseCssDeclarations(declarations);
                cssClasses.put(className, properties);
            }
        }
    }

    private Map<String, String> parseCssDeclarations(final String declarations) {
        final Map<String, String> properties = new HashMap<>();

        final String[] decls = declarations.split(";");
        for (String decl : decls) {
            decl = decl.trim();
            if (decl.isEmpty()) { continue; }

            final int colonIndex = decl.indexOf(':');
            if (colonIndex == -1) { continue; }

            final String property = decl.substring(0, colonIndex).trim();
            final String value    = decl.substring(colonIndex + 1).trim();

            properties.put(property, value);
        }
        return properties;
    }

    private void applyCssClassStyles(final Map<String, String> attributes, final String classAttr) {
        // Handle multiple classes separated by spaces
        final String[] classes = classAttr.trim().split("\\s+");

        for (String className : classes) {
            final Map<String, String> classStyles = cssClasses.get(className);
            if (classStyles != null) {
                // Apply CSS properties as attributes
                // CSS properties will be overridden by element attributes
                attributes.putAll(classStyles);
            }
        }
    }

    private void applyAttributes(final SVGElement svgElement, final Element element) {
        final Map<String, String> attributes = new HashMap<>();

        // First, apply CSS class styles if element has a class attribute
        final String classAttr = element.getAttribute("class");
        if (!classAttr.isEmpty()) { applyCssClassStyles(attributes, classAttr); }

        // Then collect element attributes (these override CSS class styles)
        final var attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            final var attr = attrs.item(i);
            attributes.put(attr.getNodeName(), attr.getNodeValue());
        }

        // Parse and apply attributes
        attributeParser.parseAttributes(svgElement, attributes);
    }

    private double parseLength(String value) {
        if (value == null || value.isEmpty()) { return 0; }

        // Remove units
        value = value.replaceAll("[a-zA-Z%]+$", "").trim();

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parsePercentage(String value, final double defaultValue) {
        if (value == null || value.isEmpty()) { return defaultValue; }

        if (value.endsWith("%")) {
            value = value.substring(0, value.length() - 1);
            try {
                return Double.parseDouble(value) / 100.0;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get parsed gradients by ID.
     */
    public Map<String, SVGGradient> getGradients() { return new HashMap<>(gradients); }
}

