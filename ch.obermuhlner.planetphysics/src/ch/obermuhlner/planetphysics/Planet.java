package ch.obermuhlner.planetphysics;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import ch.obermuhlner.planetphysics.math.Vector2;

public class Planet {

	private static final int TAIL_LENGTH = 100;
	
	private Vector2 position;
	private Vector2 speed;
	private double mass;
	private double radius;
	
	private boolean deleted;
	
	public final Deque<Vector2> oldPositions = new ArrayDeque<>();

	public Planet(Vector2 position, Vector2 speed, double mass) {
		this.position = position;
		this.speed = speed;
		this.mass = mass;
		this.radius = Math.sqrt(mass);
	}
	
	public void merge(Planet other) {
		other.deleted = true;
		
		speed = speed.multiply(mass).add(other.speed.multiply(other.mass)).divide(mass + other.mass);
		mass += other.mass;
		radius = Math.sqrt(mass);
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public void setPosition(Vector2 newPosition) {
		oldPositions.addFirst(position);
		if (oldPositions.size() > TAIL_LENGTH) {
			oldPositions.removeLast();
		}
		position = newPosition;
	}
	
	public Collection<Vector2> getOldPositions() {
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
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public String toString() {
		return "Planet [position=" + position + ", speed=" + speed + "]";
	}
}
