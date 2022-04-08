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
import dji.common.flightcontroller.flightassistant.FaceAwareState;
import dji.common.flightcontroller.flightassistant.PerceptionInformation;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

@SuppressLint("SetTextI18n")

/**
 * 
 * A singleton class which fetches and handles data from the drone.
 * Updates the position of the drone, and reads sensor data.
 * 
 * It is a singleton class as there should only be one instance of
 * the class which handles data.
 */
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

    /**
     * 
     * Returns the instance of the class. Creates a new one
     * if no instance exists.
     * 
     * @return The instance of the ConnectionToServer class.
     */
    public static DroneDataProcessing getInstance() {
        if (DroneDataProcessing.INSTANCE == null) {
            DroneDataProcessing.INSTANCE = new DroneDataProcessing();
        }
        return DroneDataProcessing.INSTANCE;
    }

    /**
     * 
     * Initializes data processing.
     * 
     * @param textViews The text fields in the app.
     * @param aircraft The aircraft that we are connected to.
     */
    public void setup(TextViews textViews, Aircraft aircraft) {
        this.textViews = textViews;
        this.currentPosition = new DataPoint(0, 0, 0);
        this.dataPoints = new ArrayList<DataPoint>();
        this.aircraft = aircraft;
    }

    /**
     * private constructor to prevent initialization.
     */
    private DroneDataProcessing() {}

    /**
     * 
     * Activates or disables the forward sensor. 
     * Called via a button on the app.
     * 
     * @param b Whether the forward sensor should be on.
     */
    public void setIfForward(boolean b) {
        this.forwardOption = b;
    }

    /**
     * 
     * Activates or disables the backward sensor.
     * Called via a button on the app.
     * 
     * @param b Whether the backward sensor should be on.
     */
    public void setIfBackward(boolean b) {
        this.backwardOption = b;
    }

    /**
     * 
     * Activates or disables the upward sensor.
     * Called via a button on the app.
     * 
     * @param b Whether the upward sensor should be on.
     */
    public void setIfUpward(boolean b) {
        this.upwardOption = b;
    }

    /**
     * 
     * Activates or disables the downward sensor.
     * Called via a button on the app.
     * 
     * @param b Whether the downward sensor should be on.
     */
    public void setIfDownward(boolean b) {
        this.downwardOption = b;
    }

    /**
     * Starts the updating of the position of the drone via a callback function on the 
     * aircraft's flight controller. This callback function will be run
     * every time that the drone updates its velocity.
     * Standard value is 10 times a second.
     */
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

    /**
     * Stops updating the position of the drone. 
     */
    private void stopPositionListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().setStateCallback(null);
    }

    /**
     * Starts reading sensor data via a callback function on the 
     * aircraft's flight assistant. This callback function will be run
     * every time that the drone updates its sensor data.
     * Standard value is every 0.4 seconds.
     */
    private void startSensorListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(new CommonCallbacks.CompletionCallbackWith<PerceptionInformation>() {
            @Override
            public void onSuccess(PerceptionInformation perceptionInformation) {
                // 60000 is the sensor's default value when not sensing anything.
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

    /**
     * Stops reading sensor values.
     */
    private void stopSensorListener() {
        if (this.aircraft == null) {
            return;
        }
        this.aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(null);
    }

    /**
     * Stops the data processing and resets all variables associated with processing.
     * Sends the data stored in the cache before doing so.
     * 
     * Called via a button on the app.
     */
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
        // disconnect
        ConnectionToServer.getInstance().reset();
    }

    /**
     * Starts the data processing.
     * 
     * Called via a button on the app.
     */
    public void startAll() {
        this.startPositionListener();
        this.startSensorListener();
        // start sending data
    }

    /**
     * Pauses creating sensor data, but keeps updating the drone's position.
     * Called via a button on the app.
     */
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

    /**
     * 
     * Sets the current position of the drone, calculates based on time and velocity.
     * Also prints the new position to the app.
     * 
     * s = s_0 + v * t
     * 
     * @param xVelocity The velocity in the x-axis, in meters per second.
     * @param yVelocity The velocity in the y-axis, in meters per second.
     * @param zVelocity The velocity in the z-axis, in meters per second.
     * @param dtMillis The time difference, in milliseconds.
     */
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

    /**
     * 
     * Sets the yaw (rotation around the y-axis) and the height of the drone. 
     * 
     * @param yaw The yaw of the drone, in degrees.
     * @param height The height of the drone, in meters.
     */
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
     * Takes data from the drone's sensors and creates an absolute xyz coordinate from that
     * data and the drone's position.
     * 
     * @param forwardDistance The sensed distance from the forward sensor, in meters.
     * @param backwardDistance The sensed distance from the backward sensor, in meters.
     * @param upwardDistance The sensed distance from the upward sensor, in meters.
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

        // horizontal data
        for (int i = 0; i < horizontalDistances.length; i++) {
            if (horizontalDistances[i] < 60000 && horizontalDistances[i] > 0) {
                /** 
                 * The horizontalDistances array stores sensed points from the sensor in a cone, with a 
                 * 4 degree difference between each element. With the if clause below we can specify that
                 * we want the points in a 90 degree cone in front or to the back of the drone.
                 */
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
        // sends data points only if there is a 100 or more points in the cache.
        if (this.dataPoints.size() >= 100) {
            this.sendData();
        }
    }

    /**
     * Sends data via a threaded solution. 
     * 
     * This is threaded because android requires HTTP requests be sent on
     * another thread than the main one. 
     */
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

}
