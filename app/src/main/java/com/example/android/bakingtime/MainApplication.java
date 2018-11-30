package com.example.android.bakingtime;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //debugging with stetho
        Stetho.initializeWithDefaults(this);

        //detect changes in network connection
        setupConnectionBuddy();
    }

    private void setupConnectionBuddy() {
        ConnectionBuddyConfiguration networkInspectorConfiguration = new ConnectionBuddyConfiguration.Builder(this).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);
    }
}
