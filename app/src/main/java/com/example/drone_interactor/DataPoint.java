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
}
