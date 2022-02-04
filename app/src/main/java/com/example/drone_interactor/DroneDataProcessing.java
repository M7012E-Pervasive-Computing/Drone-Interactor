package com.example.drone_interactor;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class DroneDataProcessing {

    public DataPoint[] dataPoints;

    public double[] currentPosition;

    public double[] currentAngle;

    private FlightController flightController;
    private static final String TAG = DroneDataProcessing.class.getName();


    public void test(TextView[] textViews, Aircraft aircraft) {
        Log.i(TAG, "TEST IS WORKING");
        textViews[0].setText("TEST IS WORKING");

        aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                MainActivity.getInstance().setText(textViews[0], "x: " + String.valueOf(flightControllerState.getVelocityX()) + " , y: " + String.valueOf(flightControllerState.getVelocityY()) + " , z: " + String.valueOf(flightControllerState.getVelocityZ()));
                MainActivity.getInstance().setText(textViews[1], flightControllerState.areMotorsOn() == true ? "Motors are on" : "Motors are turned off");
            }
        });
        // new FlightController();

    /*FlightControllerState.Callback() {

            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                test.setText(flightControllerState.areMotorsOn() ? "MOTORS ON" : "MOTORS OFF");
                Log.i(TAG, flightControllerState.toString());
            }
        };*/
        // DJISDKManager.getInstance().startConnectionToProduct();
        /*while (true) {
            dji.common.flightcontroller.ObstacleDetectionSector.class
        }*/
    }



}
