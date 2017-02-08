package ch.obermuhlner.planetphysics.javafx;

import java.util.Random;

import ch.obermuhlner.planetphysics.Planet;
import ch.obermuhlner.planetphysics.Simulation;
import ch.obermuhlner.planetphysics.math.Vector2;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetsSimulationApp extends Application {

	private Random random = new Random();

	private double deltaTime = 5.0;
	
	private final Simulation simulation = new Simulation();
	
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
        
        // tool bar
        FlowPane toolbarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        mainBorderPane.setTop(toolbarFlowPane);
        
        
        
        // canvas
        Canvas canvas = new Canvas(1200, 600);
        mainBorderPane.setCenter(canvas);
		GraphicsContext graphics = canvas.getGraphicsContext2D();
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

		primaryStage.setScene(scene);
        primaryStage.show();
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
		return x * 1 + 600;
	}

	private double toScreenY(double y) {
		return y * 1 + 300;
	}

	private double toScreenPixels(double x) {
		return x * 10;
	}

	private void simulateStep() {
		simulation.simulateStep(deltaTime);
	}
}
