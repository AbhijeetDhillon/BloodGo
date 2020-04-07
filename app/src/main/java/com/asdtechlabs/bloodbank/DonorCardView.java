package com.asdtechlabs.bloodbank;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.asdtechlabs.bloodbank.DonorScreen.noRequest;
import static com.asdtechlabs.bloodbank.MainActivity.editors;
import static com.asdtechlabs.bloodbank.MainActivity.sharedpreferences;
import static com.asdtechlabs.bloodbank.SignIn.sharedPref;

/**
 * Created by Abhijeet on 8/26/2019.
 */

public class DonorCardView extends Fragment {


    TextView SeekerName, Date,Time, noOfBottles,location, iAmComing, notComing, iHaveDonated,showOnMap;
    ImageView callButton;
    static LinearLayout infoOfDonation;
    View v,vs;
    LinearLayout donorAccepted, donorRequested,donorDonated;
    CardView cardView;
    FirebaseUser user;
    FirebaseFirestore db;
    String seekerId,token,requestedDonors,TAG = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.donor_requests, container, false);
        vs = inflater.inflate(R.layout.fragment_donor_screen, container, false);
        DonorScreen.isAccepted = true;
        DonorScreen.highlight();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        callButton = v.findViewById(R.id.callButton);
        SeekerName = (TextView) v.findViewById(R.id.seekerName);
        //location = (TextView) v.findViewById(R.id.location);
        showOnMap = v.findViewById(R.id.showOnMap);
        Date = v.findViewById(R.id.date);
        Time = v.findViewById(R.id.time);
        noOfBottles = v.findViewById(R.id.noOfBottles);
        donorAccepted = v.findViewById(R.id.donorAccepted);
        donorRequested = v.findViewById(R.id.donorRequested);
        donorDonated = v.findViewById(R.id.donorDonated);
        iAmComing = v.findViewById(R.id.iAmComing);
        iHaveDonated = v.findViewById(R.id.iHaveDonated);
        notComing = v.findViewById(R.id.notComing);
        infoOfDonation = v.findViewById(R.id.infoOfDonation);
        Bundle arguments = getArguments();
        infoOfDonation.setVisibility(View.GONE);
        cardView = v.findViewById(R.id.card_view_Donor);
        callButton.setVisibility(View.VISIBLE);
        String name, dateofDonation, bottles, time, contact;
        float  placeLat,placeLon;


        requestedDonors = sharedpreferences.getString("noOfRequests",null);

        infoOfDonation.setVisibility(View.VISIBLE);

        name = sharedpreferences.getString("SeekerName", null);
        dateofDonation = sharedpreferences.getString("dateofDonation", null);
        placeLat = sharedpreferences.getFloat("PlaceofDonationLat", 0);
        placeLon = sharedpreferences.getFloat("PlaceofDonationLon", 0);
        bottles = sharedpreferences.getString("NoOfBottles", null);
        seekerId = sharedpreferences.getString("seekerId", null);
        contact = sharedpreferences.getString("contact", null);
        time = sharedpreferences.getString("time", null);
        token = sharedpreferences.getString("token", null);



        if (seekerId != null && name != null && placeLat != 0) {

            SeekerName.setText(name);
            Date.setText(dateofDonation);
            noOfBottles.setText(bottles);
            Time.setText(time);
            donorRequested.setVisibility(View.GONE);
            donorAccepted.setVisibility(View.VISIBLE);


            showOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strUri = "http://maps.google.com/maps?q=loc:" + placeLat + "," + placeLon + " (" + "Donation Destination" + ")";
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                    startActivity(intent);
                }
            });

            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Intent intent = new Intent(Intent.ACTION_DIAL);
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                            "tel", contact, null));
                    phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getAppContext().startActivity(phoneIntent);

                }
            });

            if (sharedpreferences.getBoolean("hasLeft", false) && !sharedpreferences.getBoolean("isDriveCompleted", false) && !sharedpreferences.getBoolean("Completed", false) ) {


                // if user has pressed I have left then this action will take place
                finalState();

            } else if(!sharedpreferences.getBoolean("isDriveCompleted", false) && !sharedpreferences.getBoolean("Completed", false)){

                DocumentReference dref = db.collection("seekerRequest").document(seekerId).collection(requestedDonors).document(user.getUid());
                dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                // options to user, I am coming or I can't come. Based on the option selected the user will redirected

                                iAmComing.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

                                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        final View dialogView = inflater.inflate(R.layout.dialogcustom_donor_depature, null);
                                        dialogBuilder.setView(dialogView);
                                        final AlertDialog alertDialog = dialogBuilder.create();
                                        alertDialog.show();

                                        TextView continueDonation = dialogView.findViewById(R.id.donorLeft);
                                        TextView close = dialogView.findViewById(R.id.goBack);

                                        continueDonation.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                DocumentReference dref = db.collection("seekerRequest").document(seekerId);
                                                dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documents = task.getResult();
                                                            if (documents.exists()) {

                                                                //if blood drive is over, don't take the information
                                                                if (!documents.getBoolean("isBloodDriveOver")) {
                                                                    db.collection("seekerRequest").document(seekerId).collection(requestedDonors).document(user.getUid()).update("isComing", true);

                                                                    SendNotification sendNotification = new SendNotification();
                                                                    sendNotification.getAppReadyForNotification(sharedPref.getString("UserName", "A donor"), "has left for your location. Start Tracking", token);

                                                                    NewRequestDonation.mRecycler.setVisibility(View.GONE);

                                                                    editors.putBoolean("hasLeft", true);
                                                                    editors.apply();
                                                                    finalState();
                                                                    alertDialog.dismiss();

                                                                }

                                                                else
                                                                {
                                                                    noRequest.setText("Looks like the seeker cancelled the drive");
                                                                    noRequest.setVisibility(View.VISIBLE);
                                                                    DonorScreen.frame.setVisibility(View.GONE);
                                                                    cardView.setVisibility(View.GONE);
                                                                    clear();
                                                                    alertDialog.dismiss();
                                                                }
                                                                }
                                                            }
                                                        }
                                                    });
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

                                notComing.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        db.collection("seekerRequest").document(seekerId).collection(requestedDonors).document(user.getUid()).update("isCancelled", true);

                                        db.collection("users").document(user.getUid()).collection("requests").document(seekerId)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                        editors.clear();
                                                        editors.apply();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting document", e);
                                                    }
                                                });
                                        DonorScreen.frame.setVisibility(View.GONE);
                                        cardView.setVisibility(View.GONE);
                                        noRequest.setText("No Ongoing Drive!");
                                        noRequest.setVisibility(View.VISIBLE);


                                    }
                                });

                            }
                        }
                    }

                });


            }

            else
            {

            }
        }

        else
        {
            noRequest.setText("No OnGoing Drive!");
            noRequest.setVisibility(View.VISIBLE);
        }

            return v;

    }

    public void finalState() {


        Intent servIntent = new Intent(getContext(),LocationService.class);
        getContext().startService(servIntent);


        donorAccepted.setVisibility(View.GONE);
        donorDonated.setVisibility(View.VISIBLE);
        iHaveDonated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                clear();
                DocumentReference dref = db.collection("seekerRequest").document(seekerId);
                dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documents = task.getResult();
                            if (documents.exists()) {

                                //if blood drive is over, don't take the information
                                if (!documents.getBoolean("isBloodDriveOver")) {


                                    db.collection("seekerRequest").document(seekerId).collection(requestedDonors).document(user.getUid()).update("isCompleted",true);
                                    db.collection("users").document(user.getUid()).collection("requests").document(seekerId)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                    editors.clear();
                                                    editors.apply();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error deleting document", e);
                                                }
                                            });
                                    DonorScreen.frame.setVisibility(View.GONE);
                                    cardView.setVisibility(View.GONE);
                                    SendNotification sendNotification = new SendNotification();
                                    sendNotification.getAppReadyForNotification(sharedPref.getString("UserName","A donor"),"has donated blood",token);
                                    noRequest.setText("Donation Successful, Thank You!");
                                    noRequest.setVisibility(View.VISIBLE);
                                }

                                else {
                                    noRequest.setText("Looks like the seeker cancelled the drive");
                                    noRequest.setVisibility(View.VISIBLE);

                                }

                            }
                        }
                    }
                });
            }


            });
        }


        public void clear()
        {
            editors.putBoolean("hasLeft",false);
            editors.putBoolean("isDriveCompleted",false);
            editors.putBoolean("hasAccepted",false );
            editors.putString("SeekerName",null);
            editors.putString("dateofDonation", null);
            editors.putFloat("PlaceofDonationLat",0);
            editors.putFloat("PlaceofDonationLon",0);
            editors.putString("NoOfBottles", null);
            editors.putString("seekerId",null);
            editors.putString("contact",null );
            editors.putString("time",null);
            editors.putString("token",null);
            editors.putString("nameOfPlace",null);
            editors.apply();
        }


}
