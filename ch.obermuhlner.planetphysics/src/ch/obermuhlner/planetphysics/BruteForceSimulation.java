package ch.obermuhlner.planetphysics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.obermuhlner.planetphysics.math.Vector2;

public class BruteForceSimulation implements Simulation {

	public static final double GRAVITY = 10.0;

	private boolean collisions = true;
	
	private double weightLessThreshold = 0.0;
	
	private final List<Planet> planets = new ArrayList<>();

	private final List<Planet> weightlessPlanets = new ArrayList<>();

	public void setCollisions(boolean collisions) {
		this.collisions = collisions;
	}
	
	public void setWeightLessThreshold(double weightLessThreshold) {
		this.weightLessThreshold = weightLessThreshold;
	}
	
	@Override
	public void clear() {
		planets.clear();
		weightlessPlanets.clear();
	}
	
	@Override
	public void add(Planet planet) {
		if (planet.getMass() <= weightLessThreshold) {
			weightlessPlanets.add(planet);
		} else {
			planets.add(planet);
		}
	}
	
	@Override
	public Collection<Planet> getPlanets() {
		return Collections.unmodifiableCollection(planets);
	}
	
	@Override
	public Collection<Planet> getWeightlessPlanets() {
		return Collections.unmodifiableCollection(weightlessPlanets);
	}

	@Override
	public void simulateStep(double deltaTime, int tailLength) {
		for (Planet planet : planets) {
			calculateGravity(planet, deltaTime);
		}
		for (Planet planet : weightlessPlanets) {
			calculateGravity(planet, deltaTime);
		}
		
		updateSpeed(planets, deltaTime, tailLength);
		updateSpeed(weightlessPlanets, deltaTime, tailLength);
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
						if (collisions && distance < planet.getRadius() + other.getRadius()) {
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

	private void updateSpeed(List<Planet> planets, double deltaTime, int tailLength) {
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
