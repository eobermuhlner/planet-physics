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
import ch.obermuhlner.planetphysics.NamedPlanet;
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
import javafx.scene.layout.VBox;
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

			Planet central = new NamedPlanet("Sun", Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
			planets.add(central);

			planets.add(new NamedPlanet("Mercury", createOrbitingPlanet(central, 100, 0.1, Color.MAGENTA.getHue())));
			planets.add(new NamedPlanet("Venus", createOrbitingPlanet(central, 150, 0.2, Color.YELLOW.getHue())));
			
			Planet earth = new NamedPlanet("Earth", createOrbitingPlanet(central, 250, 3, Color.TURQUOISE.getHue()));
			planets.add(earth);
			planets.add(createOrbitingPlanet(earth, 5, 0.01, Color.BLANCHEDALMOND.getHue()));

			planets.add(new NamedPlanet("Mars", createOrbitingPlanet(central, 350, 0.2, Color.RED.getHue())));

			planets.addAll(createAsteroids(central, 200, 0.0, 420, 520, Color.LIGHTGREEN.getHue()));

			Planet jupiter = new NamedPlanet("Jupiter", createOrbitingPlanet(central, 700, 3, Color.BISQUE.getHue()));
			planets.add(jupiter);
			planets.add(createOrbitingPlanet(jupiter, 10, 0.01, Color.LIGHTBLUE.getHue()));
			planets.add(createOrbitingPlanet(jupiter, 15, 0.01, Color.VIOLET.getHue()));
			planets.add(createOrbitingPlanet(jupiter, 22, 0.01, Color.GREEN.getHue()));
			planets.add(createOrbitingPlanet(jupiter, 26, 0.01, Color.CADETBLUE.getHue()));

			Planet saturn = new NamedPlanet("Saturn", createOrbitingPlanet(central, 1200, 3, Color.GREEN.getHue()));
			planets.add(saturn);
			planets.addAll(createAsteroids(saturn, 50, 0.0, 5, 10, Color.BLANCHEDALMOND.getHue()));

			planets.add(new NamedPlanet("Uranus", createOrbitingPlanet(central, 1700, 0.2, Color.DEEPSKYBLUE.getHue())));
			planets.add(new NamedPlanet("Neptune", createOrbitingPlanet(central, 2400, 0.2, Color.LIGHTSTEELBLUE.getHue())));

			planets.addAll(createAsteroids(central, 100, 0.0, 2600, 3000, Color.DARKGREEN.getHue()));

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
			
			Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 50.0, Color.YELLOW.getHue());
			planets.add(central);
			planets.addAll(createAsteroids(central, 1000, 0.01, 10, 500));

			return planets;
		});

		SCENARIOS.put("Lagrange Points", () -> {
			List<Planet> planets = new ArrayList<>();
			
			double centralMass = 100.0;
			Planet central = new NamedPlanet("Sun", Vector2.of(0, 0), Vector2.of(0, 0), centralMass, Color.YELLOW.getHue());
			planets.add(central);

			double orbitRadius = 200;
			
			int tooCloseToPlanet = 12;
			for (int angle = 0 + tooCloseToPlanet; angle < 360 - tooCloseToPlanet; angle++) {
				planets.add(createOrbitingPlanet(central, orbitRadius, Math.toRadians(angle), 0, Color.BLUE.getHue()));
			}
			
			double planetMass = 1;
			NamedPlanet planet = new NamedPlanet("Planet", createOrbitingPlanet(central, orbitRadius, Math.toRadians(0), planetMass, Color.BLANCHEDALMOND.getHue()));
			planets.add(planet);

//			double lagrangeOrbitRadius = orbitRadius * Math.pow(planetMass / (3.0*centralMass), 1.0/3.0);
//			System.out.println(lagrangeOrbitRadius);
//			planets.add(new NamedPlanet("L1", createOrbitingPlanet(central, orbitRadius - lagrangeOrbitRadius, Math.toRadians(0), 0, Color.MAGENTA.getHue())));
//			planets.add(new NamedPlanet("L2", createOrbitingPlanet(central, orbitRadius + lagrangeOrbitRadius, Math.toRadians(0), 0, Color.CORAL.getHue())));
			
			planets.add(new NamedPlanet("L3", createOrbitingPlanet(central, orbitRadius, Math.toRadians(180), 0, Color.LIGHTBLUE.getHue())));
			planets.add(new NamedPlanet("L4", createOrbitingPlanet(central, orbitRadius, Math.toRadians(60), 0, Color.RED.getHue())));
			planets.add(new NamedPlanet("L5", createOrbitingPlanet(central, orbitRadius, Math.toRadians(-60), 0, Color.GREEN.getHue())));

			return planets;
		});

		SCENARIOS.put("Random 10", () -> {
			return createRandomPlanets(10, 100, 1.0);
		});

		SCENARIOS.put("Random 100", () -> {
			return createRandomPlanets(100, 200, 2.0);
		});

		SCENARIOS.put("Random 1000", () -> {
			return createRandomPlanets(1000, 400, 3.0);
		});
	}
	

	private final Simulation simulation = new BruteForceSimulation();

	private BooleanProperty collisionsProperty = new SimpleBooleanProperty(true);
	private DoubleProperty deltaTimeProperty = new SimpleDoubleProperty(1.0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);
	private BooleanProperty tailAutoProperty = new SimpleBooleanProperty();
	private IntegerProperty tailLengthProperty = new SimpleIntegerProperty(0);
	private BooleanProperty tailWeightlessProperty = new SimpleBooleanProperty();

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

        {
	        VBox box = new VBox(4);
	        toolbarFlowPane.getChildren().add(box);
	        
	        Button newButton = new Button("New...");
	        box.getChildren().add(newButton);
	        newButton.addEventHandler(ActionEvent.ACTION, event -> {
	        	showScenarioChoice();
	        });
        }
        
        {
	        VBox box = new VBox(4);
	        toolbarFlowPane.getChildren().add(box);
	        
	        Button runButton = new Button("Run");
	        Button stopButton = new Button("Stop");
	        Button stepButton = new Button("Step");
	
	        updateRunButtons(runButton, stopButton, stepButton, simulationTimeline.getStatus() == Status.RUNNING);
	
	        box.getChildren().add(runButton);
	        runButton.addEventHandler(ActionEvent.ACTION, event -> {
	            simulationTimeline.play();
	            updateRunButtons(runButton, stopButton, stepButton, true);
	        });
	
	        box.getChildren().add(stopButton);
	        stopButton.addEventHandler(ActionEvent.ACTION, event -> {
	            simulationTimeline.stop();
	            updateRunButtons(runButton, stopButton, stepButton, false);
	        });
	
	        box.getChildren().add(stepButton);
	        stepButton.addEventHandler(ActionEvent.ACTION, event -> {
	        	simulateStep();
	    		drawSimulator();
	        });
        }
        
        {
	        VBox box = new VBox(4);
	        toolbarFlowPane.getChildren().add(box);
	        
	        box.getChildren().add(new Label("Zoom:"));
	        Slider zoomSlider = new Slider(-2.0, 2.0, 0.0);
	        zoomSlider.setShowTickMarks(true);
	        zoomSlider.setShowTickLabels(true);
	        zoomSlider.setMajorTickUnit(1.0f);
	        box.getChildren().add(zoomSlider);
	        Bindings.bindBidirectional(zoomProperty, zoomSlider.valueProperty());
	        zoomSlider.valueProperty().addListener(event -> {
	    		drawSimulator();
	        });
        }
        
        {
	        VBox box = new VBox(4);
	        toolbarFlowPane.getChildren().add(box);
	        
	        box.getChildren().add(new Label("Time Step:"));
	        Slider deltaTimeSlider = new Slider(0.0, 5.0, 1.0);
	        deltaTimeSlider.setShowTickMarks(true);
	        deltaTimeSlider.setShowTickLabels(true);
	        deltaTimeSlider.setMajorTickUnit(1.0f);
	        box.getChildren().add(deltaTimeSlider);
	        Bindings.bindBidirectional(deltaTimeProperty, deltaTimeSlider.valueProperty());

	        CheckBox collisionsCheckBox = new CheckBox("Collisions");
	        box.getChildren().add(collisionsCheckBox);
	        Bindings.bindBidirectional(collisionsCheckBox.selectedProperty(), collisionsProperty);
        }

        {
	        VBox box = new VBox(4);
	        toolbarFlowPane.getChildren().add(box);
	        
	        box.getChildren().add(new Label("Tail:"));
	        CheckBox tailAutoCheckBox = new CheckBox("Auto");
	        box.getChildren().add(tailAutoCheckBox);
	        Bindings.bindBidirectional(tailAutoProperty, tailAutoCheckBox.selectedProperty());
	        tailAutoCheckBox.setSelected(true);
	
	        Slider tailLengthSlider = new Slider(0.0, 100.0, 0.0);
	        tailLengthSlider.setShowTickMarks(true);
	        tailLengthSlider.setShowTickLabels(true);
	        tailLengthSlider.setMajorTickUnit(10f);
	        box.getChildren().add(tailLengthSlider);
	        Bindings.bindBidirectional(tailLengthProperty, tailLengthSlider.valueProperty());
	        tailLengthSlider.disableProperty().bind(tailAutoCheckBox.selectedProperty());
	        
	        CheckBox tailWeightlessCheckBox = new CheckBox("Weightless");
	        box.getChildren().add(tailWeightlessCheckBox);
	        Bindings.bindBidirectional(tailWeightlessProperty, tailWeightlessCheckBox.selectedProperty());
        }
        
        {
	        GridPane gridPane = new GridPane();
	        toolbarFlowPane.getChildren().add(gridPane);
	        
	        int rowIndex = 0;
	        
	        gridPane.add(new Label("Step:"), 0, rowIndex);
	        Label stepLabel = new Label("0");
	        gridPane.add(stepLabel, 1, rowIndex++);
	        Bindings.bindBidirectional(stepLabel.textProperty(), simulationStepProperty, SIMULATION_INTEGER_FORMAT);

	        gridPane.add(new Label("Time:"), 0, rowIndex);
	        Label timeLabel = new Label("0");
	        gridPane.add(timeLabel, 1, rowIndex++);
	        Bindings.bindBidirectional(timeLabel.textProperty(), simulationTimeProperty, SIMULATION_TIME_FORMAT);
        
	        gridPane.add(new Label("Planets:"), 0, rowIndex);
	        Label planetCountLabel = new Label("0");
	        gridPane.add(planetCountLabel, 1, rowIndex++);
	        Bindings.bindBidirectional(planetCountLabel.textProperty(), simulationPlanetCountProperty, SIMULATION_INTEGER_FORMAT);

	        gridPane.add(new Label("Weightless:"), 0, rowIndex);
	        Label planetWeightlessCountLabel = new Label("0");
	        gridPane.add(planetWeightlessCountLabel, 1, rowIndex++);
	        Bindings.bindBidirectional(planetWeightlessCountLabel.textProperty(), simulationWeightlessPlanetCountProperty, SIMULATION_INTEGER_FORMAT);
        }

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
		
		int tailLength = tailLengthProperty.get();
		double tailFactor = tailLength == 0 ? 0 : Math.pow(0.05, 1.0 / tailLengthProperty.get());
		
		for (Planet planet : simulation.getWeightlessPlanets()) {
			drawPlanet(graphics, planet, tailWeightlessProperty.get() ? tailLength : 0, tailFactor);
		}
		for (Planet planet : simulation.getPlanets()) {
			drawPlanet(graphics, planet, tailLength, tailFactor);
		}
	}

	private void drawPlanet(GraphicsContext graphics, Planet planet, int tailLength, double tailFactor) {
		double radiusScreenPixels = toScreenPixels(planet.getRadius());
		Vector2 position = planet.getPosition();
		
		Color color = Color.hsb(planet.getHue(), 1.0, 1.0);

		List<Vector2> oldPositions = planet.getOldPositions();
		if (oldPositions != null) {
			Color tailColor = color;
			for (int i = 0; i < Math.min(tailLength, oldPositions.size()); i++) {
				Vector2 tailPosition = oldPositions.get(i);
				graphics.setStroke(tailColor);
				graphics.strokeLine(toScreenX(position.x), toScreenY(position.y), toScreenX(tailPosition.x), toScreenY(tailPosition.y));
				tailColor = tailColor.deriveColor(0, 1.0, tailFactor, 1.0);
				
				position = tailPosition;
			}
		}
		
		position = planet.getPosition();
		graphics.setFill(color);
		
		double screenX = toScreenX(position.x)-radiusScreenPixels/2;
		double screenY = toScreenY(position.y)-radiusScreenPixels/2;
		graphics.fillOval(screenX, screenY, radiusScreenPixels, radiusScreenPixels);
		
		graphics.fillText(planet.getName(), screenX+2+radiusScreenPixels, screenY+2+radiusScreenPixels);
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
		simulation.simulateStep(deltaTimeProperty.get(), tailLengthProperty.get());
		
		simulationStepProperty.set(simulationStepProperty.get() + 1);
		simulationTimeProperty.set(simulationTimeProperty.get() + deltaTimeProperty.get());
		
		int planetCount = simulation.getPlanets().size();
		simulationPlanetCountProperty.set(planetCount);
		
		int weightlessPlanetCount = simulation.getWeightlessPlanets().size();
		simulationWeightlessPlanetCountProperty.set(weightlessPlanetCount);

		if (tailWeightlessProperty.get()) {
			planetCount += weightlessPlanetCount;
		}
		if (tailAutoProperty.get()) {
			tailLengthProperty.set(Math.max(0, 100 - planetCount / 6));
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
