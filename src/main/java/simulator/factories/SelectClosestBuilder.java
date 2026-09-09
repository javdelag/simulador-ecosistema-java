package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectClosestBuilder extends Builder<SelectionStrategy>{

    public SelectClosestBuilder(){
        super("closest", "Closest");
    }
    
    @Override
    protected SelectionStrategy createInstance(JSONObject data){
        return new SelectClosest();
    }
}
