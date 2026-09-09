package simulator.misc;

import java.util.Random;

public class Utils {
  public static final Random RAND = new Random();

  public static double constrainValueInRange(double value, double min, double max) {
    value = value > max ? max : value;
    value = value < min ? min : value;
    return value;
  }

  public static double getRandomizedParameter(double value, double tolerance) {
    assert (tolerance > 0 && tolerance <= 1);
    double t = (RAND.nextDouble() - 0.5) * 2 * tolerance;
    return value * (1 + t);
  }
}
