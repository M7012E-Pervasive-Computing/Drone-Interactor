package com.example.drone_interactor;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

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

    public DataPoint[] dataPoints;

    public DataPoint currentPosition; // in meters distance from start

    public double currentAngle;

    private FlightController flightController;
    private static final String TAG = DroneDataProcessing.class.getName();
    private TextViews textViews;

    public DroneDataProcessing(TextViews textViews) {
        this.textViews = textViews;
        this.currentPosition = new DataPoint(0, 0, 0);
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
                    MainActivity.getInstance().setText(textViews.forwardDistance,
                            "Forward distance: " +
                                Double.valueOf(perceptionInformation.getDistances()[0]) / 1000);
                    MainActivity.getInstance().setText(textViews.backwardDistance,
                            "Backward distance: " +
                                Double.valueOf(perceptionInformation.getDistances()[45]) / 1000);
                }
                MainActivity.getInstance().setText(textViews.upwardDistance, "Upward distance: " + Double.valueOf(perceptionInformation.getUpwardObstacleDistance()) / 1000);
//                if (index > 0) {
//                    int[] distances = perceptionInformation.getDistances();
//                    if (distances.length >= index - 1) {
//                        MainActivity.getInstance().setText(textViews[7], "METERS FORWARD: " + perceptionInformation.getDistances()[perceptionInformation.getDataPackageIndex() - 2]);
//                    } else {
//                        MainActivity.getInstance().setText(textViews[0], "INDEX length is not larger " + distances.length + " : " + index);
//                    }
//                } else {
//                    MainActivity.getInstance().setText(textViews[0], "INDEX < 0" + index);
//                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }

    public void test(Aircraft aircraft) {
//        aircraft.getFlightController().getFlightAssistant().getVisualObstaclesAvoidanceDistance(PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Upward, new CommonCallbacks.CompletionCallbackWith<Float>() {
//            @Override
//            public void onSuccess(Float aFloat) {
//                MainActivity.getInstance().setText(textViews[0], "SUCCESS FLOAT: " + aFloat);
//            }
//
//            @Override
//            public void onFailure(DJIError djiError) {
//                MainActivity.getInstance().setText(textViews[0], "ERROR FLOAT: " + djiError.toString());
//            }
//        });
        aircraft.getFlightController().getFlightAssistant().setVisualPerceptionInformationCallback(new CommonCallbacks.CompletionCallbackWith<PerceptionInformation>() {
            @Override
            public void onSuccess(PerceptionInformation perceptionInformation) {
                // MainActivity.getInstance().setText(textViews[7], "METERS UP: " + perceptionInformation.getUpwardObstacleDistance());
                // MainActivity.getInstance().setText(textViews[0], "INDEX: " + perceptionInformation.getDataPackageIndex());
                MainActivity.getInstance().setText(textViews.debugText, Arrays.toString(perceptionInformation.getDistances()));
                int index = perceptionInformation.getDataPackageIndex();
                if (perceptionInformation.getDistances().length > 0 && perceptionInformation.getDistances().length >= 89) {
                    MainActivity.getInstance().setText(textViews.forwardDistance, "Forward distance: " + Double.valueOf(perceptionInformation.getDistances()[0]) / 1000);
                    MainActivity.getInstance().setText(textViews.backwardDistance, "Backward distance: " + Double.valueOf(perceptionInformation.getDistances()[45]) / 1000);
                }
                MainActivity.getInstance().setText(textViews.upwardDistance, "Upward distance: " + Double.valueOf(perceptionInformation.getUpwardObstacleDistance()) / 1000);
//                if (index > 0) {
//                    int[] distances = perceptionInformation.getDistances();
//                    if (distances.length >= index - 1) {
//                        MainActivity.getInstance().setText(textViews[7], "METERS FORWARD: " + perceptionInformation.getDistances()[perceptionInformation.getDataPackageIndex() - 2]);
//                    } else {
//                        MainActivity.getInstance().setText(textViews[0], "INDEX length is not larger " + distances.length + " : " + index);
//                    }
//                } else {
//                    MainActivity.getInstance().setText(textViews[0], "INDEX < 0" + index);
//                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
//        aircraft.getFlightController().getFlightAssistant().setVisionDetectionStateUpdatedCallback(new VisionDetectionState.Callback()  {
//            @Override
//            public void onUpdate(@NonNull VisionDetectionState visionDetectionState) {
//                // MainActivity.getInstance().setText(textViews[0], visionDetectionState.getObstacleDistanceInMeters() + "----------");
//                if (visionDetectionState.getDetectionSectors() != null) {
//                    MainActivity.getInstance().setText(textViews[0], visionDetectionState.getDetectionSectors().length + "length");
//                } else {
//                    MainActivity.getInstance().setText(textViews[0], "null - length");
//                }
//
//                MainActivity.getInstance().setText(textViews[7], "METERS: " + visionDetectionState.getSystemWarning());
////                ObstacleDetectionSector[] sensors = visionDetectionState.getDetectionSectors();
////                if (sensors.length > 0) {
////                    if (sensors[0].getObstacleDistanceInMeters() > 0) {
////                        MainActivity.getInstance().setText(textViews[7], "Forward distancess: " + sensors[0].getObstacleDistanceInMeters());
////                    } else {
////                        MainActivity.getInstance().setText(textViews[7], "length is 0");
////                    }
////
////                } else {
////                    MainActivity.getInstance().setText(textViews[7], "length 0");
////                }
//            }
//        });

//        Radar radar = aircraft.getRadar();
//        if (radar != null) {
//            Log.e(TAG, "RADAR: " + radar);
//            Log.e(TAG, "RADAR CONNECTED: " + radar.isConnected());
//            //textViews[0].setText("RADAR NOT NULL");
//            MainActivity.getInstance().setText(textViews[0], "RADAR NOT NULL");
//        } else {
//            // textViews[0].setText("RADAR NULL");
//            MainActivity.getInstance().setText(textViews[0], "RADAR NULL");
//        }

        // Log.e(TAG, "RADAR: " + radar);
        // Log.e(TAG, "RADAR CONNECTED: " + radar.isConnected());
        // radar.setHorizontalRadarObstacleAvoidanceEnabled(true, djiError -> Log.e(TAG, "ON RESULT HORIZONTAL RADAR" + djiError.toString()));

        aircraft.getFlightController().setStateCallback(new FlightControllerState.Callback() {
            private long milisecondsBefore = -1;

            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                if (this.milisecondsBefore != -1) {
                    // set new distance
                    long currentTime = currentTimeMillis();
                    DroneDataProcessing.this.setNewCurrentPosition(
                            flightControllerState.getVelocityX(),
                            flightControllerState.getVelocityY(),
                            flightControllerState.getVelocityZ(),
                            (currentTime - this.milisecondsBefore));
                }
                this.milisecondsBefore = currentTimeMillis();
                // set new data
                DroneDataProcessing.this.setDroneStatus(flightControllerState.areMotorsOn());
                DroneDataProcessing.this.setCurrentAngle(flightControllerState.getAttitude().yaw);
            }
        });
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
                "Forward distance: " + forwardDistance + " m");
        MainActivity.getInstance().setText(this.textViews.backwardDistance,
                "Backward distance: " + backwardDistance + " m");
        MainActivity.getInstance().setText(this.textViews.upwardDistance,
                "Upward distance: " + upwardDistance + " m");
        MainActivity.getInstance().setText(this.textViews.downwardDistance,
                "Downward distance: " + downwardDistance + " m");
    }


}
