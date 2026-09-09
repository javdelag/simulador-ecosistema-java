package simulator.factories;

import org.json.JSONObject;
import simulator.model.Animal;
import simulator.model.Wolf;
import simulator.model.SelectionStrategy;
import simulator.misc.Vector2D;

public class WolfBuilder extends Builder<Animal>{

    private Factory<SelectionStrategy> strategyFactory;
    
    public WolfBuilder(Factory<SelectionStrategy> strategyFactory){
        super("wolf", "Wolf");
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected void fillInData(JSONObject object){
        object.put("mate_strategy", "selection strategy for mating (optional), default: first");
        object.put("hunt_strategy", "selection strategy for hunting (optional), default: first");    
        object.put("pos", "position with x and y (optional) default: random");
    }

    @Override
    protected Animal createInstance(JSONObject data){
        SelectionStrategy mateStrategy;
        if(data.has("mate_strategy")){
            mateStrategy = strategyFactory.createInstance(data.getJSONObject("mate_strategy"));
        }
        else{ // sino por defecto que es selecFirst
            JSONObject defaultStrategy = new JSONObject();
            defaultStrategy.put("type","first");
            defaultStrategy.put("data", new JSONObject());
            mateStrategy = strategyFactory.createInstance(defaultStrategy);
        }

        // hacemos lo mismo con la estrategia de hunt
        SelectionStrategy huntingStrategy;
        if(data.has("hunt_strategy")){
            huntingStrategy = strategyFactory.createInstance(data.getJSONObject("hunt_strategy"));
        }
        else{ // sino por defecto que es selecFirst
            JSONObject defaultStrategy = new JSONObject();
            defaultStrategy.put("type","first");
            defaultStrategy.put("data", new JSONObject());
            huntingStrategy = strategyFactory.createInstance(defaultStrategy);
        }

        Vector2D pos = null;
        if(data.has("pos")){
            JSONObject dataPos = data.getJSONObject("pos");

            if(dataPos.has("x_range") && dataPos.has("y_range")){
                double minX = dataPos.getJSONArray("x_range").getDouble(0);
                double maxX = dataPos.getJSONArray("x_range").getDouble(1);
                double minY = dataPos.getJSONArray("y_range").getDouble(0);
                double maxY = dataPos.getJSONArray("y_range").getDouble(1);

                pos = Vector2D.getRandomVector(minX, maxX, minY, maxY);
            }
        }
        return new Wolf(mateStrategy,huntingStrategy, pos);
    }
}
