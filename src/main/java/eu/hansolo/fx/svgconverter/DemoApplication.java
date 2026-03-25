package eu.hansolo.fx.svgconverter;

import eu.hansolo.fx.svgconverter.converter.CanvasConverter;
import eu.hansolo.fx.svgconverter.converter.SceneGraphConverter;
import eu.hansolo.fx.svgconverter.model.*;
import eu.hansolo.fx.svgconverter.parser.SVGParser;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;


/**
 * Demo application showing SVG to JavaFX conversion.
 * Displays both SceneGraph and Canvas versions side by side.
 */
public class DemoApplication extends Application {
    private static final int DEFAULT_CANVAS_WIDTH  = 2000;
    private static final int DEFAULT_CANVAS_HEIGHT = 2000;

    private final SVGParser parser = new SVGParser();
    
    @Override
    public void start(Stage primaryStage) {
        // Create a simple SVG document programmatically
        SVGDocument document = createDemoSVGDocument();

        try {
            final String filename = getClass().getResource("./demo-icon.svg").getPath();
            document = parser.parse(filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Determine canvas dimensions from SVG document
        final double canvasWidth  = getCanvasSize(document).getWidth();
        final double canvasHeight = getCanvasSize(document).getHeight();

        // Convert to SceneGraph
        final SceneGraphConverter sceneGraphConverter = new SceneGraphConverter();
        final Group               sceneGraphRoot      = sceneGraphConverter.convert(document);
        
        // Create container for SceneGraph version
        StackPane sceneGraphPane = new StackPane(sceneGraphRoot);
        sceneGraphPane.setPrefSize(canvasWidth, canvasHeight);
        sceneGraphPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        
        VBox sceneGraphBox = new VBox(10);
        sceneGraphBox.setAlignment(Pos.CENTER);
        Label sceneGraphLabel = new Label("SceneGraph Version");
        sceneGraphLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sceneGraphBox.getChildren().addAll(sceneGraphLabel, sceneGraphPane);
        
        // Convert to Canvas
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        CanvasConverter canvasConverter = new CanvasConverter(canvas);
        canvasConverter.convert(document);
        
        // Create container for Canvas version
        StackPane canvasPane = new StackPane(canvas);
        canvasPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        
        VBox canvasBox = new VBox(10);
        canvasBox.setAlignment(Pos.CENTER);
        Label canvasLabel = new Label("Canvas Version");
        canvasLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        canvasBox.getChildren().addAll(canvasLabel, canvasPane);
        
        // Create main layout
        HBox mainLayout = new HBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(sceneGraphBox, canvasBox);
        
        // Add title
        Label titleLabel = new Label("SVG to JavaFX Converter Demo");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label descLabel = new Label("Comparing SceneGraph nodes vs Canvas rendering");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(titleLabel, descLabel);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.getChildren().addAll(titleBox, mainLayout);
        
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("SVG to JavaFX Converter Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Create a demo SVG document with various shapes.
     */
    private SVGDocument createDemoSVGDocument() {
        SVGDocument document = new SVGDocument();
        document.setWidth(200);
        document.setHeight(200);
        document.setViewBox(new SVGDocument.ViewBox(0, 0, 200, 200));
        
        SVGGroup root = new SVGGroup();
        
        // Add a blue rectangle with rounded corners
        SVGRect rect = new SVGRect(20, 20, 160, 160);
        rect.setRx(20);
        rect.setRy(20);
        rect.setFill("#4A90E2");
        rect.setStroke("#2E5C8A");
        rect.setStrokeWidth(3);
        root.addChild(rect);
        
        // Add a yellow circle
        SVGCircle circle = new SVGCircle(100, 80, 25);
        circle.setFill("#FFD700");
        circle.setStroke("#FFA500");
        circle.setStrokeWidth(2);
        root.addChild(circle);
        
        // Add a red triangle (using path)
        eu.hansolo.fx.svgconverter.model.SVGPath triangle = new eu.hansolo.fx.svgconverter.model.SVGPath();
        triangle.addCommand(new eu.hansolo.fx.svgconverter.model.SVGPath.MoveTo(60, 120, false));
        triangle.addCommand(new eu.hansolo.fx.svgconverter.model.SVGPath.LineTo(100, 160, false));
        triangle.addCommand(new eu.hansolo.fx.svgconverter.model.SVGPath.LineTo(140, 120, false));
        triangle.addCommand(new eu.hansolo.fx.svgconverter.model.SVGPath.ClosePath());
        triangle.setFill("#FF6B6B");
        triangle.setStroke("#C92A2A");
        triangle.setStrokeWidth(2);
        root.addChild(triangle);
        
        document.setRoot(root);
        return document;
    }

    private Dimension2D getCanvasSize(SVGDocument document) {
        if (document.hasViewBox()) {
            return new Dimension2D(document.getViewBox().getWidth(), document.getViewBox().getHeight());
        } else {
            return new Dimension2D(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

