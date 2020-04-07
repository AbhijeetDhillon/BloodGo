package com.asdtechlabs.bloodbank;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;

import static com.asdtechlabs.bloodbank.PostSignIn.MyPREFERENCES;

/**
 * Created by Abhijeet on 8/27/2019.
 */

public class MyApplication extends Application {

    private static Context context;
    public static SharedPreferences sharedpreferences;


    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        MyApplication.sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        FirebaseApp.initializeApp(this);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
