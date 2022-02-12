package com.example.drone_interactor;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
// import dji.common.flightcontroller.flightassistant.ObstacleActionMode;
import dji.common.flightcontroller.flightassistant.PerceptionInformation;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

@SuppressLint("SetTextI18n")
public class DroneDataProcessing {

    public ArrayList<DataPoint> dataPoints;

    public DataPoint currentPosition; // in meters distance from start

    public double currentAngle;

    private FlightController flightController;
    private static final String TAG = DroneDataProcessing.class.getName();
    private TextViews textViews;

    public DroneDataProcessing(TextViews textViews) {
        this.textViews = textViews;
        this.currentPosition = new DataPoint(0, 0, 0);
        this.dataPoints = new ArrayList<DataPoint>();
    }

    public void startPositionListener(Aircraft aircraft) {
        aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
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
                DroneDataProcessing.this.setCurrentAngle(flightControllerState.getAttitude().yaw);
            }
        });
    }

    //                MainActivity.getInstance().setText(textViews.currentHeight,
//                        "Current Height: " +
//                                (double)(round(flightControllerState.getUltrasonicHeightInMeters() * 100)) / 100);


    public void stopPositionListener(Aircraft aircraft) {
        aircraft.getFlightController().setStateCallback(null);
    }

    public void startSensorListener(Aircraft aircraft) {
        aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(new CommonCallbacks.CompletionCallbackWith<PerceptionInformation>() {
            @Override
            public void onSuccess(PerceptionInformation perceptionInformation) {
                // MainActivity.getInstance().setText(textViews.debugText, Arrays.toString(perceptionInformation.getDistances()));
                if (perceptionInformation.getDistances().length > 0 &&
                        perceptionInformation.getDistances().length >= 45) {
                    int forwardDistance = perceptionInformation.getDistances()[0];
                    int backwardDistance = perceptionInformation.getDistances()[45];
                    int downwardDistance = perceptionInformation.getDownwardObstacleDistance();
                    int upwardDistance = perceptionInformation.getUpwardObstacleDistance();
                    DroneDataProcessing.this.setNewDataPoint(forwardDistance, backwardDistance, upwardDistance, downwardDistance);
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                MainActivity.getInstance().setText(DroneDataProcessing.this.textViews.debugText, djiError.getDescription());
            }
        });
    }

    public void stopSensorListener(Aircraft aircraft) {
        aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(null);
    }

    public void setNewCurrentPosition(double xVelocity,
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

    public void setCurrentAngle(double yaw) {
        this.currentAngle = yaw;
        MainActivity.getInstance().setText(this.textViews.currentAngle,
                "Current angle: " + (double)(round(yaw * 1000) / 1000));
    }

    public void setDroneStatus(Boolean motorsOn) {
        MainActivity.getInstance().setText(this.textViews.motors,
                motorsOn ? "Motors are on" : "Motors are turned off");
    }

    /**
     *
     * @param forwardDistance in meters
     * @param backwardDistance in meters
     * @param upwardDistance in meters
     * @param downwardDistance in meters
     */
    public void setNewDataPoint(double forwardDistance, double backwardDistance,
                                double upwardDistance, double downwardDistance) {

        MainActivity.getInstance().setText(this.textViews.forwardDistance,
                "Forward distance: " + forwardDistance / 10 + " cm");
        MainActivity.getInstance().setText(this.textViews.backwardDistance,
                "Backward distance: " + backwardDistance / 10 + " cm");
        MainActivity.getInstance().setText(this.textViews.upwardDistance,
                "Upward distance: " + upwardDistance / 10 + " cm");
        MainActivity.getInstance().setText(this.textViews.downwardDistance,
                "Downward distance: " + downwardDistance / 10 + " cm");
        double angle = this.currentAngle;
        if (angle < 0) {
            angle = 180 - angle;
        }
        this.dataPoints = new ArrayList<DataPoint>();
        double forwardXPlace = this.currentPosition.getX() + forwardDistance * Math.cos(Math.toRadians(angle));
        double forwardYPlace = this.currentPosition.getY() + forwardDistance * Math.sin(Math.toRadians(angle));

        double backwardXPlace = this.currentPosition.getX() + backwardDistance * Math.cos(Math.toRadians(angle + 180d));
        double backwardYPlace = this.currentPosition.getY() + backwardDistance * Math.sin(Math.toRadians(angle + 180d));

        this.dataPoints.add(new DataPoint(forwardXPlace, forwardYPlace, this.currentPosition.getZ()));
        this.dataPoints.add(new DataPoint(backwardXPlace, backwardYPlace, this.currentPosition.getZ()));
        this.dataPoints.add(new DataPoint(this.currentPosition.getX(), this.currentPosition.getY(), this.currentPosition.getZ() + upwardDistance));
        this.dataPoints.add(new DataPoint(this.currentPosition.getX(), this.currentPosition.getY(), this.currentPosition.getZ() - downwardDistance));
        MainActivity.getInstance().setText(this.textViews.debugText, "Data points: " + Arrays.toString(this.dataPoints.toArray()));
    }


}
