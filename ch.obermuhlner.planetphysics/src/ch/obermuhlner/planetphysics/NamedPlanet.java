package ch.obermuhlner.planetphysics;

import ch.obermuhlner.planetphysics.math.Vector2;

public class NamedPlanet extends Planet {

	private String name;
	
	public NamedPlanet(String name, Planet template) {
		super(template.getPosition(), template.getSpeed(), template.getMass(), template.getHue());
		
		this.name = name;
	}

	public NamedPlanet(String name, Vector2 position, Vector2 speed, double mass, double hue) {
		super(position, speed, mass, hue);
		
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
}
