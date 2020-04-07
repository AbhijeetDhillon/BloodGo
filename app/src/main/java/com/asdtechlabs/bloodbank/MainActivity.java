package com.asdtechlabs.bloodbank;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.asdtechlabs.bloodbank.SignIn.editorName;
import static com.asdtechlabs.bloodbank.SignIn.sharedPref;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mTextMessage,mTextMessage2,donateBloodMain,requestBloodMain;
    private static final String TAG = "MapActivity";
    BottomNavigationItemView navigationDonate,navigationRequest;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    static SharedPreferences.Editor editor;
    static SharedPreferences pref;
    static SharedPreferences.Editor editorRequest;
    static SharedPreferences RequestPref;
    public static SharedPreferences sharedpreferences;
    static SharedPreferences.Editor editors;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    static ProgressBar progressBar;
    static Boolean isGpsOn = false, RequestedBlood;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAF-LQ_-U:APA91bEtDVKi4QOgrvySrfzv32rjArveFw_OQ1Iyf1IpkrG-7OeMrOzXi12ot3Ebmj3H5PEDi-Iw21KD21IZQZEosBrRn0K0zgYTNSM7F4SiPSMCghNFLn9-vUq8EcBMZPw4QEQ3nime";
    final private String contentType = "application/json";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;
    String token;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!isNetworkConnected()) {

            setContentView(R.layout.no_internet);

        } else {
            setContentView(R.layout.activity_main);



            sharedpreferences = MyApplication.getAppContext().getSharedPreferences("AcceptedData", Context.MODE_PRIVATE);// ;0 - for private mode

            pref = MyApplication.getAppContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        //Getting Device Information
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        donateBloodMain = findViewById(R.id.donateBloodMain);
        requestBloodMain = findViewById(R.id.requestBloodMain);
        progressBar = findViewById(R.id.progress_barMain);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Center Title
        toolbar.setTitleMarginStart((int) (width / 4));


        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        RequestPref = getSharedPreferences("Request", Context.MODE_PRIVATE);


        sharedPref = getSharedPreferences("Pref", Context.MODE_PRIVATE);
        String token = sharedPref.getString("token", null);
        if (TextUtils.isEmpty(token)) {
            getNotificationToken();
        }

        if (MyApplication.sharedpreferences.getBoolean("isDonate", false)) {
            RequestedBlood = false;
            donateBloodMain.setBackgroundResource(R.drawable.button_red_activated);
            donateBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.ButtonActiveBottom);
            requestBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            requestBloodMain.setBackgroundResource(R.drawable.blood_group);

            loadFragment(new DonorScreen());
        } else {
            RequestedBlood = true;
            requestBloodMain.setBackgroundResource(R.drawable.button_red_activated);
            requestBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.ButtonActiveBottom);
            donateBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            donateBloodMain.setBackgroundResource(R.drawable.blood_group);

            DocumentReference dref = db.collection("seekerRequest").document(user.getUid());
            dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if (document.getBoolean("isSearchedBlood")) {
                                loadFragment(new RequestScreen2());
                                Log.d(TAG, "your field exist");
                            } else {
                                loadFragment(new RequestScreen());
                                Log.d(TAG, "your field does not exist");
                                //Create the filed
                            }
                        } else {
                            loadFragment(new RequestScreen());
                        }
                    }
                }
            });


        }


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);


        } else {

//            Intent servIntent = new Intent(MainActivity.this,LocationService.class);
//            startService(servIntent);

            turnGPSOn(MyApplication.getAppContext());

        }

    }


    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                finish();
                moveTaskToBack(true);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                // finish activity
            } else {
                Toast.makeText(this, "Press Back again to Exit.",
                        Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3 * 1000);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.medical) {

            Intent intent = new Intent(MainActivity.this, MedicalConditions.class);
            startActivity(intent);

        }

