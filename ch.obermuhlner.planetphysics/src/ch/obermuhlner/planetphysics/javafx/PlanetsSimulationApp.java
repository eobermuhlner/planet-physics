package ch.obermuhlner.planetphysics.javafx;

import java.util.Random;

import ch.obermuhlner.planetphysics.Planet;
import ch.obermuhlner.planetphysics.Simulation;
import ch.obermuhlner.planetphysics.math.Vector2;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetsSimulationApp extends Application {

	private Random random = new Random();

	private final Simulation simulation = new Simulation();

	private DoubleProperty deltaTimeProperty = new SimpleDoubleProperty(5.0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

	private Canvas simulationCanvas;
	
	public PlanetsSimulationApp() {

		simulation.planets.add(new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 30.0));

		simulation.planets.add(new Planet(Vector2.of(0, 100), Vector2.of(-2, 0), 1.0));
		simulation.planets.add(new Planet(Vector2.of(0, 200), Vector2.of(-1.2, 0), 1.0));
		simulation.planets.add(new Planet(Vector2.of(0, -300), Vector2.of(1.0, 0), 1.0));
		
//		for (int i = 0; i < 100; i++) {
//			Vector2 position = Vector2.of(random(-300, 300), random(-300, 300));
//			//Vector2 speed = Vector2.of(random(-1, 1), random(-1, 1));
//			Vector2 speed = Vector2.ofPolar(position.getAngle()+Math.PI*0.5, random(1.0, 2.0));
//			simulation.planets.add(new Planet(position, speed, random(0.01, 0.02)));
//		}
	}
	
	private double random(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root);

        BorderPane mainBorderPane = new BorderPane();
        root.getChildren().add(mainBorderPane);
        
        mainBorderPane.setTop(createToolbar());
        
        simulationCanvas = createSimulationCanvas();
		mainBorderPane.setCenter(simulationCanvas);
		setupSimulationRendering();
        
		primaryStage.setScene(scene);
        primaryStage.show();
	}

	private void setupSimulationRendering() {
		GraphicsContext graphics = simulationCanvas.getGraphicsContext2D();
		drawSimulator(graphics);
		
		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				simulateStep();
				drawSimulator(graphics);
			}
		}));
		timeline.playFromStart();
	}

	private Node createToolbar() {
        FlowPane toolbarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        toolbarFlowPane.setHgap(4);
        toolbarFlowPane.setVgap(4);

//        Button runButton = new Button("Run");
//        toolbarFlowPane.getChildren().add(runButton);
//        
//        Button stepButton = new Button("Step");
//        toolbarFlowPane.getChildren().add(stepButton);
        
        Slider zoomSlider = new Slider(-2, 10.0, 0.0);
        Bindings.bindBidirectional(zoomProperty, zoomSlider.valueProperty());
        toolbarFlowPane.getChildren().add(zoomSlider);

        return toolbarFlowPane;
	}

	private Canvas createSimulationCanvas() {
        Canvas canvas = new Canvas(1200, 600);
		return canvas;
	}

	private void drawSimulator(GraphicsContext graphics) {
		graphics.setFill(Color.BLACK);
		graphics.fillRect(0, 0, graphics.getCanvas().getWidth(), graphics.getCanvas().getHeight());
		
		for (Planet planet : simulation.planets) {
			graphics.setFill(Color.RED);
			double radiusScreenPixels = toScreenPixels(planet.getRadius());
			Vector2 position = planet.getPosition();
			graphics.fillOval(toScreenX(position.x)-radiusScreenPixels/2, toScreenY(position.y)-radiusScreenPixels/2, radiusScreenPixels, radiusScreenPixels);

			Color tailColor = Color.WHITE;
			for (Vector2 tailPosition : planet.getOldPositions()) {
				graphics.setStroke(tailColor);
				graphics.strokeLine(toScreenX(position.x), toScreenY(position.y), toScreenX(tailPosition.x), toScreenY(tailPosition.y));
				tailColor = tailColor.deriveColor(0, 1.0, 0.98, 1.0);
				
				position = tailPosition;
			}
		}
	}

	private double toScreenX(double x) {
		double zoomFactor = Math.pow(10.0, zoomProperty.get());
		return x / zoomFactor + simulationCanvas.getWidth() / 2;
	}

	private double toScreenY(double y) {
		double zoomFactor = Math.pow(10.0, zoomProperty.get());
		return y / zoomFactor + simulationCanvas.getHeight() / 2;
	}

	private double toScreenPixels(double x) {
		return x;
	}

	private void simulateStep() {
		simulation.simulateStep(deltaTimeProperty.get());
	}
}
