package com.asdtechlabs.bloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


import static com.asdtechlabs.bloodbank.DonorScreen.available;
import static com.asdtechlabs.bloodbank.DonorScreen.frame;
import static com.asdtechlabs.bloodbank.DonorScreen.noRequest;
import static com.asdtechlabs.bloodbank.MainActivity.editors;
import static com.asdtechlabs.bloodbank.MainActivity.sharedpreferences;


public class NewRequestDonation extends Fragment {

    View v;
    ArrayList<String> seekerName = new ArrayList<String>();
    ArrayList<String> bottlesRequired = new ArrayList<>();
    ArrayList<String> Date = new ArrayList<>();
    ArrayList<String> ContactNumber = new ArrayList<>();
    ArrayList<String> Time = new ArrayList<>();
    ArrayList<Float> LocationLat = new ArrayList<>();
    ArrayList<Float> LocationLon = new ArrayList<>();
    ArrayList<String> nameOfPlace = new ArrayList<>();
    ArrayList<String> seekers = new ArrayList<>();
    ArrayList<String> Tokens = new ArrayList<>();
    static RecyclerView mRecycler;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Boolean SecondLoad = false, isInTheLoop = false, exitLoop=false, HasExpired = false, noRequests = true,isBloodOver = true;
    static Boolean hasAcceptedRequest = false;
    String TAG = "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR";
    String dateTime,datetimeFirestore;
    java.util.Date currentDateTime,currentTimeDate;
    String requestedDonors;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        v = inflater.inflate(R.layout.fragment_new_request_donation, container, false);


        mRecycler = (RecyclerView) v.findViewById(R.id.recylerviewDonor);
        sharedpreferences = getActivity().getSharedPreferences("AcceptedData", Context.MODE_PRIVATE);// ;0 - for private mode
        editors = sharedpreferences.edit();
        editors.putBoolean("hasAccepted",hasAcceptedRequest );
        editors.putBoolean("hasExpired",HasExpired );
        editors.apply();

