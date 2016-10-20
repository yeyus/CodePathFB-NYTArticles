package com.ea7jmf.nytarticles;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class NYTApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

}
