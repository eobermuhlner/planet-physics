package ch.obermuhlner.planetphysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.obermuhlner.planetphysics.math.Vector2;

public class Simulation {

	public static final double GRAVITY = 10.0;

	private final List<Planet> planets = new ArrayList<>();

	private final List<Planet> weightlessPlanets = new ArrayList<>();

	private double weightLessThreshold = 0.0;
	
	private int tailLength;
	
	public void setWeightLessThreshold(double weightLessThreshold) {
		this.weightLessThreshold = weightLessThreshold;
	}
	
	public void setTailLength(int tailLength) {
		this.tailLength = tailLength;
	}
	
	public void clear() {
		planets.clear();
		weightlessPlanets.clear();
	}
	
	public void add(Planet planet) {
		if (planet.getMass() <= weightLessThreshold) {
			weightlessPlanets.add(planet);
		} else {
			planets.add(planet);
		}
	}
	
	public Collection<Planet> getPlanets() {
		return Collections.unmodifiableCollection(planets);
	}
	
	public Collection<Planet> getWeightlessPlanets() {
		return Collections.unmodifiableCollection(weightlessPlanets);
	}
	
	public void simulateStep(double deltaTime) {
		for (Planet planet : planets) {
			calculateGravity(planet, deltaTime);
		}
		for (Planet planet : weightlessPlanets) {
			calculateGravity(planet, deltaTime);
		}
		
		updateSpeed(planets, deltaTime);
		updateSpeed(weightlessPlanets, deltaTime);
	}
	
	private void calculateGravity(Planet planet, double deltaTime) {
		if (planet.isDeleted()) {
			return;
		}
		
		Vector2 totalForce = planets.stream()
				.map(other -> {
					if (other != planet && !other.isDeleted()) {
						Vector2 delta = planet.getPosition().subtract(other.getPosition());
						double distance = delta.getLength();
						if (distance < planet.getRadius() + other.getRadius()) {
							if (planet.getMass() == 0.0) {
								other.merge(planet);
							} else {
								planet.merge(other);
							}
						} else {
							double magnitude = -GRAVITY * other.getMass() / (distance * distance);
							Vector2 force = delta.normalize().multiply(magnitude);
							return force;
						}
					}
					return Vector2.ZERO;
				})
				.reduce(Vector2.ZERO, (accu, value) -> accu.add(value));
		
		planet.setSpeed(planet.getSpeed().add(totalForce.multiply(deltaTime)));
	}

	private void updateSpeed(List<Planet> planets, double deltaTime) {
		Iterator<Planet> iterator = planets.iterator();
		while (iterator.hasNext()) {
			Planet planet = iterator.next();
			
			if (planet.isDeleted()) {
				iterator.remove();
			} else {
				planet.setPosition(planet.getPosition().add(planet.getSpeed().multiply(deltaTime)), tailLength);
			}
		}
	}
}
