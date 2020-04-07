package com.asdtechlabs.bloodbank;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class LocationService extends Service {


    LocationManager locationManager;
    Context mContext;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {

        mContext = this;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                20000,
                200, locationListenerGPS);
        //isLocationEnabled();

    }

    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();


            GeoPoint currentLocation = new GeoPoint(latitude, longitude);
            db
                    .collection("users")
                    .document(user.getUid())
                    .update("location", currentLocation);

            String msg="New Latitude: "+latitude + "New Longitude: "+longitude;
            //Toast.makeText(mContext,msg,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

            //Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT ).show();

        }

        @Override
        public void onProviderDisabled(String provider) {

            //Toast.makeText( getApplicationContext(), "Gps Disabled, Please Enable GPS", Toast.LENGTH_SHORT ).show();

        }
    };



    private void isLocationEnabled() {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
        else{
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Confirm Location");
            alertDialog.setMessage("Your Location is enabled, please enjoy");
            alertDialog.setNegativeButton("Back to interface",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
    }
}


//    String TAG = "FDEWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
//    static Boolean isGpsOn = false;
//    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
//

//    private FusedLocationProviderClient mFusedLocationProviderClient;
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//
//
//        // This will be called when your Service is created for the first time
//        // Just do any operations you need in this method.
//
//
//            turnGPSOn(MyApplication.getAppContext());
//            isGpsOn = true;
//
//
//
//
//
//
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//
//    private void getDeviceLocation(){
//        Log.d(TAG, "getDeviceLocation: getting the devices current location");
//
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        try{
//
//
//            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//
//                    if (location != null) {
//
//                        double lat = location.getLatitude();
//                        double lon = location.getLongitude();
//                        GeoPoint currentLocation = new GeoPoint(lat, lon);
//                        db
//                                .collection("users")
//                                .document(user.getUid())
//                                .update("location", currentLocation);
//                    }
//                }
//            });
//
//            mFusedLocationProviderClient.getLastLocation().addOnFailureListener( new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d(TAG, "onComplete: current location is null");
//                    Toast.makeText(getApplicationContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//
//        }catch (SecurityException e){
//            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
//        }
//
//
//    }
//
//    public void turnGPSOn(Context context){
//        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
//                .addApi(LocationServices.API).build();
//        googleApiClient.connect();
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(100);
//        locationRequest.setFastestInterval(10000 / 2);
//        locationRequest.setSmallestDisplacement(10);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//        builder.setAlwaysShow(true);
//
//        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//            @Override
//            public void onResult(LocationSettingsResult result) {
//                final Status status = result.getStatus();
//                switch (status.getStatusCode()) {
//                    case LocationSettingsStatusCodes.SUCCESS:
//                        Log.i(TAG, "All location settings are satisfied.");
//                        getDeviceLocation();
//                        break;
//
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
//                        break;
//                }
//            }
//        });
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//        turnGPSOn(MyApplication.getAppContext());
//

//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//
//
//    }

