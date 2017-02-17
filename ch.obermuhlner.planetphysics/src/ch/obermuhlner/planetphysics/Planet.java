package ch.obermuhlner.planetphysics;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetphysics.math.Vector2;

public class Planet {

	public static final int DEFAULT_TAIL_LENGTH = 0;
	
	private String name;
	private Vector2 position;
	private Vector2 speed;
	private double mass;
	private double radius;
	private double hue;
	
	private boolean deleted;
	
	public List<Vector2> oldPositions = null;

	public Planet(Vector2 position, Vector2 speed, double mass, double hue) {
		this(null, position, speed, mass, hue);
	}
	
	public Planet(String name, Planet planet) {
		this(name, planet.position, planet.speed, planet.mass, planet.hue);
	}

	public Planet(String name, Vector2 position, Vector2 speed, double mass, double hue) {
		this.name = name;
		this.position = position;
		this.speed = speed;
		this.mass = mass;
		this.radius = Math.sqrt(mass);
		this.hue = hue;
	}
	
	public void merge(Planet other) {
		other.deleted = true;
		
		hue = mass > other.mass ? hue : other.hue;
		speed = speed.multiply(mass).add(other.speed.multiply(other.mass)).divide(mass + other.mass);
		mass += other.mass;
		radius = Math.sqrt(mass);		
	}
	
	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 newPosition) {
		setPosition(newPosition, DEFAULT_TAIL_LENGTH);
	}
	
	public void setPosition(Vector2 newPosition, int tailLength) {
		if (tailLength == 0) {
			oldPositions = null;
		} else {
			if (oldPositions == null) {
				oldPositions = new ArrayList<>();
			}
			oldPositions.add(0, position);
			while (oldPositions.size() > tailLength) {
				oldPositions.remove(oldPositions.size() - 1);
			}
		}
		position = newPosition;
	}
	
	public List<Vector2> getOldPositions() {
		return oldPositions;
	}
	
	public void setSpeed(Vector2 speed) {
		this.speed = speed;
	}
	
	public Vector2 getSpeed() {
		return speed;
	}
	
	public void setMass(double mass) {
		this.mass = mass;
	}
	
	public double getMass() {
		return mass;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public double getHue() {
		return hue;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "Planet [position=" + position + ", speed=" + speed + "]";
	}
}
