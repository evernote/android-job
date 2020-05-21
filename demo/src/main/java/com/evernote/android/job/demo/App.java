package com.evernote.android.job.demo;

import android.app.Application;
import android.os.StrictMode;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

/**
 * @author rwondratschek
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        JobManager.create(this).addJobCreator(new DemoJobCreator());
    }
}
