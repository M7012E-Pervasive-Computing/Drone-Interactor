package com.example.drone_interactor;

import android.util.Log;

import java.util.Timer;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class DroneDriving implements Runnable {
    private TextViews textViews;
    private Aircraft aircraft;
    private FlightController flightController;
    private static DroneDriving INSTANCE;
    private Timer sendVirtualStickDataTimer;
    private double[] steps;

    private static final String TAG = DroneDriving.class.getName();


    private final int delay = 100;


    public static DroneDriving getInstance() {
        if (DroneDriving.INSTANCE == null) {
            DroneDriving.INSTANCE = new DroneDriving();
        }
        return DroneDriving.INSTANCE;
    }

    private DroneDriving() { }

    public void setAircraft(Aircraft aircraft, TextViews textviews) {
        this.aircraft = aircraft;
        this.flightController = this.aircraft.getFlightController();
        this.textViews = textviews;
        // this.startYawListener();
        this.flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        this.flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
    }


    @Override
    public void run() {
        for (int i = 0; i < this.steps.length; i++) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                return;
            }
            this.doMove(this.steps[i]);
        }

        DroneDriving.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                DroneDataProcessing.getInstance().gridNetwork();
                DroneDriving.this.steps = null;
                MainActivity.getInstance().setText(DroneDriving.this.textViews.debugText, "Setting steps to null in drone driving");
                Thread.currentThread().interrupt();
            }
        });

    }

    public void driveOneMeterForward() {
        if (this.steps != null || !DroneRotation.getInstance().allowDriving()) {
            return;
        }
        Log.i(TAG, "Drive one meter");

        this.flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        this.flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);


        int length = (1000 / delay) + 2;
        double[] steps = new double[length];
        String str = "";
        for (int i = 0; i < steps.length - 2; i++) {
            steps[i] = 0.96; // 1 meter / second
            str += steps[i] + ", ";
        }
        steps[length - 2] = 0;
        steps[length - 1] = 0;

        MainActivity.getInstance().setText(DroneDriving.this.textViews.debugText, str + " length " + steps.length);
        this.steps = steps;

        DroneDriving.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

                Log.i(TAG, "SET VIRTUAL STICK TO TRUE");
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                Thread thread = new Thread(DroneDriving.this);
                thread.start();
            }
        });

    }

    public boolean allowRotation() {
        return this.steps == null;
    }

    private void doMove(double velocity) {
        this.flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        this.flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        DroneRotation.getInstance().sendMovementData(new FlightControlData(0f, (float) velocity, 0f, 0f));
//        DroneDriving.this.flightController.setVirtualStickAdvancedModeEnabled(true);
//        this.flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
//        this.flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
//        DroneDriving.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, (float) velocity, 0f, 0f), new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError djiError) {
//                if (djiError != null) {
//                    Log.w(TAG, "DJI ERROR IS" + djiError.getDescription());
//                }
//                Log.i(TAG, "ON RESULT IN DO Move " );
//            }
//        });
//        DroneDriving.this.flightController.setVirtualStickAdvancedModeEnabled(false);
    }

}