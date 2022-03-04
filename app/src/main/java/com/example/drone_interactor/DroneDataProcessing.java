package com.example.drone_interactor;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
// import dji.common.flightcontroller.flightassistant.ObstacleActionMode;
import dji.common.flightcontroller.flightassistant.FaceAwareState;
import dji.common.flightcontroller.flightassistant.PerceptionInformation;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

@SuppressLint("SetTextI18n")
public class DroneDataProcessing {

    public ArrayList<DataPoint> dataPoints;

    public DataPoint currentPosition; // in meters distance from start

    public double currentAngle;
    public double height = 0;

    private FlightController flightController;
    private static final String TAG = DroneDataProcessing.class.getName();
    private TextViews textViews;
    private static DroneDataProcessing INSTANCE = null;
    private Aircraft aircraft = null;

    private boolean forwardOption;
    private boolean backwardOption;
    private boolean upwardOption;
    private boolean downwardOption;

    public static DroneDataProcessing getInstance() {
        if (DroneDataProcessing.INSTANCE == null) {
            DroneDataProcessing.INSTANCE = new DroneDataProcessing();
        }
        return DroneDataProcessing.INSTANCE;
    }

    public void setup(TextViews textViews, Aircraft aircraft) {
        this.textViews = textViews;
        this.currentPosition = new DataPoint(0, 0, 0);
        this.dataPoints = new ArrayList<DataPoint>();
        this.aircraft = aircraft;
    }
    private DroneDataProcessing() {
    }

    public void setIfForward(boolean b) {
        this.forwardOption = b;
    }

    public void setIfBackward(boolean b) {
        this.backwardOption = b;
    }

    public void setIfUpward(boolean b) {
        this.upwardOption = b;
    }

    public void setIfDownward(boolean b) {
        this.downwardOption = b;
    }

