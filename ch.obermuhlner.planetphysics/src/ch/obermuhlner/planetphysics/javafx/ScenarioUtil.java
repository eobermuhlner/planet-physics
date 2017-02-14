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
		return createAsteroids(central, count, mass, 100, 1000);
	}
	
	public static List<Planet> createAsteroids(Planet central, int count, double mass, double minOrbitRadius, double maxOrbitRadius) {
		List<Planet> asteroids = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			double orbitRadius = minOrbitRadius + (i + Math.random()) * (maxOrbitRadius - minOrbitRadius) / count;
			double hue = i * 300.0 / count;
			asteroids.add(createOrbitingPlanet(central, orbitRadius, mass, hue));
		}
		
		return asteroids;
	}

	public static Planet createOrbitingPlanet(Planet central, double orbitRadius, double mass, double hue) {
		double angle = Math.random() * 2*Math.PI;
		return createOrbitingPlanet(central, orbitRadius, angle, mass, hue);
	}
	
	public static Planet createOrbitingPlanet(Planet central, double orbitRadius, double angle, double mass, double hue) {
		Vector2 position = central.getPosition().add(Vector2.ofPolar(angle, orbitRadius));
		double orbitSpeed = Math.sqrt(Simulation.GRAVITY * (mass + central.getMass()) / orbitRadius);
		Vector2 speed = central.getSpeed().add(Vector2.ofPolar(angle + Math.PI*0.5, orbitSpeed));
		return new Planet(position, speed, mass, hue);
	}
	
}
