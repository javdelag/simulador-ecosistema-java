package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public abstract class Region implements Entity, FoodSupplier, RegionInfo {

	protected List<Animal> animals;

	public Region() {
		this.animals = new ArrayList<>();
	}

	public final void addAnimal(Animal a) {
		if (a == null)
			throw new IllegalArgumentException("Animal cannot be null");
		animals.add(a);
	}

	public final void removeAnimal(Animal a) {
		if (a == null)
			throw new IllegalArgumentException("Animal cannot be null");
		animals.remove(a);
	}

	public final List<Animal> getAnimals() {
		return Collections.unmodifiableList(animals);
	}

	@Override
	public JSONObject asJSON() {
		JSONObject o = new JSONObject();
		JSONArray arr = new JSONArray();
		for (Animal a : animals) {
			arr.put(a.asJSON());
		}
		o.put("animals", arr);
		return o;
	}
}