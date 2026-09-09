package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

    final static String SHEEP_GENETIC_CODE = "Sheep";
    final static double INIT_SIGHT_SHEEP = 40;
    final static double INIT_SPEED_SHEEP = 35;
    final static double BOOST_FACTOR_SHEEP = 2.0;
    final static double MAX_AGE_SHEEP = 8;
    final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
    final static double FOOD_DROP_RATE_SHEEP = 20.0;
    final static double DESIRE_THRESHOLD_SHEEP = 65.0;
    final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
    final static double PREGNANT_PROBABILITY_SHEEP = 0.9;

    protected Animal dangerSource;
    protected SelectionStrategy dangerStrategy;

    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
        super("Sheep", Diet.HERBIVORE, 40.0, 35.0, mateStrategy, pos);
        this.dangerStrategy = dangerStrategy;
        this.dangerSource = null;
    }

    protected Sheep(Sheep p1, Animal p2) {
        super(p1, p2);
        this.dangerStrategy = p1.dangerStrategy;
        this.dangerSource = null;
    }

    @Override
    public void update(double dt) {
        if (this.state == State.DEAD) { // si esta en DEAD no hacer nada
            return;
        }

        switch (this.state) {
            case NORMAL:
                updateNormal(dt);
                break;
            case DANGER:
                updateDanger(dt);
                break;
            case MATE:
                updateMate(dt);
                break;
            case HUNGER: // la oveja no tiene este estado
                break;
            default:
                break;
        }

        if (isOutOfMap()) {
            adjustPosition();
            setState(State.NORMAL);
        }

        if (this.energy == 0.0 || this.age > 8.0) {
            setState(State.DEAD);
        }

        if (this.state != State.DEAD) { // gestionamos el hambre y la comida del gestor de regiones lo añadimos a la
                                        // energia
            double foodPoints = this.regionMngr.getFood(this, dt);
            this.energy += foodPoints;
            if (this.energy > 100.0) { // mantener entre 0 y 100
                this.energy = 100.0;
            }
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
        }
    }

    private void updateNormal(double dt) {
        if (this.pos.distanceTo(this.dest) < 8.0) {
            this.dest = Vector2D.getRandomVector(0, this.regionMngr.getWidth() - 1, 0, this.regionMngr.getHeight() - 1);
        }

        move(this.speed * dt * Math.exp((energy - 100.0) * 0.007));
        this.age += dt;

        this.energy -= 20.0 * dt;
        if (this.energy < 0.0) {
            this.energy = 0.0;
        }
        if (this.energy > 100.0) {
            this.energy = 100.0;
        }

        this.desire += 40.0 * dt;
        if (this.desire < 0.0) {
            this.desire = 0.0;
        }
        if (this.desire > 100.0) {
            this.desire = 100.0;
        }

        if (this.dangerSource == null) {
            List<Animal> carnivoresInRegion = this.regionMngr.getAnimalsInRange(this,
                    animal -> animal.getDiet() == Diet.CARNIVORE); // Creamos una lista en la que buscamos solo a los
                                                                   // carnivoros que esten dentro del rango
            if (!carnivoresInRegion.isEmpty()) {
                this.dangerSource = this.dangerStrategy.select(this, carnivoresInRegion);
            }
        }

        if (this.dangerSource != null) {
            setState(State.DANGER);
        } else if (this.desire > 65.0) {
            setState(State.MATE);
        }

    }

    private void updateDanger(double dt) {
        if (this.dangerSource != null && this.dangerSource.getState() == State.DEAD) { // mira si el animal de danger
                                                                                       // esta muerto y si lo esta se
                                                                                       // quita del peligro de la oveja
            this.dangerSource = null;
        }

        if (this.dangerSource == null) {
            if (this.pos.distanceTo(this.dest) < 8.0) {
                this.dest = Vector2D.getRandomVector(0, this.regionMngr.getWidth() - 1, 0,
                        this.regionMngr.getHeight() - 1);
            }

            move(this.speed * dt * Math.exp((energy - 100.0) * 0.007));
            this.age += dt;

            this.energy -= 20.0 * dt;
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
            if (this.energy > 100.0) {
                this.energy = 100.0;
            }

            this.desire += 40.0 * dt;
            if (this.desire < 0.0) {
                this.desire = 0.0;
            }
            if (this.desire > 100.0) {
                this.desire = 100.0;
            }
        } else if (this.dangerSource != null) {

            this.dest = this.pos.plus(this.pos.minus(this.dangerSource.getPosition()).direction());
            move(2.0 * this.speed * dt * Math.exp((energy - 100.0) * 0.007));
            this.age += dt;
            this.energy -= 20.0 * 1.2 * dt;
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
            if (this.energy > 100.0) {
                this.energy = 100.0;
            }

            this.desire += 40.0 * dt;
            if (this.desire < 0.0) {
                this.desire = 0.0;
            }
            if (this.desire > 100.0) {
                this.desire = 100.0;
            }
        }

        if (this.dangerSource == null || this.pos.distanceTo(this.dangerSource.getPosition()) > this.sightRange) {
            List<Animal> carnivoresInRegion = this.regionMngr.getAnimalsInRange(this,
                    animal -> animal.getDiet() == Diet.CARNIVORE); // Creamos una lista en la que buscamos solo a los
                                                                   // carnivoros que esten dentro del rango
            if (!carnivoresInRegion.isEmpty()) {
                this.dangerSource = this.dangerStrategy.select(this, carnivoresInRegion);
            }

            if (this.dangerSource == null) {
                if (this.desire < 65.0) {
                    setState(State.NORMAL);
                } else {
                    setState(State.MATE);
                }
            }
        }
    }

    private void updateMate(double dt) {
        if (this.mateTarget != null && (this.mateTarget.getState() == State.DEAD
                || this.pos.distanceTo(this.mateTarget.getPosition()) > this.sightRange)) {
            this.mateTarget = null;
        }

        if (this.mateTarget == null) {
            List<Animal> matesInRegion = this.regionMngr.getAnimalsInRange(this,
                    animal -> animal.getGeneticCode().equals(this.geneticCode));
            if (!matesInRegion.isEmpty()) {
                this.mateTarget = this.mateStrategy.select(this, matesInRegion);
            } else {
                if (this.pos.distanceTo(this.dest) < 8.0) {
                    this.dest = Vector2D.getRandomVector(0, this.regionMngr.getWidth() - 1, 0,
                            this.regionMngr.getHeight() - 1);
                }

                move(this.speed * dt * Math.exp((energy - 100.0) * 0.007));
                this.age += dt;

                this.energy -= 20.0 * dt;
                if (this.energy < 0.0) {
                    this.energy = 0.0;
                }
                if (this.energy > 100.0) {
                    this.energy = 100.0;
                }

                this.desire += 40.0 * dt;
                if (this.desire < 0.0) {
                    this.desire = 0.0;
                }
                if (this.desire > 100.0) {
                    this.desire = 100.0;
                }
            }
        } else {
            this.dest = this.mateTarget.getPosition();

            move(2.0 * this.speed * dt * Math.exp((this.energy - 100.0) * 0.007));
            this.age += dt;

            this.energy -= 20.0 * dt;
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
            if (this.energy > 100.0) {
                this.energy = 100.0;
            }

            this.desire += 40.0 * dt;
            if (this.desire < 0.0) {
                this.desire = 0.0;
            }
            if (this.desire > 100.0) {
                this.desire = 100.0;
            }

            if (this.pos.distanceTo(this.mateTarget.getPosition()) < 8.0) {
                this.desire = 0.0;
                this.mateTarget.resetDesire();

                if (this.baby == null && Utils.RAND.nextDouble() < 0.9) {
                    this.baby = new Sheep(this, this.mateTarget);
                }

                this.mateTarget = null;

            }
        }

        if (this.dangerSource == null) {
            List<Animal> carnivoresInRegion = this.regionMngr.getAnimalsInRange(this,
                    animal -> animal.getDiet() == Diet.CARNIVORE); // Creamos una lista en la que buscamos solo a los
                                                                   // carnivoros que esten dentro del rango
            if (!carnivoresInRegion.isEmpty()) {
                this.dangerSource = this.dangerStrategy.select(this, carnivoresInRegion);
            }
        }

        if (this.dangerSource != null) {
            setState(State.DANGER);
        } else if (this.desire < 65.0) {
            setState(State.NORMAL);
        }
    }

    // Actions
    @Override
    protected void setNormalStateAction() {
        this.mateTarget = null;
        this.dangerSource = null;
    }

    @Override
    protected void setMateStateAction() {
        this.dangerSource = null;
    }

    @Override
    protected void setHungerStateAction() {

    }

    @Override
    protected void setDangerStateAction() {
        this.mateTarget = null;
    }

    @Override
    protected void setDeadStateAction() {
        this.mateTarget = null;
        this.dangerSource = null;
    }

}
