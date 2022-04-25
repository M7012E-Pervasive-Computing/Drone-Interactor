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
    private int lengthOfSteps;

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
        // this.startYawListener();
        this.flightController.setYawControlMode(YawControlMode.ANGLE);
        this.flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
    }

    @Override
    public void run() {

        if (this.slowRotateMode) {
            //if (i % length == 0) {
            //    1000
            //}
            for (int i = 0; i < this.steps.length; i++) {
                // rotate
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, e.getMessage());
                    return;
                }
                this.doRotate(this.steps[i]);

                // check if we should sleep 1000 ms
                if (i % this.lengthOfSteps == 0) {
                    DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
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
                    DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
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
                this.doRotate(this.steps[i]);
            }
        }

        DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
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

    private double getAngle(double currentAngle, double rotateAngle) {
         double yawRotateTo = (currentAngle + rotateAngle) % 360;
         if (yawRotateTo <= -180) {
            yawRotateTo += 360;
         }
         if (yawRotateTo > 180) {
             yawRotateTo -= 360;
         }
         return yawRotateTo;
//        double yaw = 180 + angle;
//        double yawRotateTo = yaw + rotateAngle;
//        yawRotateTo = yawRotateTo % 360;
//        return yawRotateTo - 180;
    }

    public void rotateDrone(int rotateAngle) {
        if (this.steps != null && !DroneDriving.getInstance().allowRotation()) {
            return;
        }
        double yaw = this.currentAngle;
        int length = 10;
        if (Math.abs(rotateAngle) < 50) {
            length = 6;
        } else if (Math.abs(rotateAngle) > 130) {
            length = 14;
        }
        double[] steps = new double[length]; // 10 is best for 90 degrees
        double rotateTo = this.getAngle(yaw, rotateAngle);
        String str = "";
        for (int i = 0; i < steps.length; i++) {
            steps[i] = rotateTo;
            str += steps[i] + ", ";
        }

//        double[] steps = new double[(Math.abs(rotateAngle) / degreeSegments) + 1]; // degree segments
//        String str = "";
//        for (int i = 0; i < steps.length; i++) {
//            steps[i] = this.getAngle(yaw, (i + 1) * degreeSegments * multiplier);
//            str += steps[i] + ", ";
//        }

        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, str + " length " + steps.length);
        this.steps = steps;

        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                Thread thread = new Thread(DroneRotation.this);
                thread.start();
            }
        });
    }

    public boolean allowDriving() {
        return this.steps == null;
    }

    private void doRotate(double degree) {
        this.flightController.setYawControlMode(YawControlMode.ANGLE);
        DroneRotation.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, 0f, (float) degree, 0f), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {  }
        });
    }

//    public void startYawListener() {
//        this.aircraft.getFlightController().setStateCallback(flightControllerState -> DroneRotation.this.currentAngle = flightControllerState.getAttitude().yaw);
//    }

    public void setYaw(double yaw) {
        this.currentAngle = yaw;
    }
    
    public void slowRotate360() {
        if (this.steps != null && !DroneDriving.getInstance().allowRotation()) {
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
//            currAngle = currAngle + 30;
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
                // MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "STARTING ON SLOW THREAD");
                Thread thread = new Thread(DroneRotation.this);
                thread.start();
            }
        });

    }
}