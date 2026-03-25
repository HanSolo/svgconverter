## SVG to JavaFX Converter

A comprehensive Java library that parses SVG files and converts them to JavaFX code in two formats:
1. **SceneGraph Version**: Uses JavaFX nodes (Group, Path, Shape, etc.)
2. **Canvas Version**: Uses JavaFX Canvas drawing commands

### Supported SVG Elements
- ✅ `<path>`     - All path commands (M, L, H, V, C, S, Q, T, A, Z)
- ✅ `<rect>`     - Rectangles with rounded corners
- ✅ `<circle>`   - Circles
- ✅ `<ellipse>`  - Ellipses
- ✅ `<line>`     - Lines
- ✅ `<polygon>`  - Polygons
- ✅ `<polyline>` - Polylines
- ✅ `<g>`        - Groups with nested elements

### Supported Attributes
- ✅ Fill (solid colors, gradients)
- ✅ Stroke (color, width, dash-array, line-cap, line-join)
- ✅ Opacity (fill-opacity, stroke-opacity, opacity)
- ✅ Transforms (translate, rotate, scale, skewX, skewY, matrix)
- ✅ ViewBox and dimensions

### Supported Features
- ✅ All SVG path commands
- ✅ Relative and absolute coordinates
- ✅ Transform composition
- ✅ Linear gradients
- ✅ Radial gradients
- ✅ Named colors (147 SVG colors)
- ✅ Hex colors (#RGB, #RRGGBB)
- ✅ RGB/RGBA colors
- ❌ Animations (not supported by design)


### Building the Project

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build
```

### Demo
```bash
# Run demo
./gradlew demo
```


### SceneGraph Version
```java
// Load and convert SVG to SceneGraph
SVGParser   parser   = new SVGParser();
SVGDocument document = parser.parse("icon.svg");

SceneGraphConverter converter = new SceneGraphConverter();
Group               root      = converter.convert(document);

// Use in JavaFX scene
Scene scene = new Scene(root, 400, 400);
stage.setScene(scene);
```

### Canvas Version
```java
// Load and convert SVG to Canvas
SVGParser   parser   = new SVGParser();
SVGDocument document = parser.parse("icon.svg");

Canvas          canvas    = new Canvas(400, 400);
CanvasConverter converter = new CanvasConverter(canvas);
converter.convert(document);

// Use in JavaFX scene
Scene scene = new Scene(new StackPane(canvas));
stage.setScene(scene);
```

### Builder Pattern
```java
// SceneGraph with builder
Group root = new SceneGraphBuilder()
    .fromSVGFile("icon.svg")
    .withScale(1.5)
    .preserveAspectRatio(true)
    .build()
    .getRoot();

// Canvas with builder
Canvas canvas = new CanvasBuilder()
    .fromSVGFile("icon.svg")
    .withSize(300, 300)
    .withBackgroundColor(Color.WHITE)
    .build()
    .getCanvas();
```

### Requirements
- Java 26+
- JavaFX 26+
- Gradle 9.x

### License
Apache 2.0
