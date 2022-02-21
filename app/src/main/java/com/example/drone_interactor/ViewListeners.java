package com.example.drone_interactor;

import android.R;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ViewListeners extends AppCompatActivity {

    private static final String TAG = ViewListeners.class.getName();

    private final Button startButton;
    private final Button stopButton;
    private final EditText ipAndPort;
    private final Button pauseButton;

    protected String connectionString = "";

    public ViewListeners(Button startButton, Button stopButton, EditText ipAndPort, Button pauseButton) {
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.ipAndPort = ipAndPort;
        this.pauseButton = pauseButton;

        this.startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "Clicked on START, " + ViewListeners.this.connectionString);
                ConnectionToServer.getInstance().setConnectionString(ViewListeners.this.ipAndPort.toString());

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
    }
}
