package com.example.drone_interactor;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.radar.Radar;

public class DroneDataProcessing {

    public DataPoint[] dataPoints;

    public DataPoint currentPosition; // in meters distance from start

    public double[] currentAngle;

    private FlightController flightController;
    private static final String TAG = DroneDataProcessing.class.getName();
    private TextView[] textViews;

    public DroneDataProcessing(TextView[] textViews) {
        this.textViews = textViews;
        this.currentPosition = new DataPoint(0, 0, 0);
    }

    /**
     * textViews are as follows:
     *   index 0: mainDisplay
     *   index 1: motors on or off
     */
    public void test(Aircraft aircraft) {
        Log.i(TAG, "TEST IS WORKING");
        textViews[0].setText("TEST IS WORKING");


        // Radar radar = aircraft.getRadar();
        // Log.e(TAG, "RADAR: " + radar);
        // Log.e(TAG, "RADAR CONNECTED: " + radar.isConnected());
        // radar.setHorizontalRadarObstacleAvoidanceEnabled(true, djiError -> Log.e(TAG, "ON RESULT HORIZONTAL RADAR" + djiError.toString()));

        aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
            private long milisecondsBefore = -1;

            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                if (this.milisecondsBefore != -1) {
                    // Radar radar = aircraft.getRadar();
                    // radar.isConnected();
                    long currentTime = currentTimeMillis();
                    DroneDataProcessing.this.setNewCurrentPosition(
                            flightControllerState.getVelocityX(),
                            flightControllerState.getVelocityY(),
                            flightControllerState.getVelocityZ(),
                            (currentTime - this.milisecondsBefore));
                }

                this.milisecondsBefore = currentTimeMillis();
                MainActivity.getInstance().setText(textViews[5],
                        "Current Height: " +
                        (double)(round(flightControllerState.getUltrasonicHeightInMeters() * 100)) / 100);
                MainActivity.getInstance().setText(textViews[0], "x: " + String.valueOf(flightControllerState.getVelocityX()) + " , y: " + String.valueOf(flightControllerState.getVelocityY()) + " , z: " + String.valueOf(flightControllerState.getVelocityZ()));
                String motorsOnText = flightControllerState.areMotorsOn() == true ? "Motors are on" : "Motors are turned off";
                MainActivity.getInstance().setText(textViews[1], motorsOnText);
                flightControllerState.getAttitude();
            }
        });
    }

    public void setNewCurrentPosition(double xVelocity, double yVelocity, double zVelocity, double dtMillis) {
        double newX = this.currentPosition.getX() + xVelocity * (dtMillis / 1000);
        double newY = this.currentPosition.getY() + yVelocity * (dtMillis / 1000);
        double newZ = this.currentPosition.getZ() + zVelocity * (dtMillis / 1000);
        this.currentPosition.setData(newX, newY, newZ);
        this.textViews[2].setText("x: " + (double)(round(newX * 100)) / 100);
        this.textViews[3].setText("y: " + (double)(round(newY * 100)) / 100);
        this.textViews[4].setText("z: " + (double)(round(newZ * 100)) / 100);
    }


}
