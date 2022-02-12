package com.example.drone_interactor;

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
        return "x: " + this.x + "; y: " + this.y + "; z: " + this.z;
    }
}
