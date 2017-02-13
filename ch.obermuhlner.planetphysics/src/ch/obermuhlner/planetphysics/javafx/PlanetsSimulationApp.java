package ch.obermuhlner.planetphysics.javafx;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Random;

import ch.obermuhlner.planetphysics.Planet;
import ch.obermuhlner.planetphysics.Simulation;
import ch.obermuhlner.planetphysics.math.Vector2;
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

	private Random random = new Random();

	private final Simulation simulation = new Simulation();

	private DoubleProperty deltaTimeProperty = new SimpleDoubleProperty(1.0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);
	private IntegerProperty tailProperty = new SimpleIntegerProperty(3);

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

	private double translateXProperty = 0;
	private double translateYProperty = 0;
	
	private Canvas simulationCanvas;
	Timeline simulationTimeline = new Timeline();
	
	public PlanetsSimulationApp() {

		Planet central = new Planet(Vector2.of(0, 0), Vector2.of(0, 0), 1000.0, Color.YELLOW.getHue());
		addPlanet(central);

//		addPlanet(createOrbitingPlanet(central, 100, 10, Color.GREEN.getHue()));
//		addPlanet(createOrbitingPlanet(central, 320, 1, Color.BLUE.getHue()));

//		addPlanet(createOrbitingPlanet(central, 300, 20, Color.BLANCHEDALMOND.getHue()));
//		addPlanet(createOrbitingPlanet(central, 600, 10, Color.MAGENTA.getHue()));

		addPlanet(new Planet(Vector2.of(2000, 800), Vector2.of(-2, 0), 10, Color.BLANCHEDALMOND.getHue()));
		
		int n = 10000;
		for (int i = 0; i < n; i++) {
			double orbitRadius = 100.0 + i * 1000.0 / n;
			double mass = 0.0;
			double hue = i * 300.0 / n;
			addPlanet(createOrbitingPlanet(central, orbitRadius, mass, hue));
		}
	}
	
	private double random(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}
	
	private Planet createOrbitingPlanet(Planet central, double orbitRadius, double mass, double hue) {
		double angle = random(0, 2*Math.PI);
		Vector2 position = central.getPosition().add(Vector2.ofPolar(angle, orbitRadius));
		Vector2 speed = Vector2.ofPolar(angle + Math.PI*0.5, Math.sqrt(Simulation.GRAVITY * (mass + totalMass) / orbitRadius));
		return new Planet(position, speed, mass, hue);
	}
	
	public void clearPlanets() {
		simulation.clear();
		totalMass = 0;
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

        toolbarFlowPane.getChildren().add(new Label("Time:"));
        Slider deltaTimeSlider = new Slider(0.0, 5.0, 1.0);
        deltaTimeSlider.setShowTickMarks(true);
        deltaTimeSlider.setShowTickLabels(true);
        deltaTimeSlider.setMajorTickUnit(1.0f);
        toolbarFlowPane.getChildren().add(deltaTimeSlider);
        Bindings.bindBidirectional(deltaTimeProperty, deltaTimeSlider.valueProperty());

        toolbarFlowPane.getChildren().add(new Label("Tail:"));
        Slider tailSlider = new Slider(0.0, 100.0, 3.0);
        tailSlider.setShowTickMarks(true);
        tailSlider.setShowTickLabels(true);
        tailSlider.setMajorTickUnit(10f);
        toolbarFlowPane.getChildren().add(tailSlider);
        Bindings.bindBidirectional(tailProperty, tailSlider.valueProperty());

        return toolbarFlowPane;
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
    			speed = Vector2.ofPolar(position.getAngle() + Math.PI*0.5, Math.sqrt(Simulation.GRAVITY * (mass + totalMass) / position.getLength()));
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
		
		double tailFactor = Math.pow(0.05, 1.0 / tailProperty.get());
		
		for (Planet planet : simulation.getPlanets()) {
			drawPlanet(graphics, planet, tailFactor);
		}
		for (Planet planet : simulation.getWeightlessPlanets()) {
			drawPlanet(graphics, planet, tailFactor);
		}
	}

	private void drawPlanet(GraphicsContext graphics, Planet planet, double tailFactor) {
		Color color = Color.hsb(planet.getHue(), 1.0, 1.0);
		graphics.setFill(color);
		double radiusScreenPixels = toScreenPixels(planet.getRadius());
		Vector2 position = planet.getPosition();
		graphics.fillOval(toScreenX(position.x)-radiusScreenPixels/2, toScreenY(position.y)-radiusScreenPixels/2, radiusScreenPixels, radiusScreenPixels);

		Color tailColor = color;
		for (Vector2 tailPosition : planet.getOldPositions()) {
			graphics.setStroke(tailColor);
			graphics.strokeLine(toScreenX(position.x), toScreenY(position.y), toScreenX(tailPosition.x), toScreenY(tailPosition.y));
			tailColor = tailColor.deriveColor(0, 1.0, tailFactor, 1.0);
			
			position = tailPosition;
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
		simulation.setTailLength(tailProperty.get());
		simulation.simulateStep(deltaTimeProperty.get());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
