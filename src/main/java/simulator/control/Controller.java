package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

    private Simulator sim;

    public Controller(Simulator sim) {
        if (sim == null)
            throw new IllegalArgumentException("Simulator cannot be null");
        this.sim = sim;
    }

    public void loadData(JSONObject data) {
        if (data == null)
            throw new IllegalArgumentException("Data cannot be null");

        // Regions first (optional)
        if (data.has("regions")) {
            JSONArray regions = data.getJSONArray("regions");
            for (int i = 0; i < regions.length(); i++) {
                JSONObject entry = regions.getJSONObject(i);

                JSONArray rowRange = entry.getJSONArray("row");
                JSONArray colRange = entry.getJSONArray("col");
                JSONObject spec = entry.getJSONObject("spec");

                int rf = rowRange.getInt(0);
                int rt = rowRange.getInt(1);
                int cf = colRange.getInt(0);
                int ct = colRange.getInt(1);

                for (int r = rf; r <= rt; r++)
                    for (int c = cf; c <= ct; c++)
                        sim.setRegion(r, c, spec);
            }
        }

        // Animals
        JSONArray animals = data.getJSONArray("animals");
        for (int i = 0; i < animals.length(); i++) {
            JSONObject entry = animals.getJSONObject(i);
            int amount = entry.getInt("amount");
            JSONObject spec = entry.getJSONObject("spec");

            for (int n = 0; n < amount; n++)
                sim.addAnimal(spec);
        }
    }

    public void run(double t, double dt, boolean sv, OutputStream out) {
        // Capture initial state before loop
        JSONObject initState = sim.asJSON();

        // Setup viewer if requested
        SimpleObjectViewer view = null;
        if (sv) {
            MapInfo m = sim.getMapInfo();
            view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
            view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
        }

        // Simulation loop
        while (sim.getTime() <= t) {
            sim.advance(dt);
            if (sv)
                view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
        }

        // Capture final state after loop
        JSONObject finalState = sim.asJSON();

        // Close viewer
        if (sv)
            view.close();

        // Write output JSON
        JSONObject result = new JSONObject();
        result.put("in", initState);
        result.put("out", finalState);

        PrintStream p = new PrintStream(out);
        p.println(result.toString(2));
    }

    private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
        List<ObjInfo> ol = new ArrayList<>(animals.size());
        for (AnimalInfo a : animals)
            ol.add(new ObjInfo(a.getGeneticCode(), (int) a.getPosition().getX(), (int) a.getPosition().getY(),
                    (int) Math.round(a.getAge()) + 2));
        return ol;
    }
}