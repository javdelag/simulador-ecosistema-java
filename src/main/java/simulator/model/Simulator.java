package simulator.model;

import org.json.JSONObject;
import simulator.factories.Factory;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Simulator implements JSONable{
  private Factory<Animal> animalFactory;
  private Factory<Region> regionFactory;
  private RegionManager regionManager;
  private List<Animal> animals;
  private double time;

  public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory, Factory<Region> regionsFactory){
    this.animalFactory = animalsFactory;
    this.regionFactory = regionsFactory;
    this.regionManager = new RegionManager(cols, rows, width, height);
    this.animals = new ArrayList<>();
    this.time = 0.0;
  }
  
  private void setRegion(int row, int col, Region r){
    this.regionManager.setRegion(row, col, r);
  }    
  public void setRegion(int row, int col, JSONObject rJson){
    Region r = this.regionFactory.createInstance(rJson);
    setRegion(row, col, r);
  }
  private void addAnimal(Animal a){
    this.animals.add(a);
    this.regionManager.registerAnimal(a);
  }
  public void addAnimal(JSONObject aJson){
    Animal a = this.animalFactory.createInstance(aJson);
    addAnimal(a);
  }
  public MapInfo getMapInfo(){
    return this.regionManager;
  }
  public List<? extends AnimalInfo> getAnimals(){
    return Collections.unmodifiableList(this.animals);
  }
  public double getTime(){
    return this.time;
  }
  public void advance(double dt){
    this.time +=dt;

    List<Animal> deadAnimals = new ArrayList<>(); // creamos una lista con todos los animales muertos
    for(Animal animal: this.animals){ // luego recorremos toda la lista de animales y vamos comprobando cuales estan muertos para añadir a la lsita de muertos
        if(animal.getState() == State.DEAD){
            deadAnimals.add(animal);
        }
    }

    for(Animal dead : deadAnimals){// Recorremos la lista de animales muertos y los eliminamos de la lista de animales vivos y tambien de su region 
        this.animals.remove(dead);
        this.regionManager.unregisterAnimal(dead);
    }

    for(Animal animal : this.animals){ // Recorremos los animales y vamos actualizando uno por uno y luego tambien su región
        animal.update(dt);
        this.regionManager.updateAnimalRegion(animal);
    }

    this.regionManager.updateAllRegions(dt);

    List<Animal> babies = new ArrayList<>(); // Creamos una lista de bebes y vamos a ir metiendolos en la lista
    for(Animal animal : this.animals){
        if(animal.isPregnant()){
            Animal baby = animal.deliverBaby();
            if(baby != null){
                babies.add(baby);
            }
        }
    }

    for(Animal baby: babies){
        addAnimal(baby);
    }

  }
  public JSONObject asJSON(){

    JSONObject json = new JSONObject();
    json.put("time", this.time);
    json.put("state", this.regionManager.asJSON());

    return json;
  }



}
