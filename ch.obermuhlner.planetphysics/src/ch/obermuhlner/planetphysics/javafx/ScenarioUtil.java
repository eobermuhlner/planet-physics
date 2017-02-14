package ch.obermuhlner.planetphysics.javafx;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetphysics.Planet;
import ch.obermuhlner.planetphysics.Simulation;
import ch.obermuhlner.planetphysics.math.Vector2;

public class ScenarioUtil {

	public static double random(double min, double max) {
		return Math.random() * (max - min) + min;
	}
	
	public static List<Planet> createAsteroids(Planet central, int count, double mass) {
		List<Planet> asteroids = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			double orbitRadius = 100.0 + i * 1000.0 / count;
			double hue = i * 300.0 / count;
			asteroids.add(createOrbitingPlanet(central, orbitRadius, mass, hue));
		}
		
		return asteroids;
	}
	
	public static Planet createOrbitingPlanet(Planet central, double orbitRadius, double mass, double hue) {
		double angle = Math.random() * 2*Math.PI;
		Vector2 position = central.getPosition().add(Vector2.ofPolar(angle, orbitRadius));
		double orbitSpeed = Math.sqrt(Simulation.GRAVITY * (mass + central.getMass()) / orbitRadius);
		Vector2 speed = central.getSpeed().add(Vector2.ofPolar(angle + Math.PI*0.5, orbitSpeed));
		return new Planet(position, speed, mass, hue);
	}
	
}
