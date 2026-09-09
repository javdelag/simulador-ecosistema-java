package simulator.model;

public class DefaultRegion extends Region {

    final static double FOOD_EAT_RATE_HERBS = 60.0;
    final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
    final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;

    @Override
    public double getFood(AnimalInfo a, double dt) { // método para calcular la cantidad de comida disponible para un animal dado su tipo y el número de herbívoros en la región, si es carnívoro no hay comida disponible, si es herbívoro la cantidad de comida disponible se calcula con una fórmula que depende del número de herbívoros presentes en la región
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
        return FOOD_EAT_RATE_HERBS * Math.exp(-Math.max(0.0, nHerb - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt;
    }

    @Override
    public void update(double dt) {
        // does nothing
    }
}