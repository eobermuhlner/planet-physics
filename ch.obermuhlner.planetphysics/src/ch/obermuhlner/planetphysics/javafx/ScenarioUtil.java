package ch.obermuhlner.planetphysics.javafx;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetphysics.Planet;
import ch.obermuhlner.planetphysics.BruteForceSimulation;
import ch.obermuhlner.planetphysics.math.Vector2;

public class ScenarioUtil {

	public static double random(double min, double max) {
		return Math.random() * (max - min) + min;
	}

	public static List<Planet> createAsteroids(Planet central, int count, double mass) {
		return createAsteroids(central, count, mass, 100, 1000);
	}
	
	public static List<Planet> createAsteroids(Planet central, int count, double mass, double minOrbitRadius, double maxOrbitRadius) {
		return createAsteroids(central, count, mass, minOrbitRadius, maxOrbitRadius, null);
	}
	
	public static List<Planet> createAsteroids(Planet central, int count, double mass, double minOrbitRadius, double maxOrbitRadius, Double defaultHue) {
		List<Planet> asteroids = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			double orbitRadius = minOrbitRadius + (i + Math.random()) * (maxOrbitRadius - minOrbitRadius) / count;
			double hue = defaultHue != null ? defaultHue : i * 300.0 / count;
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
		double orbitSpeed = Math.sqrt(BruteForceSimulation.GRAVITY * (mass + central.getMass()) / orbitRadius);
		Vector2 speed = central.getSpeed().add(Vector2.ofPolar(angle + Math.PI*0.5, orbitSpeed));
		return new Planet(position, speed, mass, hue);
	}
	
	public static List<Planet> createRandomPlanets(int count, double maxRadius, double maxSpeed) {
		List<Planet> planets = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			planets.add(new Planet(
					Vector2.of(random(-maxRadius, maxRadius), random(-maxRadius, maxRadius)),
					Vector2.of(random(-maxSpeed, maxSpeed), random(-maxSpeed, maxSpeed)),
					random(0.1, 2),
					random(0, 360)));
		}
		
		return planets;
	}
}
