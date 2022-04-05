package com.example.drone_interactor;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.util.CommonCallbacks;
import dji.sdk.products.Aircraft;
import dji.sdk.flightcontroller.FlightController;
import com.example.drone_interactor.MainActivity;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class DroneRotation {

    private TextViews textViews;
    private Aircraft aircraft;
    private FlightController flightController;
    private static DroneRotation INSTANCE;

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

    }

    private class SendVirtualStickData extends TimerTask {

        @Override
        public void run() {
            DroneRotation.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, 0f, 90f, 0f), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "ROTATING TO 90 DEGREES.");
                    DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
                }
            });
        }
    }

    public void rotateDrone() {
        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "Calling rotation.");
                SendVirtualStickData sendVirtualStickData = new SendVirtualStickData();
                Timer sendVirtualStickDataTimer = new Timer();
                sendVirtualStickDataTimer.schedule(sendVirtualStickData, 100, 100);
            }
        });
    }
}