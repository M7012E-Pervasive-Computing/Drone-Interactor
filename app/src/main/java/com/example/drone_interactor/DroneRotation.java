package com.example.drone_interactor;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.YawControlMode;
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
    private Timer sendVirtualStickDataTimer;

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
        private int a = 0;
        @Override
        public void run() {
            this.a++;
            if (this.a == 6) {
                DroneRotation.this.flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "--");
                    }
                });
                DroneRotation.this.sendVirtualStickDataTimer.cancel();
                return;
            }
            DroneRotation.this.flightController.sendVirtualStickFlightControlData(new FlightControlData(0f, 0f, 20f, 0f), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "ROTATING TO 90 DEGREES.");

                }
            });
        }
    }

    public void rotateDrone() {

        MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, DroneRotation.this.flightController.isVirtualStickControlModeAvailable() + "-");
        DroneRotation.this.flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DroneRotation.this.flightController.setYawControlMode(YawControlMode.ANGLE);
                MainActivity.getInstance().setText(DroneRotation.this.textViews.debugText, "Calling rotation.");
                SendVirtualStickData sendVirtualStickData = new SendVirtualStickData();
                DroneRotation.this.sendVirtualStickDataTimer = new Timer();
                DroneRotation.this.sendVirtualStickDataTimer.schedule(sendVirtualStickData, 0, 200);
            }
        });

    }
}