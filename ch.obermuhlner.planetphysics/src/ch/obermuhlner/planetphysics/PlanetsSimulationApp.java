package ch.obermuhlner.planetphysics;

import ch.obermuhlner.planetphysics.math.Vector2;

public class PlanetsSimulationApp {

	public static void main(String[] args) {
		Simulation simulation = new Simulation();
		simulation.planets.add(new Planet(Vector2.of(3, 3), Vector2.of(1, 0), 10.0));
		simulation.planets.add(new Planet(Vector2.of(-3, -3), Vector2.of(-1, 0), 10.0));
	
		double deltaTime = 1.0;

		for (int i = 0; i < 10; i++) {
			simulation.simulateStep(deltaTime);
			simulation.print();
		}
	}
}
