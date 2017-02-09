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
import javafx.scene.control.Label;
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

	private double translateXProperty = 0;
	private double translateYProperty = 0;
	
	private Canvas simulationCanvas;
	Timeline simulationTimeline = new Timeline();
	
	public PlanetsSimulationApp() {

		Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
		simulation.planets.add(central);

		simulation.planets.add(createOrbitingPlanet(central, 100, 10, Color.GREEN.getHue()));
		simulation.planets.add(createOrbitingPlanet(central, 200, 1, Color.MAGENTA.getHue()));
		simulation.planets.add(createOrbitingPlanet(central, 300, 10, Color.BLUE.getHue()));
		
//		for (int i = 0; i < 100; i++) {
//			double orbitRadius = random(100, 600);
//			double mass = random(0.0, 0.1);
//			double hue = Color.RED.getHue() + mass * 200;
//			simulation.planets.add(createOrbitingPlanet(central, orbitRadius, mass, hue));
//		}
	}
	
	private double random(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}
	
	private Planet createOrbitingPlanet(Planet central, double orbitRadius, double mass, double hue) {
		double angle = random(0, 2*Math.PI);
		Vector2 position = central.getPosition().add(Vector2.ofPolar(angle, orbitRadius));
		Vector2 speed = Vector2.ofPolar(angle + Math.PI*0.5, Math.sqrt(Simulation.GRAVITY * (mass + central.getMass()) / orbitRadius));
		return new Planet(position, speed, mass, hue);
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
		setupSimulationDragging();
        
		primaryStage.setScene(scene);
        primaryStage.show();
	}

	private void setupSimulationRendering() {
		GraphicsContext graphics = simulationCanvas.getGraphicsContext2D();
		drawSimulator(graphics);
		
		simulationTimeline.setCycleCount(Timeline.INDEFINITE);
		simulationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				simulateStep();
				drawSimulator(graphics);
			}
		}));
		simulationTimeline.play();
	}
	
	private double lastMouseDragX;
	private double lastMouseDragY;
	private void setupSimulationDragging() {
		simulationCanvas.setOnMousePressed(event -> {
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
		});
		simulationCanvas.setOnMouseDragged(event -> {
			double deltaX = event.getX() - lastMouseDragX;
			double deltaY = event.getY() - lastMouseDragY;
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
			
			translateXProperty += deltaX;
			translateYProperty += deltaY;
		});
		simulationCanvas.setOnMouseReleased(event -> {
			double deltaX = event.getX() - lastMouseDragX;
			double deltaY = event.getY() - lastMouseDragY;
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
			
			translateXProperty += deltaX;
			translateYProperty += deltaY;
		});
	}

	private Node createToolbar() {
        FlowPane toolbarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        toolbarFlowPane.setHgap(4);
        toolbarFlowPane.setVgap(4);

        Button runButton = new Button("Run");
        Button stopButton = new Button("Stop");
        Button stepButton = new Button("Step");

        updateRunButtons(runButton, stopButton, stepButton, true);

        toolbarFlowPane.getChildren().add(runButton);
        runButton.addEventHandler(ActionEvent.ACTION, event -> {
            simulationTimeline.play();
            updateRunButtons(runButton, stopButton, stepButton, true);
        });

        toolbarFlowPane.getChildren().add(stopButton);
        stopButton.addEventHandler(ActionEvent.ACTION, event -> {
            simulationTimeline.stop();
            updateRunButtons(runButton, stopButton, stepButton, false);
        });

        toolbarFlowPane.getChildren().add(stepButton);
        stepButton.addEventHandler(ActionEvent.ACTION, event -> {
        	simulateStep();
    		GraphicsContext graphics = simulationCanvas.getGraphicsContext2D();
    		drawSimulator(graphics);
        });
        
        toolbarFlowPane.getChildren().add(new Label("Zoom:"));
        Slider zoomSlider = new Slider(-2.0, 2.0, 0.0);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setMajorTickUnit(1.0f);
        toolbarFlowPane.getChildren().add(zoomSlider);
        Bindings.bindBidirectional(zoomProperty, zoomSlider.valueProperty());
        zoomSlider.valueProperty().addListener(event -> {
    		GraphicsContext graphics = simulationCanvas.getGraphicsContext2D();
    		drawSimulator(graphics);
        });
        
        return toolbarFlowPane;
	}

	private void updateRunButtons(Button runButton, Button stopButton, Button stepButton, boolean running) {
		runButton.setDisable(running);
    	stopButton.setDisable(!running);
    	stepButton.setDisable(running);
	}

	private Canvas createSimulationCanvas() {
        Canvas canvas = new Canvas(1200, 600);
		return canvas;
	}

	private void drawSimulator(GraphicsContext graphics) {
		graphics.setFill(Color.BLACK);
		graphics.fillRect(0, 0, graphics.getCanvas().getWidth(), graphics.getCanvas().getHeight());
		
		for (Planet planet : simulation.planets) {
			Color color = Color.hsb(planet.getHue(), 1.0, 1.0);
			graphics.setFill(color);
			double radiusScreenPixels = toScreenPixels(planet.getRadius());
			Vector2 position = planet.getPosition();
			graphics.fillOval(toScreenX(position.x)-radiusScreenPixels/2, toScreenY(position.y)-radiusScreenPixels/2, radiusScreenPixels, radiusScreenPixels);

			Color tailColor = color;
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
		return x / zoomFactor + simulationCanvas.getWidth() / 2 + translateXProperty;
	}

	private double toScreenY(double y) {
		double zoomFactor = Math.pow(10.0, zoomProperty.get());
		return y / zoomFactor + simulationCanvas.getHeight() / 2  + translateYProperty;
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
