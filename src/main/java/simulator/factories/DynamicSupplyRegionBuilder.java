package simulator.factories;

import org.json.JSONObject;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

    public DynamicSupplyRegionBuilder(){
        super("dynamic", "Dynamic Supply Region");
    }

    @Override
    protected void fillInData(JSONObject object) {
         object.put("factor", " growth factor (optional, default: 2.0");
         object.put("food", "initial food amount (optional,default: 1000.0)");
    }

    @Override
    protected Region createInstance(JSONObject data){

        double factor = 2.0;
        if(data.has("factor")){
            factor = data.getDouble("factor");
        }

        double food = 1000.0;
        if(data.has("food")){
            food = data.getDouble("food");
        }
        return new DynamicSupplyRegion(factor, food); // creamos una nueva region con los valores default o si tienen unos los que se han añadido

    }


}