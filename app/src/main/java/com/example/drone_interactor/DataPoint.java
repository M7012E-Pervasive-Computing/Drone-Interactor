package com.example.drone_interactor;

import static java.lang.Math.round;

/**
 * A data point class which stores data coordinates. There exists methods for getting and setting
 * data coordinates, as well an override method for toString().
 */
public class DataPoint {
    private double x;
    private double y;
    private double z;

    /**
     * Constructor for a data point.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public DataPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Getter for coordinate as a double list
     * @return coordinate as a double list
     */
    public double[] getData() {
        return new double[]{this.x, this.y, this.z};
    }

    /**
     * Getter for x coordinate.
     * @return x coordinate
     */
    public double getX() {
        return this.x;
    }

    /**
     * Getter for y coordinate.
     * @return y coordinate
     */
    public double getY() {
        return this.y;
    }

    /**
     * Getter for z coordinate.
     * @return z coordinate
     */
    public double getZ() {
        return this.z;
    }

    /**
     * Setter for coordinate
     */
    public void setData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * override method for toString()
     * @return string representation of data point
     */
    public String toString() {
        return "(X: " + Double.valueOf(round(this.x * 100)) / 100 + "; Y: " +
                Double.valueOf(round(this.y * 100)) / 100 + "; Z: " +
                Double.valueOf(round(this.z * 100)) / 100 + ")";
    }
}
