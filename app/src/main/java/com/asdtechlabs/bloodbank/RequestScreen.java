package com.asdtechlabs.bloodbank;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.asdtechlabs.bloodbank.MainActivity.editor;
import static com.asdtechlabs.bloodbank.MainActivity.pref;
import static com.asdtechlabs.bloodbank.SignIn.sharedPref;
import static com.firebase.ui.auth.AuthUI.TAG;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class RequestScreen extends Fragment implements View.OnClickListener,PlacesAutoCompleteAdapter.ClickListener {


    TextView ap,an,bp,bn,abp,abn,op,on,donateBlood,recieveBlood,presentLocation;
    static Button search;
    static TextView location;
    static Boolean isTime=false,isLocation=false,isBloodGroup=false,isAction=false,isDate=false;
    View v;
    FirebaseUser user;
    FirebaseFirestore db;
    GeoPoint locationofBlood;
    String bloodGroup,bottlesRequired,distance,timeOfDonation,dateOfDonation,TAG="XXXXXXXXXXXXXXXXXXXXXXXXXXXXx";
    int mYear, mMonth, mDay, mHour, mMinute;
    Timestamp timestamp;
    Calendar c;
    final String[] fullName  = new String[1];
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    static RecyclerView recyclerView;
    static GeoPoint locationEntered;
    static String nameOfPlace;
    static TextView address,date, time;
    Spinner bottles;
    String noOfRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    static AlertDialog alertDialog;
    double lat,lon;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if(!isNetworkConnected())
        {
            v = inflater.inflate(R.layout.no_internet, container, false);
        }

        else {
            v = inflater.inflate(R.layout.fragment_request_screen, container, false);
            final TextView seekBarValue = v.findViewById(R.id.seekBarValue);
            search = v.findViewById(R.id.searchBlood);
            c = Calendar.getInstance();

            address = v.findViewById(R.id.address);
            location = v.findViewById(R.id.locations);
            date = v.findViewById(R.id.date);
            time = v.findViewById(R.id.time);
            distance = "10";

            //GetName of User
            String uId = user.getUid();
            DocumentReference docRef = db.collection("users").document(uId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            fullName[0] = document.getString("name");

                        } else {
                            fullName[0] = "User 1";
                            Log.d("LOGGER", "No such document");
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                    }
                }
            });


            //All the search for the location happens here
            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
