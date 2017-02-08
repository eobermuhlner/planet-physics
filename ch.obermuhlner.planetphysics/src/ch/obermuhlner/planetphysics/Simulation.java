package ch.obermuhlner.planetphysics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.obermuhlner.planetphysics.math.Vector2;

public class Simulation {

	public static final double GRAVITY = 10.0;

	public final List<Planet> planets = new ArrayList<>();
	
	public void simulateStep(double deltaTime) {
		for (Planet planet : planets) {
			calculateGravity(planet, deltaTime);
		}
		
		Iterator<Planet> iterator = planets.iterator();
		while (iterator.hasNext()) {
			Planet planet = iterator.next();
			
			if (planet.isDeleted()) {
				iterator.remove();
			} else {
				planet.setPosition(planet.getPosition().add(planet.getSpeed().multiply(deltaTime)));
			}
		}
	}

	private void calculateGravity(Planet planet, double deltaTime) {
		if (planet.isDeleted()) {
			return;
		}
		
		Vector2 totalForce = Vector2.of(0, 0); 
		for (Planet other : planets) {
			if (other.isDeleted()) {
				continue;
			}
			
			if (other != planet) {
				Vector2 delta = planet.getPosition().subtract(other.getPosition());
				double distance = delta.getLength();
				if (distance < planet.getRadius() + other.getRadius()) {
					planet.merge(other);
				} else {
					double magnitude = -GRAVITY * other.getMass() / (distance * distance);
					Vector2 force = delta.normalize().multiply(magnitude);
					totalForce = totalForce.add(force);
				}
			}
		}
		planet.setSpeed(planet.getSpeed().add(totalForce.multiply(deltaTime)));
	}
	
	public void print() {
		System.out.println();
		for (Planet planet : planets) {
			System.out.println(planet);
		}
	}
}
