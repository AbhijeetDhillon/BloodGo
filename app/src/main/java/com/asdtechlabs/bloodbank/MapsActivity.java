package com.asdtechlabs.bloodbank;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.asdtechlabs.bloodbank.DirectionsJSONParser.totalTravelTime;
import static com.asdtechlabs.bloodbank.MainActivity.pref;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    String TAG = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
    LocationManager locationManager;
    Context mContext;
    GoogleMap mMap;
    ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
    TextView estimatedTime;
    String seekerId,DonorId;
    FirebaseFirestore db;
    double latitude,latitudeseeker;
    double longitude,longitudeseeker;
    LatLng latLng2;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        estimatedTime = findViewById(R.id.estimatedTime);
        Intent intent = getIntent();
        seekerId = intent.getStringExtra("SeekerId");
        DonorId = intent.getStringExtra("DonorId");
        db = FirebaseFirestore.getInstance();


        final DocumentReference docRef = db.collection("seekerRequest").document(seekerId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    GeoPoint geopoint = snapshot.getGeoPoint("location");
                    latitudeseeker = geopoint.getLatitude();
                    longitudeseeker = geopoint.getLongitude();
                    latLng2 = new LatLng(latitudeseeker,longitudeseeker);


                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mapFragment.getMapAsync(this);

        mContext = this;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                locationListener();
                //Do something after 20 seconds
                handler.postDelayed(this, 30000);
            }
        }, 5000);
    }

    private class DownloadTask extends AsyncTask<String,String,String> {


        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }

    }

    class ParserTask extends AsyncTask<String, Integer, List<List<HashMap>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(String.valueOf(point.get("lat")));
                    double lng = Double.parseDouble(String.valueOf(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLACK);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route

            int hours = totalTravelTime / 3600;
            int minutes = (totalTravelTime % 3600) / 60;

            String timeString = String.format("%02d hrs : %02d mins", hours, minutes);
            estimatedTime.setText(timeString);
            if(lineOptions!=null)
            mMap.addPolyline(lineOptions);

        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyB2GB6wFgq8zaDHCbqLD2l-VNGfmspm00M";


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }






    public void theMapFunction(LatLng donor)
    {






        if (markerPoints.size() > 1) {
            markerPoints.clear();
            mMap.clear();
        }

//        double lat1 = (pref.getFloat("seekerLocationLat",0))*1e6;
//        double lon1 =  (pref.getFloat("seekerLocationLon",0))*1e6;
//        LatLng latLng2 = new LatLng(lat1,lon1);
        markerPoints.add(0,latLng2);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng2);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(options);

        LatLng latLng1 = donor;
        // Adding new item to the ArrayList
        markerPoints.add(1,latLng1);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 16));
        // Creating MarkerOptions

        MarkerOptions options1 = new MarkerOptions();
        // Setting the position of the marker




        options1.position(latLng1);
        options1.icon(BitmapDescriptorFactory.fromResource(R.drawable.blood));





        // Add new marker to the Google Map Android API V2

        mMap.addMarker(options1);

        // Checks, whether start and end locations are captured
        if (markerPoints.size() >= 2) {
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

    }

    public void locationListener()
    {


        final DocumentReference docRef = db.collection("users").document(DonorId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    GeoPoint geopoint = snapshot.getGeoPoint("location");
                    latitude = geopoint.getLatitude();
                    longitude = geopoint.getLongitude();
                    LatLng latLng = new LatLng(latitude,longitude);

                    theMapFunction(latLng);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


//            String msg="New Latitude: "+latitude + "New Longitude: "+longitude;


    }
}

