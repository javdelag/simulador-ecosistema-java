package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

    final static String WOLF_GENETIC_CODE = "Wolf";
    final static double INIT_SIGHT_WOLF = 50;
    final static double INIT_SPEED_WOLF = 60;
    final static double BOOST_FACTOR_WOLF = 3.0;
    final static double MAX_AGE_WOLF = 14.0;
    final static double FOOD_THRSHOLD_WOLF = 50.0;
    final static double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
    final static double FOOD_DROP_RATE_WOLF = 18.0;
    final static double FOOD_DROP_DESIRE_WOLF = 10.0;
    final static double FOOD_EAT_VALUE_WOLF = 50.0;
    final static double DESIRE_THRESHOLD_WOLF = 65.0;
    final static double DESIRE_INCREASE_RATE_WOLF = 30.0;
    final static double PREGNANT_PROBABILITY_WOLF = 0.75;

    protected Animal huntTarget;
    protected SelectionStrategy huntingStrategy;

    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
        super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
        this.huntingStrategy = huntingStrategy;
        this.huntTarget = null;
    }

    protected Wolf(Wolf p1, Animal p2) {
        super(p1, p2);
        this.huntingStrategy = p1.huntingStrategy;
        this.huntTarget = null;
    }

    @Override
    public void update(double dt) {
        if (this.state == State.DEAD) {
            return;
        }

        switch (this.state) {
            case NORMAL:
                updateNormal(dt);
                break;
            case DANGER:
                break;
            case MATE:
                updateMate(dt);
                break;
            case HUNGER:
                updateHunger(dt);
                break;
            default:
                break;
        }

        if (isOutOfMap()) {
            adjustPosition();
            setState(State.NORMAL);
        }

        if (this.energy == 0.0 || this.age > MAX_AGE_WOLF) {
            setState(State.DEAD);
        }

        if (this.state != State.DEAD) {
            double foodPoints = this.regionMngr.getFood(this, dt);
            this.energy += foodPoints;
            if (this.energy > MAX_ENERGY) {
                this.energy = MAX_ENERGY;
            }
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
        }
    }

    private void updateNormal(double dt) {
        if (this.pos.distanceTo(this.dest) < COLLISION_RANGE) {
            this.dest = Vector2D.getRandomVector(0, this.regionMngr.getWidth() - 1, 0, this.regionMngr.getHeight() - 1);
        }

        move(this.speed * dt * Math.exp((this.energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));
        this.age += dt;

        this.energy -= FOOD_DROP_RATE_WOLF * dt;
        if (this.energy < 0.0) {
            this.energy = 0.0;
        }
        if (this.energy > MAX_ENERGY) {
            this.energy = MAX_ENERGY;
        }

        this.desire += DESIRE_INCREASE_RATE_WOLF * dt;
        if (this.desire < 0.0) {
            this.desire = 0.0;
        }
        if (this.desire > MAX_DESIRE) {
            this.desire = MAX_DESIRE;
        }

        if (this.energy < FOOD_THRSHOLD_WOLF) {
            setState(State.HUNGER);
        } else if (this.desire > DESIRE_THRESHOLD_WOLF) {
            setState(State.MATE);
        }
    }

    private void updateHunger(double dt) {
        if (this.huntTarget == null || (this.huntTarget != null && (this.huntTarget.getState() == State.DEAD
                || this.pos.distanceTo(this.huntTarget.getPosition()) > this.sightRange))) {
            List<Animal> herbivoresInRegion = this.regionMngr.getAnimalsInRange(this,
                    animal -> animal.getDiet() == Diet.HERBIVORE);
            if (!herbivoresInRegion.isEmpty()) {
                this.huntTarget = this.huntingStrategy.select(this, herbivoresInRegion);
            }
        }

        if (this.huntTarget == null) {
            if (this.pos.distanceTo(this.dest) < COLLISION_RANGE) {
                this.dest = Vector2D.getRandomVector(0, this.regionMngr.getWidth() - 1, 0,
                        this.regionMngr.getHeight() - 1);
            }

            move(this.speed * dt * Math.exp((this.energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));
            this.age += dt;

            this.energy -= FOOD_DROP_RATE_WOLF * dt;
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
            if (this.energy > MAX_ENERGY) {
                this.energy = MAX_ENERGY;
            }

            this.desire += DESIRE_INCREASE_RATE_WOLF * dt;
            if (this.desire < 0.0) {
                this.desire = 0.0;
            }
            if (this.desire > MAX_DESIRE) {
                this.desire = MAX_DESIRE;
            }
        } else {
            this.dest = this.huntTarget.getPosition();
            move(BOOST_FACTOR_WOLF * this.speed * dt * Math.exp((this.energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));
            this.age += dt;

            this.energy -= FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt;
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
            if (this.energy > MAX_ENERGY) {
                this.energy = MAX_ENERGY;
            }

            this.desire += DESIRE_INCREASE_RATE_WOLF * dt;
            if (this.desire < 0.0) {
                this.desire = 0.0;
            }
            if (this.desire > MAX_DESIRE) {
                this.desire = MAX_DESIRE;
            }

            if (this.pos.distanceTo(this.huntTarget.getPosition()) < COLLISION_RANGE) {
                this.huntTarget.setState(State.DEAD);
                this.huntTarget = null;
                this.energy += FOOD_EAT_VALUE_WOLF;
                if (this.energy < 0.0) {
                    this.energy = 0.0;
                }
                if (this.energy > MAX_ENERGY) {
                    this.energy = MAX_ENERGY;
                }
            }
        }

        if (this.energy > FOOD_THRSHOLD_WOLF) {
            if (this.desire < DESIRE_THRESHOLD_WOLF) {
                setState(State.NORMAL);
            } else {
                setState(State.MATE);
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
                if (this.pos.distanceTo(this.dest) < COLLISION_RANGE) {
                    this.dest = Vector2D.getRandomVector(0, this.regionMngr.getWidth() - 1, 0,
                            this.regionMngr.getHeight() - 1);
                }

                move(this.speed * dt * Math.exp((this.energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));
                this.age += dt;

                this.energy -= FOOD_DROP_RATE_WOLF * dt;
                if (this.energy < 0.0) {
                    this.energy = 0.0;
                }
                if (this.energy > MAX_ENERGY) {
                    this.energy = MAX_ENERGY;
                }

                this.desire += DESIRE_INCREASE_RATE_WOLF * dt;
                if (this.desire < 0.0) {
                    this.desire = 0.0;
                }
                if (this.desire > MAX_DESIRE) {
                    this.desire = MAX_DESIRE;
                }
            }
        } else {
            this.dest = this.mateTarget.getPosition();
            move(BOOST_FACTOR_WOLF * this.speed * dt * Math.exp((this.energy - MAX_ENERGY) * HUNGER_DECAY_EXP_FACTOR));
            this.age += dt;

            this.energy -= FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt;
            if (this.energy < 0.0) {
                this.energy = 0.0;
            }
            if (this.energy > MAX_ENERGY) {
                this.energy = MAX_ENERGY;
            }

            this.desire += DESIRE_INCREASE_RATE_WOLF * dt;
            if (this.desire < 0.0) {
                this.desire = 0.0;
            }
            if (this.desire > MAX_DESIRE) {
                this.desire = MAX_DESIRE;
            }

            if (this.pos.distanceTo(this.mateTarget.getPosition()) < COLLISION_RANGE) {
                this.desire = 0.0;
                this.mateTarget.resetDesire();

                if (this.baby == null && Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_WOLF) {
                    this.baby = new Wolf(this, this.mateTarget);
                }

                this.energy -= FOOD_DROP_DESIRE_WOLF;
                if (this.energy < 0.0) {
                    this.energy = 0.0;
                }

                this.mateTarget = null;
            }

            if (this.energy < FOOD_THRSHOLD_WOLF) {
                setState(State.HUNGER);
            } else if (this.desire < DESIRE_THRESHOLD_WOLF) {
                setState(State.NORMAL);
            }
        }
    }

    @Override
    protected void setNormalStateAction() {
        this.mateTarget = null;
        this.huntTarget = null;
    }

    @Override
    protected void setMateStateAction() {
        this.huntTarget = null;
    }

    @Override
    protected void setHungerStateAction() {
        this.mateTarget = null;
    }

    @Override
    protected void setDangerStateAction() {
    }

    @Override
    protected void setDeadStateAction() {
        this.mateTarget = null;
        this.huntTarget = null;
    }
}