        new AsyncTaskRunner().execute("3");
        mRecycler.setVisibility(View.GONE);
        return v;
    }



    //This holds/pauses the Main UI thread to Load data from Firestore, since firetore is asynchronous
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;


        protected String doInBackground(String... params) {

            try {
                int time = Integer.parseInt(params[0])*1000;
                loadData();
                Thread.sleep(time);

                resp = "Slept for " + params[0] + " seconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            noRequest.setVisibility(View.GONE);

            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();

            }

            if(seekers.isEmpty() && !sharedpreferences.getBoolean("hasAccepted",false) && !SecondLoad) {

                SecondLoad = true;
                if(sharedpreferences.getBoolean("hasExpired",false))
                {
                    noRequest.setText("No requests");
                    noRequest.setVisibility(View.VISIBLE);
                }
                else if(!isInTheLoop && !noRequests && !isBloodOver) {
                    new AsyncTaskRunner().execute("5");
                }
                else
                {
                    noRequest.setText("No requests");
                    noRequest.setVisibility(View.VISIBLE);
                }




            }



            else if(seekers.isEmpty() && !sharedpreferences.getBoolean("hasAccepted",false) && sharedpreferences.getBoolean("hasExpired",false) )
            {
                noRequest.setText("The Blood drives are over, No new requests");
                noRequest.setVisibility(View.VISIBLE);
            }

            else if(seekers.isEmpty() && !sharedpreferences.getBoolean("hasAccepted",false))
            {
                noRequest.setText("Error fetching data, please try again later");
                noRequest.setVisibility(View.VISIBLE);
            }

            else if(sharedpreferences.getBoolean("hasAccepted",false))
            {
                noRequest.setText("Ongoing Drive, No new requests will be shown");
                noRequest.setVisibility(View.VISIBLE);
            }

            else if (!seekers.isEmpty() && !sharedpreferences.getBoolean("hasAccepted",false) )
            {
                showList(seekerName, bottlesRequired, Date, LocationLat,LocationLon,nameOfPlace, seekers,Time,ContactNumber,Tokens, hasAcceptedRequest);

            }





        }


        @Override
        protected void onPreExecute() {
            if(!SecondLoad){
                progressDialog = ProgressDialog.show(getActivity(),
                        "Loading",
                        "Fetching Data");

            }

            else
            {

                    progressDialog = ProgressDialog.show(getActivity(),
                            "Loading",
                            "It is taking longer than usual, Please Wait");


            }

        }


        @Override
        protected void onProgressUpdate(String... text) {


        }
    }

    public void loadData()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        dateTime = sdf.format(new Date());
        try {
            currentDateTime = sdf.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        DocumentReference dref = db.collection("users").document(user.getUid());
        dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                             @Override
                                             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                 if (task.isSuccessful()) {
                                                     DocumentSnapshot documents = task.getResult();
                                                     if (documents.exists()) {


                                                         if(documents.getBoolean("isAvailable"))
                                                         {
                                                             available.setChecked(true);
                                                         }
                                                         else
                                                         {
                                                             available.setChecked(false);
                                                         }
                                                     }
                                                 }
                                             }
                                         });

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid()).collection("requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {





                            if(!exitLoop) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    noRequests = false;

                                    DocumentReference dref = db.collection("seekerRequest").document(document.getId());
                                    dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot documents = task.getResult();
                                                if (documents.exists()) {

                                                    //if blood drive is over, don't take the information
                                                    if (!documents.getBoolean("isBloodDriveOver")) {
                                                        isBloodOver = false;
                                                        datetimeFirestore = documents.getString("date") + " " + documents.getString("time");

                                                        try {
                                                            currentTimeDate = sdf.parse(datetimeFirestore);
                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }

                                                        if (currentDateTime.compareTo(currentTimeDate) <= 0) {
                                                            System.out.println("earlier");

                                                            isInTheLoop = true;
                                                            seekers.add(documents.getId());
                                                            seekerName.add(documents.getString("name"));
                                                            bottlesRequired.add(documents.getString("noOfBottles"));
                                                            Date.add(documents.getString("date"));
                                                            GeoPoint geoPoint = documents.getGeoPoint("location");
                                                            LocationLat.add((float)(geoPoint.getLatitude()));
                                                            LocationLon.add((float)(geoPoint.getLongitude()));
                                                            Time.add(documents.getString("time"));
                                                            ContactNumber.add(documents.getString("contactNumber"));
                                                            Tokens.add(documents.getString("token"));
                                                            nameOfPlace.add(documents.getString("nameOfPlace"));
                                                            requestedDonors = documents.getString("noOfRequests");
                                                            editors.putString("noOfRequests","requestedDonors"+requestedDonors);
                                                            editors.apply();
                                                            DocumentReference dref = db.collection("seekerRequest").document(documents.getId()).collection(sharedpreferences.getString("noOfRequests",null)).document(user.getUid());
                                                            dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if (document.exists()) {
                                                                            if (document.getBoolean("isAccepted") && !document.getBoolean("isCompleted")) {

                                                                                // add data to accepted list
                                                                                hasAcceptedRequest = true;
                                                                                editors.putBoolean("hasLeft",document.getBoolean("isComing"));
                                                                                editors.putBoolean("isDriveCompleted",document.getBoolean("isCompleted"));
                                                                                editors.putBoolean("hasAccepted",hasAcceptedRequest );
                                                                                editors.putString("SeekerName",documents.getString("name") );
                                                                                editors.putString("dateofDonation", documents.getString("date"));
                                                                                editors.putFloat("PlaceofDonationLat",(float)(documents.getGeoPoint("location").getLatitude()));
                                                                                editors.putFloat("PlaceofDonationLon",(float) (documents.getGeoPoint("location").getLongitude() ));
                                                                                editors.putString("NoOfBottles", documents.getString("noOfBottles"));
                                                                                editors.putString("seekerId",documents.getId() );
                                                                                editors.putString("contact",documents.getString("contactNumber") );
                                                                                editors.putString("time",documents.getString("time") );
                                                                                editors.putString("token",documents.getString("token") );
                                                                                editors.putString("nameOfPlace", documents.getString("nameOfPlace"));
                                                                                editors.apply();

                                                                                //remove data from requested list

                                                                                seekers.clear();
                                                                                seekerName.clear();
                                                                                bottlesRequired.clear();
                                                                                Date.clear();
                                                                                LocationLat.clear();
                                                                                LocationLon.clear();
                                                                                Time.clear();
                                                                                ContactNumber.clear();
                                                                                Tokens.clear();
                                                                                nameOfPlace.clear();
                                                                                exitLoop = true;

                                                                                Log.d(TAG, "your field exist");
                                                                            }

                                                                            else if(document.getBoolean("isCompleted"))
                                                                            {
                                                                                seekers.remove(documents.getId());
                                                                                seekerName.remove(documents.getString("name"));
                                                                                bottlesRequired.remove(documents.getString("noOfBottles"));
                                                                                Date.remove(documents.getString("date"));
                                                                                GeoPoint geoPoint = documents.getGeoPoint("location");
                                                                                LocationLat.remove(String.valueOf(geoPoint.getLatitude()));
                                                                                LocationLon.remove(String.valueOf(geoPoint.getLongitude()));
                                                                                Time.remove(documents.getString("time"));
                                                                                ContactNumber.remove(documents.getString("contactNumber"));
                                                                                nameOfPlace.remove(documents.getString("nameOfPlace"));
                                                                                Tokens.remove(documents.getString("token"));
                                                                            }

                                                                            else {


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

                                                                                Log.d(TAG, "your field does not exist");

                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });



                                                        }

                                                        else
                                                        {

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

                                                            editors.putBoolean("hasExpired",true );
                                                            editors.apply();
                                                        }
                                                    } else {


                                                        DocumentReference dref = db.collection("seekerRequest").document(documents.getId()).collection(sharedpreferences.getString("noOfRequests",null)).document(user.getUid());
                                                        dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                             @Override
                                                                                             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                 if (task.isSuccessful()) {
                                                                                                     DocumentSnapshot document = task.getResult();
                                                                                                     if (document.exists()) {
                                                                                                         if (document.getBoolean("isAccepted") && !document.getBoolean("isCompleted") && !document.getBoolean("isCancelled") && !document.getBoolean("isRejected") ) {

                                                                                                             isBloodOver = true;
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
                                                                                                 }
                                                                                             }
                                                                                         });




                                                                            //Create the filed
                                                    }
                                                }
                                            }

                                        }
                                    });

                                }

                            }

                            Log.d(TAG, "show list reached");

                        }

                        else
                        {
                            noRequests = true;
                        }
                    }
                });
    }


    public void showList(ArrayList<String> seekerName, ArrayList<String> bottlesRequired, ArrayList<String> Date, ArrayList<Float> LocationLat, ArrayList<Float> LocationLon, ArrayList<String> PlaceName, ArrayList<String> seekers,ArrayList<String> Time,ArrayList<String> ContactNumber,ArrayList<String> Tokens,Boolean hasAcceptedRequest) {
        mRecycler = (RecyclerView) v.findViewById(R.id.recylerviewDonor);
        mRecycler.setVisibility(View.VISIBLE);
        DonorListAdapter mDonorListAdapter = new DonorListAdapter(seekerName, bottlesRequired,Date,LocationLat,LocationLon,PlaceName,seekers,Time,ContactNumber,Tokens,hasAcceptedRequest, getContext());
        mRecycler.setAdapter(mDonorListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);
    }


}
