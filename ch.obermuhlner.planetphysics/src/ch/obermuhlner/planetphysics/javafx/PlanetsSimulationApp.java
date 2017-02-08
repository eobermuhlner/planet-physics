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

	private DoubleProperty deltaTimeProperty = new SimpleDoubleProperty(1.0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

	private Canvas simulationCanvas;
	
	public PlanetsSimulationApp() {

		Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0);
		simulation.planets.add(central);

		simulation.planets.add(createOrbitingPlanet(central, 100, 10));
		simulation.planets.add(createOrbitingPlanet(central, 200, 1));
		simulation.planets.add(createOrbitingPlanet(central, 300, 10));
		
//		for (int i = 0; i < 100; i++) {
//			simulation.planets.add(createOrbitingPlanet(central, random(100, 600), random(0.1, 0.2)));
//		}
	}
	
	private double random(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}
	
	private Planet createOrbitingPlanet(Planet central, double orbitRadius, double mass) {
		Vector2 position = central.getPosition().add(Vector2.of(0, orbitRadius));
		Vector2 speed = Vector2.of(Math.sqrt(Simulation.GRAVITY * (mass + central.getMass()) / orbitRadius), 0);
		return new Planet(position, speed, mass);
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

	public static void main(String[] args) {
		launch(args);
	}
}
