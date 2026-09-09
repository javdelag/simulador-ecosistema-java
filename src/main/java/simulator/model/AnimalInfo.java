package simulator.model;

import org.json.JSONObject;
import simulator.misc.Vector2D;

public interface AnimalInfo extends JSONable { // Note that it extends JSONable
	public State getState();
	public Vector2D getPosition();
	public String getGeneticCode();
	public Diet getDiet();
	public double getSpeed();
	public double getSightRange();
	public double getEnergy();
	public double getAge();
	public Vector2D getDestination();
	public boolean isPregnant();
}