package simulator.misc;

import org.json.JSONArray;

public class Vector2D {
  double x;
  double y;

  // create the zero vector
  public Vector2D() {
    x = y = 0.0;
  }

  // copy constructor
  public Vector2D(Vector2D v) {
    x = v.x;
    y = v.y;
  }

  // create a vector from an array
  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  // return the inner product of this Vector a and b
  public double dot(Vector2D that) {
    return x * that.x + y * that.y;
  }

  // return the length of the vector
  public double magnitude() {
    return Math.sqrt(dot(this));
  }

  // return the distance between this and that
  public double distanceTo(Vector2D that) {
    return minus(that).magnitude();
  }

  // create and return a new object whose value is (this + that)
  public Vector2D plus(Vector2D that) {
    return new Vector2D(x + that.x, y + that.y);
  }

  // create and return a new object whose value is (this - that)
  public Vector2D minus(Vector2D that) {
    return new Vector2D(x - that.x, y - that.y);
  }

  // return the corresponding coordinate
  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  // create and return a new object whose value is (this * factor)
  public Vector2D scale(double factor) {
    return new Vector2D(x * factor, y * factor);
  }

  // return the corresponding unit vector
  public Vector2D direction() {
    if (magnitude() > 0.0)
      return scale(1.0 / magnitude());
    else
      return new Vector2D(this);
  }

  public Vector2D rotate(int deg) {

    double degree = deg;

    assert (degree >= -180.0 && degree <= 180.0);

    double angle = degree * Math.PI / 180.0;
    double sine = Math.sin(angle);
    double cosine = Math.cos(angle);

    // rotation matrix
//		double matrix[2][2] = { { cosine, -sine }, { sine, cosine } };
//		r.x = matrix[0][0] * x + matrix[0][1] * y;
//		r.y = matrix[1][0] * x + matrix[1][1] * y;

    Vector2D r = new Vector2D();

    r.x = cosine * x + (-sine) * y;
    r.y = sine * x + cosine * y;

    return r;
  }

  public double angle(Vector2D v) {
    double a2 = Math.atan2(v.getX(), v.getY());
    double a1 = Math.atan2(x, y);
    double angle = a1 - a2;
    double K = a1 > a2 ? -2.0 * Math.PI : 2.0 * Math.PI;
    angle = (Math.abs(K + angle) < Math.abs(angle)) ? K + angle : angle;
    return angle * 180.0 / Math.PI;
  }

  public static Vector2D get_random_vector(double min, double max) {
    assert (max >= min);
    double x = min + Utils.RAND.nextDouble(max - min);
    double y = min + Utils.RAND.nextDouble(max - min);
    assert (x >= min && x <= max);
    assert (y >= min && y <= max);
    return new Vector2D(x, y);
  }

  public static Vector2D getRandomVector(double minX, double maxX, double minY, double maxY) {
    assert (maxX >= minX);
    assert (maxY >= minY);
    double x = minX + Utils.RAND.nextDouble(maxX-minX);
    double y = minY + Utils.RAND.nextDouble(maxY-minY);
    assert (x >= minX && x <= maxX);
    assert (y >= minY && y <= maxY);
    return new Vector2D(x,y);
  }

  public JSONArray asJSONArray() {
    JSONArray a = new JSONArray();
    a.put(x);
    a.put(y);
    return a;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Vector2D other = (Vector2D) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    return true;
  }

  // return a string representation of the vector
  public String toString() {
    return "[" + x + "," + y + "]";
  }

}
