package com.example.drone_interactor;

import android.util.Log;

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
    private int lengthOfSteps;

    private static final String TAG = DroneRotation.class.getName();

    private final int STANDARD_STEP = 2;
    private final int NUM_OF_ITERATIONS_EACH = 3;

    private boolean slowRotateMode = false;
    private double[] steps;
    private int delay = 100; // between 40 and 200, 100 is best

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
        this.flightController.setYawControlMode(YawControlMode.ANGLE);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
    }

    @Override
    public void run() {

        if (this.slowRotateMode) {
            for (int i = 0; i < this.steps.length; i++) {
                // rotate
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                this.doRotate(this.steps[i]);

                // check if we should sleep 1000 ms
                if (i % this.lengthOfSteps == 0) {
                    DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) { }
                    });
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) { }
                    });
                }
            }
            this.slowRotateMode = false;
            DroneDataProcessing.getInstance().pause();
        } else {
            Log.i(TAG, "GETS TO RUN");
            Log.d(TAG, "CURRENT ANGLE: " + this.currentAngle);
            for (int i = 0; i < this.steps.length; i++) {
                try {
                    Log.i(TAG, "INSIDE LOOP: " + i);
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                Log.i(TAG, "DOING ROTATE");
                Log.d(TAG, "ROTATING TO: " + this.steps[i]);
                this.doRotate(this.steps[i]);
            }
        }

        Log.i(TAG, "SETTING FALSE");
        DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                Log.i(TAG, "SETTING NULL");
                DroneRotation.this.steps = null;
                Log.i(TAG, "STEPS IS " + DroneRotation.this.steps);
                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "Setting steps to null in drone rotation");
                Thread.currentThread().interrupt();
            }
        });
    }

    private double getAngle(double currentAngle, double rotateAngle) {
         double yawRotateTo = (currentAngle + rotateAngle) % 360;
         if (yawRotateTo <= -180) {
            yawRotateTo += 360;
         }
         if (yawRotateTo > 180) {
             yawRotateTo -= 360;
         }
         return yawRotateTo;
    }

    public void rotateDrone(int rotateAngle) {
        if (this.steps != null || !DroneDriving.getInstance().allowRotation()) {
            Log.i(TAG, "Returning");
            return;
        }

        this.flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        DroneRotation.this.flightController.setVirtualStickAdvancedModeEnabled(true);
        DroneRotation.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, 0f, 0f, 0f), null);
        DroneRotation.this.flightController.setVirtualStickAdvancedModeEnabled(false);
        this.flightController.setYawControlMode(YawControlMode.ANGLE);

        double yaw = this.currentAngle;
        int length = 10;
        if (Math.abs(rotateAngle) < 50) {
            length = 6;
        } else if (Math.abs(rotateAngle) > 130) {
            length = 14;
        }
        Log.i(TAG, "length" + length);

        double[] steps = new double[length]; // 10 is best for 90 degrees
        double rotateTo = this.getAngle(yaw, rotateAngle);
        String str = "";
        for (int i = 0; i < steps.length; i++) {
            steps[i] = rotateTo;
            str += steps[i] + ", ";
        }

        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, str + " length " + steps.length);
        this.steps = steps;

        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    Log.i(TAG, "ERROR IS " + djiError.getDescription());
                }

                Log.i(TAG, "SET VIRTUAL STICK TO TRUE");
                Thread thread = new Thread(DroneRotation.this);
                thread.start();
            }
        });
    }

    public boolean allowDriving() {
        return this.steps == null;
    }

    private void doRotate(double degree) {
        this.sendMovementData(new FlightControlData(0f, 0f, (float) degree, 0f));
    }

    public void sendMovementData(FlightControlData data) {
        DroneRotation.getInstance().flightController.setVirtualStickAdvancedModeEnabled(true);
        DroneRotation.this.flightController.sendVirtualStickFlightControlData(data, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    Log.w(TAG, "DJI ERROR IS" + djiError.getDescription());
                }
                Log.i(TAG, "ON RESULT IN DO ROTATE " );
            }
        });
        DroneRotation.getInstance().flightController.setVirtualStickAdvancedModeEnabled(false);

    }

    public void setYaw(double yaw) {
        this.currentAngle = yaw;
    }
    
    public void slowRotate360() {
        if (this.steps != null || !DroneDriving.getInstance().allowRotation()) {
            return;
        }
        DroneDataProcessing.getInstance().startAll();
        int degreesChanges = 20;
        this.lengthOfSteps = 5;
        this.slowRotateMode = true;
        this.steps = new double[this.lengthOfSteps * ((360 / degreesChanges))]; // 360 / 30 = 36 / 3
        double currAngle = this.currentAngle;
        int index = 0;
        String str = "";
        for (int rotateTo = degreesChanges; rotateTo <= 360; rotateTo += degreesChanges) {
            for (int i = 0; i < this.lengthOfSteps; i++) {
                this.steps[index] = getAngle(currAngle, rotateTo);
                str += steps[index] + ", ";
                index++;
            }
        }

        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "length is: " + this.steps.length + "\n" + str);

        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {

            @Override
            public void onResult(DJIError djiError) {
                Thread thread = new Thread(DroneRotation.this);
                thread.start();
            }
        });
    }
}
