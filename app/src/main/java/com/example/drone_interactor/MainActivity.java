package com.example.drone_interactor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.afinal.core.AsyncTask;

/**
 * The main activity of the app, starts everything else and handles the UI.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    private static MainActivity instance = null;

    /**
     * During startup the class will store it's own instance. This method will always be
     * called first during startup of app.
     */
    public MainActivity() {
        MainActivity.instance = this;
    }

    /**
     * Returns the instance of this class
     * This is done because we can only change objects on screen from the 
     * main class with it's main thread.
     */
    public static MainActivity getInstance() {
        return MainActivity.instance;
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    /**
     * starts of the application with overriding the AppCompatActivity onCreate method 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        setContentView(R.layout.activity_main);

        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());

        new ViewListeners((Button) findViewById(R.id.startButton),
                (Button) findViewById(R.id.stopButton),
                (EditText) findViewById(R.id.nameInput),
                (Button) findViewById(R.id.pauseButton),
                (Switch) findViewById(R.id.forwardOption),
                (Switch) findViewById(R.id.backwardOption),
                (Switch) findViewById(R.id.upwardOption),
                (Switch) findViewById(R.id.downwardOption),
                (Switch) findViewById(R.id.obstacleAvoidanceOption),
                (Button) findViewById(R.id.rotateLeft),
                (Button) findViewById(R.id.rotateRight),
                (Button) findViewById(R.id.rotateAndScan));
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    /**
     * Start the registration of the application with DJI with the APP_KEY stored in the Manifest
     * Code given by DJI mobile SDK
     */
    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.setDisplayContent(null);
                                }
                            });

                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("Register Success");
                                Log.e(TAG, "WHILE REGISTERING " + String.valueOf(DJISDKManager.getInstance().startConnectionToProduct()));
                            } else {
                                showToast("Register sdk fails, please check the bundle id and network connection!");
                            }
                            Log.v(TAG, djiError.getDescription());

                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            showToast("Product Disconnected");
                            notifyStatusChange();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.setDisplayContent(null);
                                }
                            });

                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            showToast("Product Connected");
                            notifyStatusChange();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.setDisplayContent(baseProduct);
                                }
                            });
                            // call our own startup class
                            startupClasses();

                            MainActivity.this.mProduct = baseProduct;
                        }

                        @Override
                        public void onProductChanged(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductChanged newProduct:%s", baseProduct));
                            MainActivity.this.mProduct = baseProduct;
                            startupClasses();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.setDisplayContent(baseProduct);
                                }
                            });
                        }

                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {
                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                        notifyStatusChange();
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }
                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {
                            Log.d(TAG, "init process: " + djisdkInitEvent);
                        }

                        @Override
                        public void onDatabaseDownloadProgress(long l, long l1) {
                            Log.d(TAG, "download: " + l + ", " + l1);

                        }
                    });
                }
            });
        }
    }

    /** 
     * Creates a TextViews instance and start every other classes which are required
     * for the application to work. The textViews are used to control the objects 
     * on the screen to show information to specific objects.
     */
    private void startupClasses() {
        TextViews textViews = new TextViews(
                findViewById(R.id.debugText),
                findViewById(R.id.motors),
                findViewById(R.id.distanceX),
                findViewById(R.id.distanceY),
                findViewById(R.id.distanceZ),
                findViewById(R.id.downwardDistance),
                findViewById(R.id.currentAngle),
                findViewById(R.id.forwardDistance),
                findViewById(R.id.backwardDistance),
                findViewById(R.id.upwardDistance),
                findViewById(R.id.forwardOption),
                findViewById(R.id.backwardOption),
                findViewById(R.id.upwardOption),
                findViewById(R.id.downwardOption),
                findViewById(R.id.obstacleAvoidanceOption));

        try {
            // initialize the class droneDataProcessing
            DroneDataProcessing droneDataProcessing = DroneDataProcessing.getInstance();
            // fetch the Aircraft instance from the DJISDKManager
            Aircraft aircraft = (Aircraft)DJISDKManager.getInstance().getProduct();
            DroneRotation.getInstance().setAircraft(aircraft, textViews);
            // start the class droneControl with correct parameters
            droneDataProcessing.setup(textViews, aircraft);
            DroneDriving.getInstance().setAircraft(aircraft, textViews);
            this.setObstacleAvoidance(true);
            showToast("Started all classes with parameters");
        } catch (Exception e) {
            showToast("Couldn't initialize all classes");
        }

    }

    /**
     * Sets the obstacle avoidance to either on or off, controlled by the class ViewListeners
     * and is activated during on a button click
     * @param b a boolean value for whether the obstacle avoidance should be on or off
     */
    public void setObstacleAvoidance(boolean b) {
        try {
            ((Aircraft)DJISDKManager.getInstance().getProduct()).getFlightController()
                .getFlightAssistant().setCollisionAvoidanceEnabled(b,
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        showToast("Obstacle avoidance turned " + (b ? "on" : "off"));
                    }
                });
        } catch (Exception e) {
            showToast("No instance of drone");
        }
    }

    /**
     * Sets the text connected to or disconnected to the drone
     */
    @SuppressLint("SetTextI18n")
    private void setDisplayContent(BaseProduct baseProduct) {
        if (baseProduct == null) {
            ((TextView) findViewById(R.id.name)).setText("Disconnected");
        } else {
            ((TextView) findViewById(R.id.name)).setText("Connected to " + baseProduct.getModel().getDisplayName());
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    /** 
     * Shows a toast message
     */
    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Sets a text message to a textView object
     * @param textView the textView object which should be set
     * @param text the text which should be set
     */
    public void setText(TextView textView, String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }
}