    private void startPositionListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
            private long millisecondsBefore = -1;

            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                if (this.millisecondsBefore != -1) {
                    // set new distance
                    long currentTime = currentTimeMillis();
                    DroneDataProcessing.this.setNewCurrentPosition(
                            flightControllerState.getVelocityX(),
                            flightControllerState.getVelocityY(),
                            flightControllerState.getVelocityZ(),
                            (currentTime - this.millisecondsBefore));
                }
                this.millisecondsBefore = currentTimeMillis();
                // set new data
                DroneDataProcessing.this.setDroneStatus(flightControllerState.areMotorsOn());
                DroneDataProcessing.this.setCurrentAngleAndHeight(flightControllerState.getAttitude().yaw, flightControllerState.getUltrasonicHeightInMeters());
            }
        });
    }

    private void stopPositionListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().setStateCallback(null);
    }

    private void startSensorListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(new CommonCallbacks.CompletionCallbackWith<PerceptionInformation>() {
            @Override
            public void onSuccess(PerceptionInformation perceptionInformation) {
                // MainActivity.getInstance().setText(textViews.debugText, Arrays.toString(perceptionInformation.getDistances()));
                int forwardDistance = 60000;
                int backwardDistance = 60000;
                if (perceptionInformation.getDistances().length > 0 &&
                        perceptionInformation.getDistances().length >= 45) {
                    forwardDistance = perceptionInformation.getDistances()[0];
                    backwardDistance = perceptionInformation.getDistances()[45];
                }
                int upwardDistance = perceptionInformation.getUpwardObstacleDistance();
                Log.e(TAG, Arrays.toString(perceptionInformation.getDistances()));
                Log.e(TAG, perceptionInformation.getAngleInterval() + ": ");
                DroneDataProcessing.this.setNewDataPoint(forwardDistance, backwardDistance, upwardDistance, perceptionInformation.getDistances(), perceptionInformation.getAngleInterval());
            }

            @Override
            public void onFailure(DJIError djiError) {
                MainActivity.getInstance().setText(DroneDataProcessing.this.textViews.debugText, djiError.getDescription());
            }
        });
    }

    private void stopSensorListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(null);
    }

    public void stopAll() {
        try {
            if (this.dataPoints.size() > 0) {
                this.sendData();
            }
            this.stopSensorListener();
            this.stopPositionListener();
        } catch (Exception e) {
            // do nothing, not connected to drone
        }
        this.dataPoints = new ArrayList<DataPoint>();
        this.currentPosition = new DataPoint(0, 0, 0);
        this.height = 0;
        ConnectionToServer.getInstance().reset();
        // disconnect
    }

    public void startAll() {
        this.startPositionListener();
        this.startSensorListener();
        // start sending data
    }

    public void pause() {
        // stop sending data
        try {
            if (this.dataPoints.size() > 0) {
                this.sendData();
            }
            this.stopSensorListener();
        } catch (Exception e) {
            // do nothing, no connection to drone
        }
    }

    private void setNewCurrentPosition(double xVelocity,
                                      double yVelocity,
                                      double zVelocity,
                                      double dtMillis) {
        double newX = this.currentPosition.getX() + xVelocity * (dtMillis / 1000);
        double newY = this.currentPosition.getY() + yVelocity * (dtMillis / 1000);
        double newZ = this.currentPosition.getZ() + zVelocity * (dtMillis / 1000);
        this.currentPosition.setData(newX, newY, newZ);

        MainActivity.getInstance().setText(this.textViews.distanceX, "x: " +
                (double)(round(newX * 100)) / 100);
        MainActivity.getInstance().setText(this.textViews.distanceY, "y: " +
                (double)(round(newY * 100)) / 100);
        MainActivity.getInstance().setText(this.textViews.distanceZ, "z: " +
                (double)(round(newZ * 100)) / 100);
    }

    private void setCurrentAngleAndHeight(double yaw, double height) {
        this.currentAngle = yaw;
        MainActivity.getInstance().setText(this.textViews.currentAngle,
                "Current angle: " + (double)(round(yaw * 1000) / 1000));
        this.height = height;
        MainActivity.getInstance().setText(this.textViews.downwardDistance,
                "Downward: " + Double.valueOf(round(height * 100)) / 100);
    }

    private void setDroneStatus(Boolean motorsOn) {
        MainActivity.getInstance().setText(this.textViews.motors,
                motorsOn ? "Motors are on" : "Motors are turned off");
    }

    /**
     *
     * @param forwardDistance in meters
     * @param backwardDistance in meters
     * @param upwardDistance in meters
     */
    private void setNewDataPoint(double forwardDistance, double backwardDistance,
                                double upwardDistance, int[] horizontalDistances, float angleDifference) {
        MainActivity.getInstance().setText(this.textViews.forwardDistance,
                "Forward: " + Double.valueOf(round(forwardDistance / 10)) / 100 + " m");
        MainActivity.getInstance().setText(this.textViews.backwardDistance,
                "Backward: " + Double.valueOf(round(backwardDistance / 10)) / 100 + " m");
        MainActivity.getInstance().setText(this.textViews.upwardDistance,
                "Upward: " + Double.valueOf(round(upwardDistance / 10)) / 100 + " m");
        double angle = this.currentAngle;
        // this.dataPoints = new ArrayList<DataPoint>();

        // horizontal data
        for (int i = 0; i < horizontalDistances.length; i++) {
            if (horizontalDistances[i] < 60000 && horizontalDistances[i] > 0) {
                if ((!this.forwardOption && (i < 22 || i > 67) ) || (!this.backwardOption && (i > 22 && i < 67))) {
                    continue;
                }
                double xPlace = this.currentPosition.getX() +
                        Double.valueOf(horizontalDistances[i]) / 1000 *
                        Math.cos(Math.toRadians(angle + i * angleDifference));
                double yPlace = this.currentPosition.getY() +
                        Double.valueOf(horizontalDistances[i]) / 1000 *
                        Math.sin(Math.toRadians(angle + i * angleDifference));
                this.dataPoints.add(new DataPoint(xPlace, yPlace, -this.currentPosition.getZ()));
            }
        }
        // upward data
        if (this.upwardOption && upwardDistance < 60000 && upwardDistance > 0) {
            this.dataPoints.add(new DataPoint(this.currentPosition.getX(), this.currentPosition.getY(), -this.currentPosition.getZ() + upwardDistance / 1000));
        }
        // downward data
        if (this.downwardOption && this.height != 0) {
            this.dataPoints.add(new DataPoint(this.currentPosition.getX(), this.currentPosition.getY(), -this.height - this.currentPosition.getZ())); // since Z is negative for higher height values
        }
        //MainActivity.getInstance().setText(this.textViews.debugText, "Data points: " + this.dataPoints.size() + " : " + Arrays.toString(this.dataPoints.toArray()));
        // send data points
        if (this.dataPoints.size() >= 100) {
            this.sendData();
        }
    }

    private void sendData() {
        DataPoint[] dataToSend = this.dataPoints.toArray(new DataPoint[0]);
        this.dataPoints = new ArrayList<DataPoint>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int numberSent = 0;
                try {
                    numberSent = ConnectionToServer.getInstance().sendMessage(dataToSend);
                    MainActivity.getInstance().setText(DroneDataProcessing.this.textViews.debugText, "Sent " + numberSent + " data points. ");
                } catch (IOException e) {
                    MainActivity.getInstance().setText(DroneDataProcessing.this.textViews.debugText, "ERROR: " + e);
                } catch (Exception e) {
                    MainActivity.getInstance().setText(DroneDataProcessing.this.textViews.debugText, "ERROR exception: " + e);
                }
            }
        });
        thread.start();
    }


//        double forwardXPlace = this.currentPosition.getX() + forwardDistance / 1000 * Math.cos(Math.toRadians(angle));
//        double forwardYPlace = this.currentPosition.getY() + forwardDistance / 1000 * Math.sin(Math.toRadians(angle));
//
//        double backwardXPlace = this.currentPosition.getX() + backwardDistance / 1000 * Math.cos(Math.toRadians(angle + 180d));
//        double backwardYPlace = this.currentPosition.getY() + backwardDistance / 1000 * Math.sin(Math.toRadians(angle + 180d));
//
//
//        if (forwardDistance < 60000) {
//            this.dataPoints.add(new DataPoint(forwardXPlace, forwardYPlace, this.currentPosition.getZ()));
//        }
//        if (backwardDistance < 60000) {
//            this.dataPoints.add(new DataPoint(backwardXPlace, backwardYPlace, this.currentPosition.getZ()));
//        }

}
