package com.example.drone_interactor;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class ViewListeners extends AppCompatActivity {

    private static final String TAG = ViewListeners.class.getName();

    private final Button startButton;
    private final Button stopButton;
    private final EditText ipAndPort;
    private final Button pauseButton;
    private final Switch forwardOption;
    private final Switch backwardOption;
    private final Switch upwardOption;
    private final Switch downwardOption;
    private final Switch obstacleAvoidanceOption;

    protected String connectionString = "";

    public ViewListeners(Button startButton, Button stopButton, EditText ipAndPort, Button pauseButton,
                         Switch forwardOption, Switch backwardOption, Switch upwardOption, Switch downwardOption,
                         Switch obstacleAvoidanceOption) {
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.ipAndPort = ipAndPort;
        this.pauseButton = pauseButton;
        this.forwardOption = forwardOption;
        this.backwardOption = backwardOption;
        this.upwardOption = upwardOption;
        this.downwardOption = downwardOption;
        this.obstacleAvoidanceOption = obstacleAvoidanceOption;

        this.startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Clicked on START, " + ViewListeners.this.connectionString);
                ConnectionToServer.getInstance().setConnectionString(ViewListeners.this.connectionString);

                DroneDataProcessing droneDataProcessing = DroneDataProcessing.getInstance();
                droneDataProcessing.startAll();
            }
        });

        this.stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Clicked on STOP");
                DroneDataProcessing droneDataProcessing = DroneDataProcessing.getInstance();
                droneDataProcessing.stopAll();
            }
        });

        this.ipAndPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ViewListeners.this.connectionString = editable.toString();
                Log.i(TAG, "CHANGED TEXT IN BOX " + editable);
            }
        });

        this.pauseButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Log.i(TAG, "Clicked on PAUSE");
               DroneDataProcessing.getInstance().pause();
           }
        });

        this.forwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfForward(true);
        this.forwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfForward(isChecked);
            }
        });

        this.backwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfBackward(true);
        this.backwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfBackward(isChecked);
            }
        });

        this.upwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfUpward(true);
        this.upwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfUpward(isChecked);
            }
        });

        this.downwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfDownward(true);
        this.downwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfDownward(isChecked);
            }
        });

        this.obstacleAvoidanceOption.setChecked(true);
        this.obstacleAvoidanceOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.getInstance().setObsctacleAvoidence(isChecked);
            }
        });
    }
}
