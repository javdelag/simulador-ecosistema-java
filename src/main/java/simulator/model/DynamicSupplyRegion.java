package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region { // Región con suministro dinámico de comida, la cantidad de comida disponible varía con el tiempo y depende del número de herbívoros presentes en la región

	final static double FOOD_EAT_RATE_HERBS = 60.0;
	final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
	final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;
	final static double INIT_FOOD = 100.0;
	final static double FACTOR = 2.0;

	private double food;
	private double factor;

	public DynamicSupplyRegion(double food, double factor) { // constructor de comida y el factor de crecimiento
		if (food <= 0)
			throw new IllegalArgumentException("Initial food must be > 0");
		if (factor < 0)
			throw new IllegalArgumentException("Factor must be >= 0");
		this.food = food;
		this.factor = factor;
	}

	@Override
	public double getFood(AnimalInfo a, double dt) { // método para calcular la cantidad de comida disponible para un animal dado su tipo y el número de herbívoros en la región, si es carnívoro no hay comida disponible, si es herbívoro la cantidad de comida disponible se calcula con una fórmula que depende del número de herbívoros presentes en la región y también se descuenta la cantidad de comida servida a los animales
		if (a == null)
			throw new IllegalArgumentException("AnimalInfo cannot be null");
		if (dt <= 0)
			throw new IllegalArgumentException("dt must be positive");
		if (a.getDiet() == Diet.CARNIVORE)
			return 0.0;
		int nHerb = 0;
		for (Animal x : animals) {
			if (x.getDiet() == Diet.HERBIVORE)
				nHerb++;
		}
		double demand = FOOD_EAT_RATE_HERBS
				* Math.exp(-Math.max(0.0, nHerb - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt; 
		double served = Math.min(food, demand);
		food -= served;
		return served;
	}

	@Override
	public void update(double dt) {
		if (dt <= 0)
			throw new IllegalArgumentException("dt must be positive");
		if (Utils.RAND.nextDouble() < 0.5) {
			food += dt * factor;
		}
	}
}