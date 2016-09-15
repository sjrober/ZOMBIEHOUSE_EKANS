package cs351.core;

/**
 * Java's Point classes don't let you set a new value for the individual
 * components, forcing you to create a new object even to make the smallest change.
 *
 * This class gets around this limitation.
 *
 * Note that this is bare-bones (cross product, dot product, etc. are left out) because
 * of the very limited way we use this class.
 *
 * @author Justin Hall
 */
final public class Vector3
{
  private double x, y, z;
  private float magnitude;
  private boolean calcMagnitude = true;

  /**
   * Main constructor - all values initialized to value.
   *
   * @param value value to use to initialize
   */
  public Vector3(double value)
  {
    set(value, value, value);
  }

  /**
   * Specifies each value individually.
   * @param x x value
   * @param y y value
   * @param z z value
   */
  public Vector3(double x, double y, double z)
  {
    set(x, y, z);
  }

  /**
   * Creates a Vector3 from the values of another Vector3.
   *
   * @param vec3 Vector3 to use to initialize
   */
  public Vector3(Vector3 vec3)
  {
    set(vec3.x, vec3.y, vec3.z);
  }

  /**
   * Custom hash code.
   *
   * @return custom hash code
   */
  @Override
  public int hashCode()
  {
    // not very good but should be ok for now (I think)
    return (int)((10 * ((int)x ^ 10) / z) * (20 * ((int)y ^ 20) / x) * (30 * ((int)z ^ 30) / y));
  }

  /**
   * Custom equals.
   *
   * @param other other Vector3
   * @return true if equal
   */
  @Override
  public boolean equals(Object other)
  {
    if (this == other) return true;
    else if (!(other instanceof Vector3)) return false;
    Vector3 vec3 = (Vector3)other;
    return this.x == vec3.x && this.y == vec3.y && this.z == vec3.z;
  }

  /**
   * Sets the values to be x, y, z
   * @param x x value
   * @param y y value
   * @param z z value
   */
  public void set(double x, double y, double z)
  {
    calcMagnitude = true;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Sets the values to be the values of the Vector3
   * @param vec3 other Vector3 object
   */
  public void set(Vector3 vec3)
  {
    set(vec3.x, vec3.y, vec3.z);
  }

  /**
   * Sets the x value
   * @param x x value
   */
  public void setX(double x)
  {
    set(x, this.y, this.z);
  }

  /**
   * Sets the y value
   * @param y y value
   */
  public void setY(double y)
  {
    set(this.x, y, this.z);
  }

  /**
   * Sets the z value
   * @param z z value
   */
  public void setZ(double z)
  {
    set(this.x, this.y, z);
  }

  /**
   * Gets the x value
   * @return x value
   */
  public double getX()
  {
    return x;
  }

  /**
   * Gets the y value
   * @return y value
   */
  public double getY()
  {
    return y;
  }

  /**
   * Gets the z value
   * @return z value
   */
  public double getZ()
  {
    return z;
  }

  /**
   * Normalizes the current Vector3.
   */
  public void normalize()
  {
    magnitude();
    x /= magnitude;
    y /= magnitude;
    z /= magnitude;
  }

  /**
   * Gets the magnitude
   * @return magnitude
   */
  public double magnitude()
  {
    if (calcMagnitude) magnitude = (float)Math.sqrt(x * x + y * y + z * z);
    calcMagnitude = false;
    return magnitude;
  }

  /**
   * Subtracts this vector from the other and creates a new Vector3.
   * @param other other vector
   * @return new vector representing the subtraction
   */
  public Vector3 subtract(Vector3 other)
  {
    return new Vector3(this.x - other.x, this.y - other.y, this.z - other.y);
  }

  /**
   * Adds this vector from the other and creates a new Vector3.
   * @param other other vector
   * @return new vector representing the addition
   */
  public Vector3 add(Vector3 other)
  {
    return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
  }

  /**
   * Performs dot product
   * @param other other vector
   * @return dot product
   */
  public double dot(Vector3 other)
  {
    return this.x * other.x + this.y * other.y + this.z * other.z;
  }

  /**
   * Performs the cross product
   * @param other other vector
   * @return cross product
   */
  public Vector3 cross(Vector3 other)
  {
    return new Vector3(this.y * other.z - this.z * other.y,
                       this.z * other.x - this.x * other.z,
                       this.x * other.y - this.y * other.x);
  }
}
