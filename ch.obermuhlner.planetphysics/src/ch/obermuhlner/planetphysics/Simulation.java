package ch.obermuhlner.planetphysics;

import java.util.Collection;

public interface Simulation {

	public void clear();

	public void add(Planet planet);
	
	public Collection<Planet> getPlanets();
	
	public Collection<Planet> getWeightlessPlanets();
	
	public void simulateStep(double deltaTime, int tailLength);

}
