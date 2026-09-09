package simulator.factories;

import org.json.JSONObject;
import simulator.model.SelectYoungest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {
    
    public SelectYoungestBuilder (){
        super("youngest", "Youngest");
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data){
        return new SelectYoungest();
    }
}
