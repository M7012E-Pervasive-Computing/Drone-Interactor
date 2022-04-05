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
import com.example.drone_interactor.DroneRotation;


/**
 * A class which contains all the UI listeners which is necessary for the app.
 */
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
    private final Button rotateLeft;

    protected String connectionString = "";

    /**
     * Constructor for a ViewListeners object, setting all the objects to the given parameters.
     * @param startButton start button
     * @param stopButton stop button
     * @param ipAndPort ip and port edit text
     * @param pauseButton pause button
     * @param forwardOption forward option switch
     * @param backwardOption backward option switch
     * @param upwardOption upward option switch
     * @param downwardOption downward option switch
     * @param obstacleAvoidanceOption obstacle avoidance option switch
     */
    public ViewListeners(Button startButton, Button stopButton, EditText ipAndPort, Button pauseButton,
                         Switch forwardOption, Switch backwardOption, Switch upwardOption, Switch downwardOption,
                         Switch obstacleAvoidanceOption, Button rotateLeft) {
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.ipAndPort = ipAndPort;
        this.pauseButton = pauseButton;
        this.forwardOption = forwardOption;
        this.backwardOption = backwardOption;
        this.upwardOption = upwardOption;
        this.downwardOption = downwardOption;
        this.obstacleAvoidanceOption = obstacleAvoidanceOption;
        this.rotateLeft = rotateLeft;

        // start the listener for the button start. When the button is pressed, the listener will
        // start the DroneDataProcessing, and adds a connectionString to the ConnectionToServer
        this.startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Clicked on START, " + ViewListeners.this.connectionString);
                ConnectionToServer.getInstance().setConnectionString(ViewListeners.this.connectionString);

                DroneDataProcessing droneDataProcessing = DroneDataProcessing.getInstance();
                droneDataProcessing.startAll();
            }
        });

        // start the listener for the button stop. When the button is pressed, the listener will
        // stop the DroneDataProcessing
        this.stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Clicked on STOP");
                DroneDataProcessing droneDataProcessing = DroneDataProcessing.getInstance();
                droneDataProcessing.stopAll();
            }
        });

        // starts the listener for the edit text ip and port. When the text is changed, the listener
        // will set the connectionString to the new value
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

        // start the listener for the button pause. When the button is pressed, the listener will
        // pause the DroneDataProcessing
        this.pauseButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Log.i(TAG, "Clicked on PAUSE");
               DroneDataProcessing.getInstance().pause();
           }
        });

        this.rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked on ROTATE LEFT");
                DroneRotation.getInstance().rotateDrone();
            }
        });

        // start the listener for the forward option switch. When the switch is changed, the listener
        // will set the forward option to the new value in DroneDataProcessing
        this.forwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfForward(true);
        this.forwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfForward(isChecked);
            }
        });

        // start the listener for the backward option switch. When the switch is changed, the listener
        // will set the backward option to the new value in DroneDataProcessing
        this.backwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfBackward(true);
        this.backwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfBackward(isChecked);
            }
        });

        // start the listener for the upward option switch. When the switch is changed, the listener
        // will set the upward option to the new value in DroneDataProcessing
        this.upwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfUpward(true);
        this.upwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfUpward(isChecked);
            }
        });

        // start the listener for the downward option switch. When the switch is changed, the listener
        // will set the downward option to the new value in DroneDataProcessing
        this.downwardOption.setChecked(true);
        DroneDataProcessing.getInstance().setIfDownward(true);
        this.downwardOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DroneDataProcessing.getInstance().setIfDownward(isChecked);
            }
        });

        // start the listener for the obstacle avoidance option switch. When the switch is changed, the listener
        // will set the obstacle avoidance option to the new value in MainActivity
        this.obstacleAvoidanceOption.setChecked(true);
        this.obstacleAvoidanceOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.getInstance().setObstacleAvoidance(isChecked);
            }
        });

    }
}
