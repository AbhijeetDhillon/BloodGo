package com.asdtechlabs.bloodbank;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.asdtechlabs.bloodbank.MainActivity.editor;
import static com.asdtechlabs.bloodbank.MainActivity.pref;
import static com.asdtechlabs.bloodbank.SignIn.editorName;
import static com.asdtechlabs.bloodbank.SignIn.sharedPref;

public class PostSignIn extends AppCompatActivity implements View.OnClickListener{
    String bloodGroup= " ",fullName, emailId ;
    static String isPostSigned ="false";
    EditText name, locationSearch;
    TextView ap,an,bp,bn,abp,abn,op,on,donateBlood,recieveBlood, proceed,currentLocation;
//    Geocoder geocoder = new Geocoder(this);
    static Boolean isName=false,isLocation=false,isBloodGroup=false,isAction=false,isDonate=false,isCheck=false;
    private static final String TAG = "MapActivity";
    String query;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private RecyclerView recyclerView;
    FirebaseUser user;
    FirebaseFirestore db;
    GeoPoint location;
    String token;
    public static final String MyPREFERENCES = "SkipPostSignIn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(!isNetworkConnected())
        {
            setContentView(R.layout.no_internet);
        }

        else {
            setContentView(R.layout.activity_post_sign_in);

            // getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.id.toolbar_title);
            //locationSearch = findViewById(R.id.location);
            name = findViewById(R.id.name);
            // locationSearch.addTextChangedListener(emailTextWatcher);
            name.addTextChangedListener(generalTextWatcher);

//        String userLocation = String.valueOf(locationSearch.getText());

            proceed = findViewById(R.id.proceed);
            user = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();

            ap = findViewById(R.id.ap);
            an = findViewById(R.id.an);
            bp = findViewById(R.id.bp);
            bn = findViewById(R.id.bn);
            abp = findViewById(R.id.abp);
            abn = findViewById(R.id.abn);
            op = findViewById(R.id.op);
            on = findViewById(R.id.on);
            donateBlood = findViewById(R.id.donate);
            recieveBlood = findViewById(R.id.seek);
            currentLocation = findViewById(R.id.currentLocation);

            ap.setOnClickListener((View.OnClickListener) this);
            an.setOnClickListener((View.OnClickListener) this);
            bp.setOnClickListener((View.OnClickListener) this);
            bn.setOnClickListener((View.OnClickListener) this);
            abp.setOnClickListener((View.OnClickListener) this);
            abn.setOnClickListener((View.OnClickListener) this);
            op.setOnClickListener((View.OnClickListener) this);
            on.setOnClickListener((View.OnClickListener) this);


        }


    }



    public void Proceed(View view)
    {

        if(isCheck) {
            String uId = user.getUid();
            String contactNumber = user.getPhoneNumber();

            getNotificationToken();

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("name", fullName);
            userDetails.put("contactNumber", contactNumber);
            userDetails.put("bloodGroup", bloodGroup);
            userDetails.put("location", location);
            userDetails.put("isAvailable", true);
            userDetails.put("token", token);

            editorName = sharedPref.edit();
            editorName.putString("UserName",fullName);
            editorName.apply();

            db.collection("users").document(uId)
                    .set(userDetails)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

            MyApplication.sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = MyApplication.sharedpreferences.edit();

            editor.putBoolean("PostSignIn", true);
            editor.putBoolean("isDonate", isDonate);
            editor.apply();

            Intent intent = new Intent(PostSignIn.this, MainActivity.class);


            //

            isName=false;
            isLocation=false;
            isBloodGroup=false;
            isAction = false;
            isCheck = false;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();
        }

        else
        {
            Toast.makeText(this, "Please enter missing fields", Toast.LENGTH_SHORT).show();
        }
        
    }

    @Override
    public void onClick(View v) {

        isBloodGroup=true;

        isCheck();
        

        ap.setBackgroundResource(R.drawable.blood_group);
        an.setBackgroundResource(R.drawable.blood_group);
        bp.setBackgroundResource(R.drawable.blood_group);
        bn.setBackgroundResource(R.drawable.blood_group);
        op.setBackgroundResource(R.drawable.blood_group);
        on.setBackgroundResource(R.drawable.blood_group);
        abp.setBackgroundResource(R.drawable.blood_group);
        abn.setBackgroundResource(R.drawable.blood_group);

        ap.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        an.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        bp.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        bn.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        op.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        on.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        abp.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
        abn.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);


        switch(v.getId()){

            case R.id.ap:

                ap.setBackgroundResource(R.drawable.button_red_activated);
                ap.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "A+";
                break;
            case R.id.an:
                an.setBackgroundResource(R.drawable.button_red_activated);
                an.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "A-";
                break;
            case R.id.bp:
                bp.setBackgroundResource(R.drawable.button_red_activated);
                bp.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "B+";
                break;
            case R.id.bn:
                bn.setBackgroundResource(R.drawable.button_red_activated);
                bn.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "B-";
                break;
            case R.id.abp:
                abp.setBackgroundResource(R.drawable.button_red_activated);
                abp.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "AB+";
                break;
            case R.id.abn:
                abn.setBackgroundResource(R.drawable.button_red_activated);
                abn.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "AB-";
                break;
            case R.id.op:
                op.setBackgroundResource(R.drawable.button_red_activated);
                op.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "O+";
                break;
            case R.id.on:

                on.setBackgroundResource(R.drawable.button_red_activated);
                on.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
                bloodGroup = "O-";
                break;



        }
    }


    public void donateBlood(View v)
    {

        isAction=true;
        isDonate=true;
        isCheck();
        
        donateBlood.setBackgroundResource(R.drawable.button_red_activated);
        donateBlood.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
        recieveBlood.setBackgroundResource(R.drawable.blood_group);
        recieveBlood.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
    }
    public void seekBlood(View v)
    {
        isDonate=false;
        isAction=true;
        isCheck();
        
        recieveBlood.setBackgroundResource(R.drawable.button_red_activated);
        recieveBlood.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
        donateBlood.setBackgroundResource(R.drawable.blood_group);
        donateBlood.setTextAppearance(MyApplication.getAppContext(),R.style.BloodGroup);
    }


    private TextWatcher generalTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable s) {
            String string = s.toString();
            isName = !string.equals("");
            fullName  = string;
            isCheck();
            
        }
    };




    public void isCheck()
    {
        if (isLocation && isAction && isBloodGroup && isName)
        {
            isCheck=true;
            proceed.setBackgroundResource(R.drawable.button_red_activated);
            proceed.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);

            
        }

        else
        {
            isCheck=false;
            proceed.setBackgroundResource(R.drawable.button_deactive);
            proceed.setTextAppearance(MyApplication.getAppContext(),R.style.Proceed);
        }



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
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }




    public void GetLocation(View view)

    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(PostSignIn.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(PostSignIn.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);


        } else {

            turnGPSOn(MyApplication.getAppContext());

        }
    }

    private void turnGPSOn(Context context){
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

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
                            status.startResolutionForResult(PostSignIn.this, REQUEST_CHECK_SETTINGS);
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


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        try{


            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(PostSignIn.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location locations) {

                    if (locations != null) {

                        location = new GeoPoint(locations.getLatitude(),locations.getLongitude());
                        isLocation = true;
                        isCheck();
                        TextView gotYourLocation = findViewById(R.id.locationPostSignIn);
                        gotYourLocation.setVisibility(View.VISIBLE);
                        TextView currentLocation = findViewById(R.id.currentLocationPostSignIn);
                        currentLocation.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Got your location", Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        Toast.makeText(getApplicationContext(), "Unable to get current location, Open Google Maps and then try again", Toast.LENGTH_LONG).show();
                    }

                }
            });

            mFusedLocationProviderClient.getLastLocation().addOnFailureListener(PostSignIn.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onComplete: current location is null");
                    Toast.makeText(getApplicationContext(), "unable to get current location", Toast.LENGTH_SHORT).show();

                }
            });


        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
            Toast.makeText(getApplicationContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(PostSignIn.this,SignIn.class);
        intent.putExtra("isPostSignIn",true);

        startActivity(intent);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
