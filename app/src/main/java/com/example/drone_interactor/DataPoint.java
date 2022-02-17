package com.example.drone_interactor;

import static java.lang.Math.round;

public class DataPoint {
    private double x;
    private double y;
    private double z;

    public DataPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double[] getData() {
        return new double[]{this.x, this.y, this.z};
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public void setData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "(X: " + Double.valueOf(round(this.x * 100)) / 100 + "; Y: " +
                Double.valueOf(round(this.y * 100)) / 100 + "; Z: " +
                Double.valueOf(round(this.z * 100)) / 100 + ")";
    }
}
