package com.example.drone_interactor;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

/**
 * Will initialize the DJI SDK, given by DJI mobile SDK for Android.
 */
public class MApplication extends Application {

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        // init the sdk
        Helper.install(MApplication.this);
    }

}