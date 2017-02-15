package ch.obermuhlner.planetphysics.javafx;

import static ch.obermuhlner.planetphysics.javafx.ScenarioUtil.createAsteroids;
import static ch.obermuhlner.planetphysics.javafx.ScenarioUtil.createOrbitingPlanet;
import static ch.obermuhlner.planetphysics.javafx.ScenarioUtil.createRandomPlanets;
import static ch.obermuhlner.planetphysics.javafx.ScenarioUtil.random;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ch.obermuhlner.planetphysics.Planet;
import ch.obermuhlner.planetphysics.BruteForceSimulation;
import ch.obermuhlner.planetphysics.Simulation;
import ch.obermuhlner.planetphysics.math.Vector2;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetsSimulationApp extends Application {

	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
	private static final DecimalFormat SIMULATION_INTEGER_FORMAT = new DecimalFormat("##0");
	private static final DecimalFormat SIMULATION_TIME_FORMAT = new DecimalFormat("##0.0");
	
	private static final Map<String, Supplier<List<Planet>>> SCENARIOS = new LinkedHashMap<>();
	
	static {
		SCENARIOS.put("Simple Solar System", () -> {
			List<Planet> planets = new ArrayList<>();

			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
			planets.add(central);

			planets.add(createOrbitingPlanet(central, 300, 5, Color.MAGENTA.getHue()));
			
			Planet earth = createOrbitingPlanet(central, 600, 10, Color.BLANCHEDALMOND.getHue());
			planets.add(earth);
			planets.add(createOrbitingPlanet(earth, 15, 1, Color.CORAL.getHue()));

			Planet jupiter = createOrbitingPlanet(central, 1000, 20, Color.GREEN.getHue());
			planets.add(jupiter);
			planets.add(createOrbitingPlanet(jupiter, 10, 0.1, Color.LIGHTBLUE.getHue()));
			planets.add(createOrbitingPlanet(jupiter, 25, 0.1, Color.RED.getHue()));

			return planets;
		});

		SCENARIOS.put("Jupiter Asteroids", () -> {
			List<Planet> planets = new ArrayList<>();
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
			planets.add(central);

			planets.add(createOrbitingPlanet(central, 800, 20, Color.BLANCHEDALMOND.getHue()));
			
			planets.addAll(createAsteroids(central, 10000, 0.0));

			return planets;
		});

		SCENARIOS.put("Saturn Ring", () -> {
			List<Planet> planets = new ArrayList<>();
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
			planets.add(central);

			planets.add(createOrbitingPlanet(central, 1500, 10, Color.BLANCHEDALMOND.getHue()));
			
			planets.addAll(createAsteroids(central, 10000, 0.0));

			return planets;
		});

		SCENARIOS.put("Incoming Stranger", () -> {
			List<Planet> planets = new ArrayList<>();
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
			planets.add(central);

			planets.addAll(createAsteroids(central, 10000, 0.0));

			planets.add(new Planet(Vector2.of(2000, 800), Vector2.of(-2, 0), 50, Color.BLANCHEDALMOND.getHue()));

			return planets;
		});

		SCENARIOS.put("Two Asteroid Systems", () -> {
			List<Planet> planets = new ArrayList<>();
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
			planets.add(central);
			planets.addAll(createAsteroids(central, 5000, 0.0));

			Planet central2 = createOrbitingPlanet(central, 2500, 500, Color.BLANCHEDALMOND.getHue());
			planets.add(central2);
			planets.addAll(createAsteroids(central2, 5000, 0.0));

			return planets;
		});

		SCENARIOS.put("Early Solar System", () -> {
			List<Planet> planets = new ArrayList<>();
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 10.0, Color.YELLOW.getHue());
			planets.add(central);
			planets.addAll(createAsteroids(central, 1000, 0.1, 1, 500));

			return planets;
		});

		SCENARIOS.put("Lagrange Points", () -> {
			List<Planet> planets = new ArrayList<>();
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 100.0, Color.YELLOW.getHue());
			planets.add(central);

			double orbitRadius = 200;
			
			int tooCloseToPlanet = 12;
			for (int angle = 0 + tooCloseToPlanet; angle < 360 - tooCloseToPlanet; angle++) {
				planets.add(createOrbitingPlanet(central, orbitRadius, Math.toRadians(angle), 0, Color.BLUE.getHue()));
			}
			
			planets.add(createOrbitingPlanet(central, orbitRadius, Math.toRadians(0), 2, Color.BLANCHEDALMOND.getHue()));

			planets.add(createOrbitingPlanet(central, orbitRadius, Math.toRadians(60), 0, Color.RED.getHue()));
			planets.add(createOrbitingPlanet(central, orbitRadius, Math.toRadians(-60), 0, Color.GREEN.getHue()));
			planets.add(createOrbitingPlanet(central, orbitRadius, Math.toRadians(180), 0, Color.LIGHTBLUE.getHue()));


			return planets;
		});

		SCENARIOS.put("Random 10", () -> {
			return createRandomPlanets(10, 1.0);
		});

		SCENARIOS.put("Random 100", () -> {
			return createRandomPlanets(100, 2.0);
		});

		SCENARIOS.put("Random 1000", () -> {
			return createRandomPlanets(1000, 3.0);
		});
	}
	

	private final Simulation simulation = new BruteForceSimulation();

	private BooleanProperty collisionsProperty = new SimpleBooleanProperty(true);
	private DoubleProperty deltaTimeProperty = new SimpleDoubleProperty(1.0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);
	private IntegerProperty tailProperty = new SimpleIntegerProperty(0);

	private IntegerProperty simulationStepProperty = new SimpleIntegerProperty(0);
	private DoubleProperty simulationTimeProperty = new SimpleDoubleProperty(0);
	private IntegerProperty simulationPlanetCountProperty = new SimpleIntegerProperty(0);
	private IntegerProperty simulationWeightlessPlanetCountProperty = new SimpleIntegerProperty(0);
	
	private DoubleProperty planetPositionXProperty = new SimpleDoubleProperty();
	private DoubleProperty planetPositionYProperty = new SimpleDoubleProperty();
	private DoubleProperty planetSpeedXProperty = new SimpleDoubleProperty();
	private DoubleProperty planetSpeedYProperty = new SimpleDoubleProperty();
	private DoubleProperty planetMassProperty = new SimpleDoubleProperty();
	private DoubleProperty planetHueProperty = new SimpleDoubleProperty();
	private BooleanProperty planetOrbitProperty = new SimpleBooleanProperty(true);

	private DoubleProperty planetMinPositionXProperty = new SimpleDoubleProperty(-300);
	private DoubleProperty planetMinPositionYProperty = new SimpleDoubleProperty(-300);
	private DoubleProperty planetMinSpeedXProperty = new SimpleDoubleProperty(-2);
	private DoubleProperty planetMinSpeedYProperty = new SimpleDoubleProperty(-2);
	private DoubleProperty planetMinMassProperty = new SimpleDoubleProperty(0);

	private DoubleProperty planetMaxPositionXProperty = new SimpleDoubleProperty(300);
	private DoubleProperty planetMaxPositionYProperty = new SimpleDoubleProperty(300);
	private DoubleProperty planetMaxSpeedXProperty = new SimpleDoubleProperty(2);
	private DoubleProperty planetMaxSpeedYProperty = new SimpleDoubleProperty(2);
	private DoubleProperty planetMaxMassProperty = new SimpleDoubleProperty(2);
	
	private double totalMass;

	private double translateX = 0;
	private double translateY = 0;
	
	private Canvas simulationCanvas;
	Timeline simulationTimeline = new Timeline();
	
	public PlanetsSimulationApp() {
	}
	
	public void clearPlanets() {
		simulation.clear();
		totalMass = 0;
		simulationStepProperty.set(0);
		simulationTimeProperty.set(0);
		simulationPlanetCountProperty.set(0);
		simulationWeightlessPlanetCountProperty.set(0);
	}
	
	public void addPlanet(Planet planet) {
		simulation.add(planet);
		totalMass += planet.getMass();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root);

        BorderPane mainBorderPane = new BorderPane();
        root.getChildren().add(mainBorderPane);
        
        mainBorderPane.setTop(createToolbar());
        
        mainBorderPane.setRight(createEditor());
        
        simulationCanvas = createSimulationCanvas();
		mainBorderPane.setCenter(simulationCanvas);
		setupSimulationRendering();
		setupSimulationDragging();
        
		primaryStage.setScene(scene);
        primaryStage.show();
        
		showScenarioChoice();
	}

	private void setupSimulationRendering() {
		drawSimulator();
		
		simulationTimeline.setCycleCount(Timeline.INDEFINITE);
		simulationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				simulateStep();
				drawSimulator();
			}
		}));
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
			
			translateX += deltaX;
			translateY += deltaY;
		});
		simulationCanvas.setOnMouseReleased(event -> {
			double deltaX = event.getX() - lastMouseDragX;
			double deltaY = event.getY() - lastMouseDragY;
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
			
			translateX += deltaX;
			translateY += deltaY;
		});
	}

	private Node createToolbar() {
        FlowPane toolbarFlowPane = new FlowPane(Orientation.HORIZONTAL);
        toolbarFlowPane.setHgap(4);
        toolbarFlowPane.setVgap(4);

        Button newButton = new Button("New...");
        toolbarFlowPane.getChildren().add(newButton);
        newButton.addEventHandler(ActionEvent.ACTION, event -> {
        	showScenarioChoice();
        });
        
        Button runButton = new Button("Run");
        Button stopButton = new Button("Stop");
        Button stepButton = new Button("Step");

        updateRunButtons(runButton, stopButton, stepButton, simulationTimeline.getStatus() == Status.RUNNING);

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
    		drawSimulator();
        });
        
        toolbarFlowPane.getChildren().add(new Label("Zoom:"));
        Slider zoomSlider = new Slider(-2.0, 2.0, 0.0);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setMajorTickUnit(1.0f);
        toolbarFlowPane.getChildren().add(zoomSlider);
        Bindings.bindBidirectional(zoomProperty, zoomSlider.valueProperty());
        zoomSlider.valueProperty().addListener(event -> {
    		drawSimulator();
        });

        CheckBox collisionsCheckBox = new CheckBox("Collisions");
        toolbarFlowPane.getChildren().add(collisionsCheckBox);
        Bindings.bindBidirectional(collisionsCheckBox.selectedProperty(), collisionsProperty);
        
        toolbarFlowPane.getChildren().add(new Label("Time Step:"));
        Slider deltaTimeSlider = new Slider(0.0, 5.0, 1.0);
        deltaTimeSlider.setShowTickMarks(true);
        deltaTimeSlider.setShowTickLabels(true);
        deltaTimeSlider.setMajorTickUnit(1.0f);
        toolbarFlowPane.getChildren().add(deltaTimeSlider);
        Bindings.bindBidirectional(deltaTimeProperty, deltaTimeSlider.valueProperty());

        toolbarFlowPane.getChildren().add(new Label("Tail:"));
        Slider tailSlider = new Slider(0.0, 100.0, 0.0);
        tailSlider.setShowTickMarks(true);
        tailSlider.setShowTickLabels(true);
        tailSlider.setMajorTickUnit(10f);
        toolbarFlowPane.getChildren().add(tailSlider);
        Bindings.bindBidirectional(tailProperty, tailSlider.valueProperty());

        toolbarFlowPane.getChildren().add(new Label("Step:"));
        Label stepLabel = new Label("0");
        toolbarFlowPane.getChildren().add(stepLabel);
        Bindings.bindBidirectional(stepLabel.textProperty(), simulationStepProperty, SIMULATION_INTEGER_FORMAT);

        toolbarFlowPane.getChildren().add(new Label("Time:"));
        Label timeLabel = new Label("0");
        toolbarFlowPane.getChildren().add(timeLabel);
        Bindings.bindBidirectional(timeLabel.textProperty(), simulationTimeProperty, SIMULATION_TIME_FORMAT);

        toolbarFlowPane.getChildren().add(new Label("Planets:"));
        Label planetCountLabel = new Label("0");
        toolbarFlowPane.getChildren().add(planetCountLabel);
        Bindings.bindBidirectional(planetCountLabel.textProperty(), simulationPlanetCountProperty, SIMULATION_INTEGER_FORMAT);

        toolbarFlowPane.getChildren().add(new Label("Weightless:"));
        Label planetWeightlessCountLabel = new Label("0");
        toolbarFlowPane.getChildren().add(planetWeightlessCountLabel);
        Bindings.bindBidirectional(planetWeightlessCountLabel.textProperty(), simulationWeightlessPlanetCountProperty, SIMULATION_INTEGER_FORMAT);


        return toolbarFlowPane;
	}

	private void showScenarioChoice() {
		Collection<String> scenarioNames = SCENARIOS.keySet();
		ChoiceDialog<String> scenarioChoiceDialog = new ChoiceDialog<String>(scenarioNames.iterator().next(), scenarioNames);
		scenarioChoiceDialog.setHeaderText("Select Scenario");
		scenarioChoiceDialog.setContentText("Select a scenario to simulate.");
		scenarioChoiceDialog.showAndWait().ifPresent(result -> {
			translateX = 0;
			translateY = 0;
			zoomProperty.set(0);
			
			clearPlanets();
			SCENARIOS.get(result).get().forEach(planet -> addPlanet(planet));
			drawSimulator();
		});
	}

	private void updateRunButtons(Button runButton, Button stopButton, Button stepButton, boolean running) {
		runButton.setDisable(running);
    	stopButton.setDisable(!running);
    	stepButton.setDisable(running);
	}
	
	private Node createEditor() {
		GridPane gridPane = new GridPane();
        gridPane.setHgap(4);
        gridPane.setVgap(4);
        BorderPane.setMargin(gridPane, new Insets(4));

        int rowIndex = 0;

    	Button clearButton = new Button("Clear");
    	gridPane.add(clearButton, 0, rowIndex++);
        clearButton.addEventHandler(ActionEvent.ACTION, event -> {
        	clearPlanets();
        	drawSimulator();
        });

        addLabels(gridPane, rowIndex++, null, "Value", "Min", "Max");
        addTextField(gridPane, rowIndex++, "Mass", DOUBLE_FORMAT, planetMassProperty, planetMinMassProperty, planetMaxMassProperty);
    	addHueSlider(gridPane, rowIndex++, "Color", planetHueProperty);
    	addTextField(gridPane, rowIndex++, "Position x", DOUBLE_FORMAT, planetPositionXProperty, planetMinPositionXProperty, planetMaxPositionXProperty);
    	addTextField(gridPane, rowIndex++, "Position y", DOUBLE_FORMAT, planetPositionYProperty, planetMinPositionYProperty, planetMaxPositionYProperty);
        addCheckBox(gridPane, rowIndex++, "Orbit", planetOrbitProperty);
    	addTextField(gridPane, rowIndex++, "Speed x", DOUBLE_FORMAT, planetSpeedXProperty, planetMinSpeedXProperty, planetMaxSpeedXProperty);
    	addTextField(gridPane, rowIndex++, "Speed y", DOUBLE_FORMAT, planetSpeedYProperty, planetMinSpeedYProperty, planetMaxSpeedYProperty);


    	Button newButton = new Button("Random");
    	gridPane.add(newButton, 0, rowIndex);
        newButton.addEventHandler(ActionEvent.ACTION, event -> {
        	planetMassProperty.set(random(planetMinMassProperty.get(), planetMaxMassProperty.get()));
        	planetPositionXProperty.set(random(planetMinPositionXProperty.get(), planetMaxPositionXProperty.get()));
        	planetPositionYProperty.set(random(planetMinPositionYProperty.get(), planetMaxPositionYProperty.get()));
        	planetSpeedXProperty.set(random(planetMinSpeedXProperty.get(), planetMaxSpeedXProperty.get()));
        	planetSpeedYProperty.set(random(planetMinSpeedYProperty.get(), planetMaxSpeedYProperty.get()));
        	planetHueProperty.set(random(0.0, 360.0));

        	Vector2 position = Vector2.of(planetPositionXProperty.get(), planetPositionYProperty.get());
        	double mass = planetMassProperty.get();
    		Vector2 speed;
    		if (planetOrbitProperty.get()) {
    			speed = Vector2.ofPolar(position.getAngle() + Math.PI*0.5, Math.sqrt(BruteForceSimulation.GRAVITY * (mass + totalMass) / position.getLength()));
    		} else {
    			speed = Vector2.of(planetSpeedXProperty.get(), planetSpeedYProperty.get());
    		}

        	Planet planet = new Planet(
        			position,
        			speed,
        			mass,
        			planetHueProperty.get());
        	addPlanet(planet);
        	drawSimulator();
        });

    	Button okButton = new Button("Add");
    	gridPane.add(okButton, 1, rowIndex++);
        okButton.addEventHandler(ActionEvent.ACTION, event -> {
        	Planet planet = new Planet(
        			Vector2.of(planetPositionXProperty.get(), planetPositionYProperty.get()),
        			Vector2.of(planetSpeedXProperty.get(), planetSpeedYProperty.get()),
        			planetMassProperty.get(),
        			planetHueProperty.get());
        	addPlanet(planet);
        	drawSimulator();
        });    	
    	
		return gridPane;
	}

	private void addLabels(GridPane gridPane, int rowIndex, String... labels) {
		for (int i = 0; i < labels.length; i++) {
			String label = labels[i];
			if (label != null) {
				gridPane.add(new Label(label), i, rowIndex);
			}
		}
	}
	
	@SafeVarargs
	private final <T> void addTextField(GridPane gridPane, int rowIndex, String label, Format format, Property<T>... properties) {
		if (label != null) {
			gridPane.add(new Text(label), 0, rowIndex);
		}
		for (int i = 0; i < properties.length; i++) {
			Property<T> property = properties[i];

			TextField valueTextField = new TextField();
			Bindings.bindBidirectional(valueTextField.textProperty(), property, format);
			gridPane.add(valueTextField, i+1, rowIndex);
		}
	}

	private CheckBox addCheckBox(GridPane gridPane, int rowIndex, String label, BooleanProperty booleanProperty) {
        gridPane.add(new Text(label), 0, rowIndex);
        
        CheckBox valueCheckBox = new CheckBox();
        Bindings.bindBidirectional(booleanProperty, valueCheckBox.selectedProperty());
		gridPane.add(valueCheckBox, 1, rowIndex);
		return valueCheckBox;
	}

	private Slider addHueSlider(GridPane gridPane, int rowIndex, String label, DoubleProperty doubleProperty) {
		Slider slider = addSlider(gridPane, rowIndex, label, doubleProperty, 0, 360, doubleProperty.get());
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(60.0f);
		
		Rectangle colorRectangle = new Rectangle();
		colorRectangle.setWidth(20);
		colorRectangle.setHeight(20);
		doubleProperty.addListener((changeEvent) -> {
			colorRectangle.setFill(Color.hsb(doubleProperty.get(), 1.0, 1.0));
		});
    	gridPane.add(colorRectangle, 2, rowIndex);
		
		return slider;
	}

	private Slider addSlider(GridPane gridPane, int rowIndex, String label, DoubleProperty doubleProperty, double min, double max, double value) {
		if (label != null) {
			gridPane.add(new Text(label), 0, rowIndex);
		}
        
        Slider valueSlider = new Slider(min, max, value);
        Bindings.bindBidirectional(doubleProperty, valueSlider.valueProperty());
		gridPane.add(valueSlider, 1, rowIndex);
		return valueSlider;
	}

	private Canvas createSimulationCanvas() {
        Canvas canvas = new Canvas(1200, 600);
		return canvas;
	}

	private void drawSimulator() {
		GraphicsContext graphics = simulationCanvas.getGraphicsContext2D();

		graphics.setFill(Color.BLACK);
		graphics.fillRect(0, 0, graphics.getCanvas().getWidth(), graphics.getCanvas().getHeight());
		
		double tailFactor = tailProperty.get() == 0 ? 0 : Math.pow(0.05, 1.0 / tailProperty.get());
		
		for (Planet planet : simulation.getPlanets()) {
			drawPlanet(graphics, planet, tailFactor);
		}
		for (Planet planet : simulation.getWeightlessPlanets()) {
			drawPlanet(graphics, planet, tailFactor);
		}
	}

	private void drawPlanet(GraphicsContext graphics, Planet planet, double tailFactor) {
		double radiusScreenPixels = toScreenPixels(planet.getRadius());
		Vector2 position = planet.getPosition();
		
		Color color = Color.hsb(planet.getHue(), 1.0, 1.0);

		Collection<Vector2> oldPositions = planet.getOldPositions();
		if (oldPositions != null) {
			Color tailColor = color;
			for (Vector2 tailPosition : oldPositions) {
				graphics.setStroke(tailColor);
				graphics.strokeLine(toScreenX(position.x), toScreenY(position.y), toScreenX(tailPosition.x), toScreenY(tailPosition.y));
				tailColor = tailColor.deriveColor(0, 1.0, tailFactor, 1.0);
				
				position = tailPosition;
			}
		}
		
		position = planet.getPosition();
		graphics.setFill(color);
		graphics.fillOval(toScreenX(position.x)-radiusScreenPixels/2, toScreenY(position.y)-radiusScreenPixels/2, radiusScreenPixels, radiusScreenPixels);
	}

	private double toScreenX(double x) {
		double zoomFactor = Math.pow(10.0, zoomProperty.get());
		return x / zoomFactor + simulationCanvas.getWidth() / 2 + translateX;
	}

	private double toScreenY(double y) {
		double zoomFactor = Math.pow(10.0, zoomProperty.get());
		return y / zoomFactor + simulationCanvas.getHeight() / 2  + translateY;
	}

	private double toScreenPixels(double x) {
		return Math.max(x, 1);
	}

	private void simulateStep() {
		simulation.simulateStep(deltaTimeProperty.get(), tailProperty.get());
		
		simulationStepProperty.set(simulationStepProperty.get() + 1);
		simulationTimeProperty.set(simulationTimeProperty.get() + deltaTimeProperty.get());
		
		simulationPlanetCountProperty.set(simulation.getPlanets().size());
		simulationWeightlessPlanetCountProperty.set(simulation.getWeightlessPlanets().size());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
