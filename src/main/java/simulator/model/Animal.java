package simulator.model;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.misc.Utils;

public abstract class Animal implements Entity, AnimalInfo {

    // Constantes
    final static double INIT_ENERGY = 100.0;
    final static double MUTATION_TOLERANCE = 0.2;
    final static double NEARBY_FACTOR = 60.0;
    final static double COLLISION_RANGE = 8;
    final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
    final static double MAX_ENERGY = 100.0;
    final static double MAX_DESIRE = 100.0;

    // Atributos
    protected String geneticCode; // Código genético
    protected Diet diet; // HERBIVORE o CARNIVORE
    protected State state; // Estado actual
    protected Vector2D pos; // Posición actual
    protected Vector2D dest; // Destino actual
    protected double energy; // Energía (0.0 a 100.0)
    protected double speed; // Velocidad
    protected double age; // Edad del animal
    protected double desire; // Deseo
    protected double sightRange; // Rango de visión
    protected Animal mateTarget; // Referencia a animal con el que quiere emparejarse
    protected Animal baby; // ¿Está embarazada?
    protected AnimalMapView regionMngr; // Gestor de regiones
    protected SelectionStrategy mateStrategy; // Estrategia de seleccion para pareja

    @Override
    public void update(double dt) {
    }

    protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy,
            Vector2D pos) {
                 
       // Excepciones
        if (geneticCode == null || geneticCode.isEmpty())
            throw new IllegalArgumentException("El animal no puede tener un código genético vacío ");
        if (diet == null)
            throw new IllegalArgumentException("El animal no puede tener una dieta vacía");
        if (sightRange <= 0)
            throw new IllegalArgumentException("El animal no puede tener un rango de vision negativo");
        if (initSpeed <= 0)
            throw new IllegalArgumentException("El animal no puede tener una velocidad negativa");
        if (mateStrategy == null)
            throw new IllegalArgumentException("El animal no puede tener una estrategia de emparejamiento vacía");

        this.geneticCode = geneticCode;
        this.diet = diet;
        this.sightRange = sightRange;
        this.speed = Utils.getRandomizedParameter(initSpeed, MUTATION_TOLERANCE);
        this.mateStrategy = mateStrategy;
        this.pos = pos;

        this.state = State.NORMAL;
        this.energy = INIT_ENERGY;
        this.desire = 0.0;
        this.dest = null;
        this.mateTarget = null;
        this.baby = null;
        this.regionMngr = null;
    }

    protected Animal(Animal p1, Animal p2) {

        this.dest = null;
        this.baby = null;
        this.mateTarget = null;
        this.regionMngr = null;

        this.state = State.NORMAL;
        this.desire = 0.0;

        this.geneticCode = p1.getGeneticCode();
        this.diet = p1.getDiet();
        this.mateStrategy = p2.mateStrategy;

        this.energy = (p1.getEnergy() + p2.getEnergy()) / 2.0;

        this.pos = p1.getPosition()
                .plus(Vector2D.get_random_vector(-1, 1).scale(NEARBY_FACTOR * (Utils.RAND.nextGaussian() + 1)));

        this.sightRange = Utils.getRandomizedParameter((p1.getSightRange() + p2.getSightRange()) / 2,
                MUTATION_TOLERANCE);

        this.speed = Utils.getRandomizedParameter((p1.getSpeed() + p2.getSpeed()) / 2.0, MUTATION_TOLERANCE);
        this.age = 0.0;
    }

    void init(AnimalMapView regMngr) {
        this.regionMngr = regMngr;

        if (this.pos == null) {
            this.pos = Vector2D.getRandomVector(0, regMngr.getWidth() - 1, 0, regMngr.getHeight() - 1);
        } else {
            adjustPosition();
        }

        this.dest = Vector2D.getRandomVector(0, regMngr.getWidth() - 1, 0, regMngr.getHeight() - 1);
    }

    Animal deliverBaby() {
        Animal baby = this.baby;
        this.baby = null;
        return baby;
    }

    protected void move(double speed) {
        pos = pos.plus(dest.minus(pos).direction().scale(speed));
    }

    protected void setState(State state) {
        this.state = state;
        switch (state) {
            case NORMAL:
                setNormalStateAction();
                break;
            case HUNGER:
                setHungerStateAction();
                break;
            case DANGER:
                setDangerStateAction();
                break;
            case MATE:
                setMateStateAction();
                break;
            case DEAD:
                setDeadStateAction();
                break;
        }
    }

    abstract protected void setNormalStateAction();

    abstract protected void setMateStateAction();

    abstract protected void setHungerStateAction();

    abstract protected void setDangerStateAction();

    abstract protected void setDeadStateAction();

    @Override
    public JSONObject asJSON() {
        JSONObject json = new JSONObject();

        JSONArray posArray = new JSONArray();
        posArray.put(pos.getX());
        posArray.put(pos.getY());

        json.put("pos", posArray);
        json.put("gcode", geneticCode);
        json.put("diet", diet.toString());
        json.put("state", state.toString());

        return json;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Vector2D getPosition() {
        return pos;
    }

    @Override
    public String getGeneticCode() {
        return geneticCode;
    }

    @Override
    public Diet getDiet() {
        return diet;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getSightRange() {
        return sightRange;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getAge() {
        return age;
    }

    @Override
    public Vector2D getDestination() {
        return dest;
    }

    @Override
    public boolean isPregnant() {
        return baby != null;
    }

    protected void adjustPosition() {
        double x = this.pos.getX();
        double y = this.pos.getY();
        double width = regionMngr.getWidth();
        double height = regionMngr.getHeight();

        while (x >= width)
            x = (x - width);
        while (x < 0)
            x = (x + width);
        while (y >= height)
            y = (y - height);
        while (y < 0)
            y = (y + height);

        this.pos = new Vector2D(x, y);
    }

    protected boolean isOutOfMap() {
        double x = pos.getX();
        double y = pos.getY();
        return x < 0 || x >= regionMngr.getWidth() || y < 0 || y >= regionMngr.getHeight();
    }

    protected void resetDesire() {
        this.desire = 0.0;
    }
}