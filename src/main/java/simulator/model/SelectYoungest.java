package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {
    
  @Override
   public Animal select(Animal a, List<Animal> as) {
        if (as == null || as.isEmpty()) {
            return null;
        }

        Animal MasJoven = as.get(0);

        for( Animal posible : as){
            if(MasJoven.getAge() > posible.getAge()){
                MasJoven = posible;
            }
        }
        
        return MasJoven;
    }

}