// ...Irrelevant code for customizing the buttons and title
                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View dialogView = inflater.inflate(R.layout.search_location, null);
                    dialogBuilder.setView(dialogView);
                    alertDialog = dialogBuilder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);

                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            isCheck();
                        }
                    });

                    //Google Maps
                    Places.initialize(v.getContext(), "AIzaSyB2GB6wFgq8zaDHCbqLD2l-VNGfmspm00M");
                    recyclerView = (RecyclerView) dialogView.findViewById(R.id.places_recycler_view);
                    mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(dialogView.getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(dialogView.getContext()));
                    recyclerView.setAdapter(mAutoCompleteAdapter);
                    mAutoCompleteAdapter.notifyDataSetChanged();


                    EditText location = dialogView.findViewById(R.id.location);
                    TextView currentLocation = dialogView.findViewById(R.id.currentLocation);
                    TextView close = dialogView.findViewById(R.id.close);

                    location.addTextChangedListener(locationTextWatcher);


                    currentLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ContextCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions((Activity) MyApplication.getAppContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                ActivityCompat.requestPermissions((Activity) MyApplication.getAppContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);


                            } else {

                                turnGPSOn(MyApplication.getAppContext());

                            }
                        }
                    });


                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();

                        }
                    });

                }
            });

            //open calender
            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);


                    DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(),
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {

                                    date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                    if (date.getText() != null) {
                                        isDate = true;
                                        isCheck();
                                    }

                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();


                }
            });

            //open clock
            time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar c = Calendar.getInstance();
                    mHour = c.get(Calendar.HOUR_OF_DAY);
                    mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(),
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {

                                    time.setText(hourOfDay + ":" + minute);

                                    if (time.getText() != null) {
                                        isTime = true;
                                        isCheck();
                                    }

                                }
                            }, mHour, mMinute, false);
                    timePickerDialog.show();


                }
            });


            //get number of Bottles
            bottles = v.findViewById(R.id.bottles);

            String[] noOfBottles = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, noOfBottles);
            bottles.setAdapter(adapter2);

            bottles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    bottlesRequired = bottles.getSelectedItem().toString();
                    // On selecting a spinner item

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                    bottlesRequired = "1";
                }
            });


            ap = v.findViewById(R.id.ap);
            an = v.findViewById(R.id.an);
            bp = v.findViewById(R.id.bp);
            bn = v.findViewById(R.id.bn);
            abp = v.findViewById(R.id.abp);
            abn = v.findViewById(R.id.abn);
            op = v.findViewById(R.id.op);
            on = v.findViewById(R.id.on);
            donateBlood = v.findViewById(R.id.donate);
            recieveBlood = v.findViewById(R.id.seek);
            // presentLocation = v.findViewById(R.id.currentLocation);

            ap.setOnClickListener((View.OnClickListener) this);
            an.setOnClickListener((View.OnClickListener) this);
            bp.setOnClickListener((View.OnClickListener) this);
            bn.setOnClickListener((View.OnClickListener) this);
            abp.setOnClickListener((View.OnClickListener) this);
            abn.setOnClickListener((View.OnClickListener) this);
            op.setOnClickListener((View.OnClickListener) this);
            on.setOnClickListener((View.OnClickListener) this);


            SeekBar seekBar = v.findViewById(R.id.progressBar);
            seekBar.setMax(101);
            seekBar.setProgress(10);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {

                    // TODO Auto-generated method stub

                    if (progress == 101) {

                        distance = "3000";
                        seekBarValue.setText("100+ kms");
                    } else {
                        distance = String.valueOf(progress);
                        seekBarValue.setText(String.valueOf(progress) + "kms");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
            });


            isLocation = false;
            isDate = false;
            isBloodGroup = false;
            isTime = false;

        }
        return v;

    }




    private void loadFragment(Fragment fragment) {
// create a FragmentManager
        FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    @Override
    public void onClick(View v) {

            isBloodGroup = true;

            isCheck();


            ap.setBackgroundResource(R.drawable.blood_group);
            an.setBackgroundResource(R.drawable.blood_group);
            bp.setBackgroundResource(R.drawable.blood_group);
            bn.setBackgroundResource(R.drawable.blood_group);
            op.setBackgroundResource(R.drawable.blood_group);
            on.setBackgroundResource(R.drawable.blood_group);
            abp.setBackgroundResource(R.drawable.blood_group);
            abn.setBackgroundResource(R.drawable.blood_group);

            ap.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            an.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            bp.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            bn.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            op.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            on.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            abp.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);
            abn.setTextAppearance(MyApplication.getAppContext(), R.style.BloodGroup);


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



    private TextWatcher locationTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!s.toString().equals("")) {
                mAutoCompleteAdapter.getFilter().filter(s.toString());
                if (recyclerView.getVisibility() == View.GONE) {recyclerView.setVisibility(View.VISIBLE);}
            } else {
                if (recyclerView.getVisibility() == View.VISIBLE) {recyclerView.setVisibility(View.GONE);}
            }




        }
    };




    public void isCheck()
    {


        if (isLocation && isDate && isBloodGroup && isTime)
        {

            search.setBackgroundResource(R.drawable.button_red_activated);
            search.setTextAppearance(MyApplication.getAppContext(),R.style.ButtonActiveBottom);
            search.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v)
                {
                    saveRequest();

                    loadFragment(new RequestScreen2());
                }
            });

        }

        else
        {
            search.setBackgroundResource(R.drawable.button_deactive);
            search.setTextAppearance(MyApplication.getAppContext(),R.style.Proceed);
        }






    }

    public void saveRequest()
    {





        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        String uId = user.getUid();
        String contactNumber = user.getPhoneNumber();
        timeOfDonation = time.getText().toString();
        dateOfDonation = date.getText().toString();
        Map<String, Object> seekerDetails = new HashMap<>();
        seekerDetails.put("name", fullName[0]);
        seekerDetails.put("contactNumber", contactNumber);
        seekerDetails.put("bloodGroupRequired", bloodGroup);
        seekerDetails.put("location", locationEntered);
        seekerDetails.put("nameOfPlace",nameOfPlace);
        seekerDetails.put("noOfBottles", bottlesRequired);
        seekerDetails.put("date", dateOfDonation);
        seekerDetails.put("time", timeOfDonation);
        seekerDetails.put("distance", distance);
        seekerDetails.put("isSearchedBlood", true);
        seekerDetails.put("isBloodDriveOver",false);
        String token = sharedPref.getString("token", null);
        seekerDetails.put("token",token);


        db.collection("seekerRequest").document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        noOfRequest = document.getString("noOfRequests");

                    if(document.get("isBloodDriveOver")!=null){

                        if (document.getBoolean("isBloodDriveOver"))
                        {
                            if(noOfRequest!=null)

                            {
                                int number = Integer.parseInt(noOfRequest);
                                number = number+1;
                                noOfRequest = String.valueOf(number);
                            }
                            else
                            {
                                noOfRequest = "1";
                            }
                        }

                    }

                        else
                        {
                            noOfRequest = "1";
                        }

                        seekerDetails.put("noOfRequests",noOfRequest);
                        MainActivity.editorRequest = MainActivity.RequestPref.edit();
                        MainActivity.editorRequest.putString("noOfRequests","requestedDonors"+noOfRequest);
                        MainActivity.editorRequest.apply();
                        saveData(seekerDetails);
                    } else {
                        noOfRequest = "1";
                        Log.d("LOGGER", "No such document");
                        seekerDetails.put("noOfRequests",noOfRequest);
                        MainActivity.editorRequest = MainActivity.RequestPref.edit();
                        MainActivity.editorRequest.putString("noOfRequests","requestedDonors"+noOfRequest);
                        MainActivity.editorRequest.apply();
                        saveData(seekerDetails);
                    }





                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });






        editor = pref.edit();

        editor.putString("bloodGroup", bloodGroup);
        editor.putString("distance", distance);



        editor.apply();



    }

    @Override
    public void click(Place place) {
        Toast.makeText(v.getContext(), place.getAddress()+", "+place.getLatLng().latitude+place.getLatLng().longitude, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
        location.setText(place.getAddress());

    }



    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(v.getContext());

        try{


            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener((Activity) v.getContext(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location locations) {

                    if (locations != null) {
                        location.setText("Got your current location!");
                        isLocation = true;
                        address.setVisibility(View.GONE);
                        lat = locations.getLatitude();
                        lon = locations.getLongitude();
                        editor = pref.edit();
                        editor.putFloat("geopointLat", (float) lat);
                        editor.putFloat("geopointLon", (float) lon);
                        nameOfPlace = "Please view on Map";
                        locationEntered = new GeoPoint(lat, lon);
                        editor.apply();
                        isCheck();
                        alertDialog.dismiss();
                        Log.d(TAG, "onComplete: current location"+locationEntered);
//                        db
//                                .collection("seekerRequest")
//                                .document(user.getUid())
//                                .update("location", currentLocation);
//
                   }

                    else
                    {
                        Toast.makeText(v.getContext(), "Unable to get current location, Open Google Maps and then try again", Toast.LENGTH_LONG).show();
                    }

                }
            });

            mFusedLocationProviderClient.getLastLocation().addOnFailureListener((Activity) v.getContext(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onComplete: current location is null");
                    Toast.makeText(v.getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();

                }
            });


        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
            Toast.makeText(v.getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
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
                            status.startResolutionForResult((Activity) v.getContext(), REQUEST_CHECK_SETTINGS);
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

    public void saveData(Map<String, Object> seekerDetails)
    {
        db.collection("seekerRequest").document(user.getUid())
                .set(seekerDetails)
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
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }



}




