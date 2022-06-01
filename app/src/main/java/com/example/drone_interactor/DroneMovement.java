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

public class DroneMovement implements Runnable {

    private boolean shouldRotate;

    private TextViews textViews;
    private Aircraft aircraft;
    private FlightController flightController;
    private static DroneMovement INSTANCE;
    private double currentAngle;
    private int lengthOfSteps;
    private boolean lastWasRotation = false;
    private boolean callOnSlowRotate = false;

    private static final String TAG = DroneMovement.class.getName();

    private boolean slowRotateMode = false;
    private double[] steps;
    private int delay = 100; // between 40 and 200, 100 is best


    public static DroneMovement getInstance() {
        if (DroneMovement.INSTANCE == null) {
            DroneMovement.INSTANCE = new DroneMovement();
        }
        return DroneMovement.INSTANCE;
    }

    public void setAircraft(Aircraft aircraft, TextViews textviews) {
        this.aircraft = aircraft;
        this.flightController = this.aircraft.getFlightController();
        this.textViews = textviews;
        this.flightController.setVirtualStickAdvancedModeEnabled(true);
        // this.flightController.setYawControlMode(YawControlMode.ANGLE);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        // this.rotateDrone(0);
    }

    private DroneMovement() { }

    @Override
    public void run() {
        if (this.shouldRotate) {
            if (this.slowRotateMode) {
                for (int i = 0; i < this.steps.length; i++) {
                    // rotate
                    try {
                        Thread.sleep(this.delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                        return;
                    }
                    this.sendMovementData(new FlightControlData(0f, 0f, (float) this.steps[i], 0f));

                    // check if we should sleep 1500 ms
                    if (i % this.lengthOfSteps == 0) {
                        this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                            }
                        });
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                            return;
                        }
                        this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                            }
                        });
                    }
                }
                this.slowRotateMode = false;
                DroneDataProcessing.getInstance().pause();

            } else {
                for (int i = 0; i < this.steps.length; i++) {
                    try {
                        Thread.sleep(this.delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                        return;
                    }
                    this.sendMovementData(new FlightControlData(0f, 0f, (float) this.steps[i], 0f));
                }
                if (this.callOnSlowRotate) {
                    this.callOnSlowRotate = false;
                    DroneMovement.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                            DroneMovement.this.steps = null;
                            DroneMovement.this.slowRotate360();
                            MainActivity.getInstance().setText(DroneMovement.this.textViews.debugText, "Setting steps to null in drone rotation");
                        }
                    });
                    return;
                }
            }
        } else {
            for (int i = 0; i < this.steps.length; i++) {
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                    return;
                }
                this.sendMovementData(new FlightControlData(0f, (float) this.steps[i], 0f, 0f));
            }
            DroneDataProcessing.getInstance().gridNetwork();
        }


        DroneMovement.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                DroneMovement.this.steps = null;
                MainActivity.getInstance().setText(DroneMovement.this.textViews.debugText, "Setting steps to null in drone rotation");
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

    public void driveOneMeterForward() {
        if (this.steps != null) {
            return;
        }
        this.shouldRotate = false;
        this.flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        this.flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        this.flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        int addingInt = 6;
        int length = (1000 / delay) + addingInt * 2;
        double[] steps = new double[length];
        String str = "";
        for (int i = 0; i < steps.length; i++) {
            if (i < addingInt || i >= steps.length - addingInt) {
                steps[i] = 0;
                str += steps[i] + ", ";
                continue;
            }
            steps[i] = 0.96; // 1 meter / second
            str += steps[i] + ", ";
        }

        MainActivity.getInstance().setText(this.textViews.debugText, str + " length " + steps.length);
        this.steps = steps;

        this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

                Log.i(TAG, "SET VIRTUAL STICK TO TRUE");
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                Thread thread = new Thread(DroneMovement.this);
                thread.start();
            }
        });

    }

    public void rotateDrone(int rotateAngle) {
        if (this.steps != null) {
            return;
        }
        this.lastWasRotation = true;
        this.shouldRotate = true;
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        this.flightController.setYawControlMode(YawControlMode.ANGLE);

        double yaw = this.currentAngle;
        int length = 17;
        if (Math.abs(rotateAngle) < 50) {
            length = 10;
        } else if (Math.abs(rotateAngle) > 130) {
            length = 22;
        }
        Log.i(TAG, "length" + length);

        double[] steps = new double[length]; // 10 is best for 90 degrees
        double rotateTo = this.getAngle(yaw, rotateAngle);
        String str = "";
        for (int i = 0; i < steps.length; i++) {
            steps[i] = rotateTo;
            str += steps[i] + ", ";
        }

        MainActivity.getInstance().setText(DroneMovement.this.textViews.debugText, str + " length " + steps.length);
        this.steps = steps;

        DroneMovement.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    Log.i(TAG, "ERROR IS " + djiError.getDescription());
                }

                Log.i(TAG, "SET VIRTUAL STICK TO TRUE");
                // MainActivity.getInstance().setText(DroneMovement.this.textViews.debugText, DroneMovement.this.flightController.isVirtualStickControlModeAvailable() + "--");
                Thread thread = new Thread(DroneMovement.this);
                thread.start();
            }
        });
    }

    public void setYaw(double yaw) {
        this.currentAngle = yaw;
    }

    public void slowRotate360() {
        if (this.steps != null) {
            return;
        }
        if (!this.lastWasRotation) {
            this.callOnSlowRotate = true;
            this.rotateDrone(0);
        }
        this.lastWasRotation = true;
        this.shouldRotate = true;
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

        MainActivity.getInstance().setText(this.textViews.debugText, "length is: " + this.steps.length + "\n" + str);


        this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "STARTING ON SLOW THREAD");
                Thread thread = new Thread(DroneMovement.this);
                thread.start();
            }
        });
    }


    private void sendMovementData(FlightControlData data) {
        this.flightController.sendVirtualStickFlightControlData(data, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    Log.w(TAG, "DJI ERROR IS" + djiError.getDescription());
                }
            }
        });
    }


}