//        else if (id == R.id.about) {
////            Intent intent = new Intent(MainActivity.this, WebView.class);
////            intent.putExtra("link","https://docs.google.com/forms/u/2/d/1f2FG8kUz5CVOInSOHEx10KaRY39pKJHEMPJlW9k1amc/edit?usp=sharing_eip&ts=5d2edf8b");
////            startActivity(intent);
//
//        }
//
        else if (id == R.id.contact) {
            Intent intent = new Intent(MainActivity.this, WebView.class);
            intent.putExtra("link","https://docs.google.com/forms/d/e/1FAIpQLSc5LPFFrO9YX-sTNqMcSvZ6QQ6wk7N5Nstow8lSM92bu6GS_Q/viewform?usp=sf_link");
            startActivity(intent);

        } else if (id == R.id.feeback) {
            Intent intent = new Intent(MainActivity.this, WebView.class);
            intent.putExtra("link","https://docs.google.com/forms/d/e/1FAIpQLSf2Jnhmx_sds8-rku-qZNns_zsS12LHrrE4uXGJJkl9WNJlpg/viewform?usp=sf_link");
            startActivity(intent);

        } else if (id == R.id.facebook) {

            Intent intent = new Intent(MainActivity.this, WebView.class);
            intent.putExtra("link","https://www.facebook.com/BloodGo-109756340454178");
            startActivity(intent);

        } else if (id == R.id.signout) {

            editorName = sharedPref.edit();
            editorName.clear().apply();
            editor = pref.edit();
            editor.clear().apply();
            editorRequest = RequestPref.edit();
            editorRequest.clear().apply();
            editors = sharedpreferences.edit();
            editors.clear().apply();

            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, SignIn.class));
                            finish();
                        }
                    });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    private void loadFragment(Fragment fragment) {
// create a FragmentManager
        FragmentManager fm = getSupportFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    public void DonateBlood(View v)
    {

        if(RequestedBlood) {
            RequestedBlood = false;
            donateBloodMain.setBackgroundResource(R.drawable.button_red_activated);
            donateBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.ButtonActiveBottom);
            requestBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            requestBloodMain.setBackgroundResource(R.drawable.blood_group);
            donateBloodMain.setEnabled(false);
            requestBloodMain.setEnabled(true);
            loadFragment(new DonorScreen());
        }
    }


    public void RequestBlood(View v) {

        if (!RequestedBlood) {
            RequestedBlood = true;
            requestBloodMain.setBackgroundResource(R.drawable.button_red_activated);
            requestBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.ButtonActiveBottom);
            donateBloodMain.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            donateBloodMain.setBackgroundResource(R.drawable.blood_group);
            donateBloodMain.setEnabled(true);
            requestBloodMain.setEnabled(false);
            DocumentReference dref = db.collection("seekerRequest").document(user.getUid());
            dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if (document.getBoolean("isSearchedBlood")) {
                                loadFragment(new RequestScreen2());
                                Log.d(TAG, "your field exist");
                            } else {
                                loadFragment(new RequestScreen());
                                Log.d(TAG, "your field does not exist");
                                //Create the filed
                            }
                        } else {
                            loadFragment(new RequestScreen());
                            Log.d(TAG, "your field does not exist");
                            //Create the filed
                        }
                    }


                }
            });
        }
    }



    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{


                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {

                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            GeoPoint currentLocation = new GeoPoint(lat, lon);
                            db
                                    .collection("users")
                                    .document(user.getUid())
                                    .update("location", currentLocation);
                        }
                    }
                });

            mFusedLocationProviderClient.getLastLocation().addOnFailureListener(MainActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onComplete: current location is null");
                    Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();

                }
            });


        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }


    }

    public void turnGPSOn(Context context){
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000 / 2);
        locationRequest.setSmallestDisplacement(10);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        getDeviceLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });

        }

        public void getNotificationToken()
        {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            token = task.getResult().getToken();

                            db
                                    .collection("users")
                                    .document(user.getUid())
                                    .update("token", token);

                            editorName = sharedPref.edit();
                            editorName.putString("token",token);
                            editorName.apply();

                            // Log and toast
                            String msg = "Testt"+ token;
                            Log.d(TAG, msg);
                          //  Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }




    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    }

