package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy{
    @Override
    public Animal select(Animal a, List<Animal> as){
        if (as == null || as.isEmpty()) {
           return null;
        }

        Animal cercano = null;
        double DistanciaMin = Double.MAX_VALUE;

        for(Animal posible:as){
            double Distancia = a.getPosition().distanceTo(posible.getPosition());

            if(Distancia < DistanciaMin){
                DistanciaMin = Distancia;
                cercano = posible;
            }

        }
        return cercano;
    }

}
