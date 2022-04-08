package com.example.drone_interactor;

import androidx.annotation.NonNull;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.products.Aircraft;
import dji.sdk.flightcontroller.FlightController;
import com.example.drone_interactor.MainActivity;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class DroneRotation implements Runnable {

    private TextViews textViews;
    private Aircraft aircraft;
    private FlightController flightController;
    private static DroneRotation INSTANCE;
    private Timer sendVirtualStickDataTimer;
    private double currentAngle;

    private final int STANDARD_STEP = 1;
    private final int NUM_OF_ITERATIONS_EACH = 3;

    private double[] steps;
    private int delay = 40;

    public static DroneRotation getInstance() {
        if (DroneRotation.INSTANCE == null) {
            DroneRotation.INSTANCE = new DroneRotation();
        }
        return DroneRotation.INSTANCE;
    }

    private DroneRotation() { }

    public void setAircraft(Aircraft aircraft, TextViews textviews) {
        this.aircraft = aircraft;
        this.flightController = this.aircraft.getFlightController();
        this.textViews = textviews;
        this.startYawListener();
        this.flightController.setYawControlMode(YawControlMode.ANGLE);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
    }

    @Override
    public void run() {
        for (int i = 0; i < this.steps.length; i++) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                return;
            }
            this.doRotate(this.steps[i]);
        }

        DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
            }
        });
        this.steps = null;
    }

//    private class SendVirtualStickData extends TimerTask {
//        private int a = 0;
//        @Override
//        public void run() {
//            this.a++;
//            if (this.a == 6) {
//                DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
//                    @Override
//                    public void onResult(DJIError djiError) {
//                        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
//                    }
//                });
//                DroneRotation.this.sendVirtualStickDataTimer.cancel();
//                return;
//            }
//            DroneRotation.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, 0f, 20f, 0f), new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onResult(DJIError djiError) {
//                    MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "ROTATING TO 90 DEGREES.");
//
//                }
//            });
//        }
//    }

    private double getAngle(double angle, double rotateAngle) {
        double yaw = 180 + angle;
        double yawRotateTo = yaw + rotateAngle;
        yawRotateTo = yawRotateTo % 360;
        return yawRotateTo - 180;
    }

    public void rotateDrone(int rotateAngle) {
        rotateDrone(rotateAngle, this.STANDARD_STEP);
    }

    public void rotateDrone(int rotateAngle, int degreeSegments) {
        if (this.steps != null) {
            return;
        }
        int multiplier = 1;
        if (rotateAngle < 0) {
            multiplier = -1;
        }
        double yaw = this.currentAngle;
        double[] steps = new double[Math.abs(rotateAngle) / degreeSegments + 1]; // degree segments
        String str = "";
        for (int i = 0; i < steps.length; i++) {
            steps[i] = this.getAngle(yaw, (i + 1) * degreeSegments * multiplier);
            str += steps[i] + ", ";
        }

        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, str);

        this.steps = steps;

        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                Thread thread = new Thread(DroneRotation.this);
                thread.start();
            }
        });


//        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "-");
//        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError djiError) {
//                DroneRotation.this.flightController.setYawControlMode(YawControlMode.ANGLE);
//                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "Calling rotation.");
//                SendVirtualStickData sendVirtualStickData = new SendVirtualStickData();
//                DroneRotation.this.sendVirtualStickDataTimer = new Timer();
//                DroneRotation.this.sendVirtualStickDataTimer.schedule(sendVirtualStickData, 0, 200);
//            }
//        });
    }

    private void doRotate(double degree) {
        DroneRotation.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, 0f, (float) degree, 0f), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }

    public void startYawListener() {
        this.aircraft.getFlightController().setStateCallback(flightControllerState -> DroneRotation.this.currentAngle = flightControllerState.getAttitude().yaw);
    }
}