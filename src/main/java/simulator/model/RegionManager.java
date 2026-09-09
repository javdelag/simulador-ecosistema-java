package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView {

    private int cols;
    private int rows;
    private int width;
    private int height;
    private int regionWidth;
    private int regionHeight;

    private Region[][] regions;
    private Map<Animal, Region> animalRegion;

    public RegionManager(int cols, int rows, int width, int height) {
        if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
            throw new IllegalArgumentException("cols, rows, width and height must be positive");

        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.regionWidth = width / cols;
        this.regionHeight = height / rows;

        this.regions = new Region[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                regions[i][j] = new DefaultRegion();

        this.animalRegion = new HashMap<>();
    }

    // --- Helpers ---

    private Region getRegionFor(Animal a) {
        int col = (int) (a.getPosition().getX() / regionWidth);
        int row = (int) (a.getPosition().getY() / regionHeight);
        col = Math.min(col, cols - 1);
        row = Math.min(row, rows - 1);
        return regions[row][col];
    }

    private int[] getRowColFor(Animal a) {
        int col = (int) (a.getPosition().getX() / regionWidth);
        int row = (int) (a.getPosition().getY() / regionHeight);
        col = Math.min(col, cols - 1);
        row = Math.min(row, rows - 1);
        return new int[] { row, col };
    }

    // --- Regiones ---

    public void setRegion(int row, int col, Region r) {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            throw new IllegalArgumentException("Row or col out of bounds");
        if (r == null)
            throw new IllegalArgumentException("Region cannot be null");

        Region old = regions[row][col];

        // Mover los animales de la region antigua a la nueva
        for (Animal a : old.getAnimals()) {
            r.addAnimal(a);
            animalRegion.put(a, r);
        }

        regions[row][col] = r;
    }

    public void registerAnimal(Animal a) {
        a.init(this);
        Region r = getRegionFor(a);
        r.addAnimal(a);
        animalRegion.put(a, r);
    }

    public void unregisterAnimal(Animal a) {
        Region r = animalRegion.get(a);
        if (r != null) {
            r.removeAnimal(a);
            animalRegion.remove(a);
        }
    }

    public void updateAnimalRegion(Animal a) {
        Region current = animalRegion.get(a);
        Region newRegion = getRegionFor(a);
        if (current != newRegion) {
            current.removeAnimal(a);
            newRegion.addAnimal(a);
            animalRegion.put(a, newRegion);
        }
    }

    public void updateAllRegions(double dt) {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                regions[i][j].update(dt);
    }

    // --- AnimalMapView / FoodSupplier ---

    @Override
    public double getFood(AnimalInfo a, double dt) {
        // getFood se delega a la region del animal, si el animal no esta registrado o no tiene region, se devuelve 0 
        Region r = animalRegion.get((Animal) a);
        return r.getFood(a, dt);
        
    }

    @Override
    public List<Animal> getAnimalsInRange(Animal a, Predicate<Animal> filter) { // getAnimalsInRange se implementa buscando en las regiones que intersectan con el rango de vision del animal, se devuelve una lista con los animales que cumplen el filtro y estan dentro del rango de vision
        List<Animal> result = new ArrayList<>();

        double sightRange = a.getSightRange();
        double ax = a.getPosition().getX();
        double ay = a.getPosition().getY();

        // determinamos el rango de filas y columnas a revisar, teniendo en cuenta los bordes del mapa
        int colMin = (int) Math.max(0, (ax - sightRange) / regionWidth);
        int colMax = (int) Math.min(cols - 1, (ax + sightRange) / regionWidth);
        int rowMin = (int) Math.max(0, (ay - sightRange) / regionHeight);
        int rowMax = (int) Math.min(rows - 1, (ay + sightRange) / regionHeight);

        for (int i = rowMin; i <= rowMax; i++) {// luego recorremos las regiones dentro del rango y vamos añadiendo a la lista de resultado los animales que cumplen el filtro y estan dentro del rango de vision
            for (int j = colMin; j <= colMax; j++) {
                for (Animal candidate : regions[i][j].getAnimals()) {
                    if (candidate == a)
                        continue;
                    double dist = a.getPosition().distanceTo(candidate.getPosition());
                    if (dist <= sightRange && filter.test(candidate))
                        result.add(candidate);
                }
            }
        }

        return result;
    }

    // --- MapInfo ---

    @Override
    public int getCols() { return cols; }

    @Override
    public int getRows() { return rows; }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public int getRegionWidth() { return regionWidth; }

    @Override
    public int getRegionHeight() { return regionHeight; }

    // --- JSONable ---

    @Override
    public JSONObject asJSON() {
        JSONArray regionsArray = new JSONArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JSONObject entry = new JSONObject();
                entry.put("row", i);
                entry.put("col", j);
                entry.put("data", regions[i][j].asJSON());
                regionsArray.put(entry);
            }
        }
        JSONObject json = new JSONObject();
        json.put("regions", regionsArray);
        return json;
    }
}