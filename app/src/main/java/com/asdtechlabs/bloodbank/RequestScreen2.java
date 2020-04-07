package com.asdtechlabs.bloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static com.asdtechlabs.bloodbank.MainActivity.editor;
import static com.asdtechlabs.bloodbank.MainActivity.pref;


public class RequestScreen2 extends Fragment implements View.OnClickListener{
    ArrayList<String> donorName = new ArrayList<String>();
    ArrayList<String> donorDistance = new ArrayList<>();
    ArrayList<String> contactNumber = new ArrayList<>();
    ArrayList<String> acceptedDonorName = new ArrayList<String>();
    ArrayList<String> acceptedDonorDistance = new ArrayList<>();
    ArrayList<String> acceptedcontactNumber = new ArrayList<>();
    ArrayList<String> FireBaseTokens = new ArrayList<>();
    ArrayList<String> acceptedFireBaseTokens = new ArrayList<>();
    ArrayList<String> userIds = new ArrayList<>();
    ArrayList<String> accepteduserIds = new ArrayList<>();
    ArrayList<String> Track = new ArrayList<>();
    ArrayList<String> acceptedTrack = new ArrayList<>();
    TextView request,track,requestAll,changePreferences, noDonorFound;
    static TextView donor,accepted;
    ImageView call;
    View v,vs;
    CheckBox selectAll;
    CardView cardView;
    static Boolean isAccepted, isSelectAll, isCoditionClear, isRequestedAll;
    FirebaseUser user;
    FirebaseFirestore db;
    Double donorLon,donorLat;
    GeoPoint seekerLocation;
    Boolean SecondLoad = false, hasEntered = false,hasClearedQuery= false,isInLoopAccepted,query2=false,query3=false;
    String TAG="XXXXXXXXXXXXXXXXXXXXXXXXXX";
    String user1;
    float [] dist;
    float distanceOfDonor;
    RecyclerView mRecycler;
    Boolean isNewDrive;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment

        if(!isNetworkConnected())
        {
            v = inflater.inflate(R.layout.no_internet, container, false);
        }

        else {
            v = inflater.inflate(R.layout.fragment_request_screen2, container, false);
            vs = inflater.inflate(R.layout.request_list, container, false);
            isAccepted = false;
            isSelectAll = false;
            isRequestedAll = false;
            accepted = v.findViewById(R.id.accepted);
            donor = v.findViewById(R.id.donor);
            changePreferences = v.findViewById(R.id.changePreferences);
            noDonorFound = v.findViewById(R.id.noDonor);
            noDonorFound.setVisibility(View.GONE);
            request = vs.findViewById(R.id.requestBlood);
            call = vs.findViewById(R.id.callButton);
            track = vs.findViewById(R.id.track);
            selectAll = v.findViewById(R.id.checkbox);
            selectAll.setVisibility(View.VISIBLE);
            requestAll = v.findViewById(R.id.requestall);
            cardView = (CardView) v.findViewById(R.id.card_view);
            user = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();
            mRecycler = (RecyclerView) v.findViewById(R.id.recylerview);

            if (MainActivity.RequestPref.getString("noOfRequests", null) == null) {
                db.collection("seekerRequest").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                        if (task.isSuccessful() && MainActivity.RequestPref.getString("noOfRequests", null) == null) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {


                                editor = pref.edit();

                                editor.putString("bloodGroup", document.getString("bloodGroupRequired"));
                                editor.putString("distance", document.getString("distance"));
                                GeoPoint geoPoint = document.getGeoPoint("location");
                                editor.putFloat("geopointLat", (float) geoPoint.getLatitude());
                                editor.putFloat("geopointLon", (float) geoPoint.getLongitude());
                                editor.apply();

                                MainActivity.editorRequest = MainActivity.RequestPref.edit();
                                MainActivity.editorRequest.putString("noOfRequests", "requestedDonors" + document.getString("noOfRequests"));
                                MainActivity.editorRequest.apply();
                                new AsyncTaskRunner().execute("3");
                            }
                        } else {
                            new AsyncTaskRunner().execute("3");
                        }

                    }
                });
            } else {
                new AsyncTaskRunner().execute("3");
            }


            Button endDrive = v.findViewById(R.id.endDrive);
            endDrive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NewDrive();
                }
            });


            accepted.setOnClickListener((View.OnClickListener) this);
            selectAll.setOnClickListener((View.OnClickListener) this);
            donor.setOnClickListener((View.OnClickListener) this);
            changePreferences.setOnClickListener((View.OnClickListener) this);
            requestAll.setOnClickListener((View.OnClickListener) this);

        }

        return v;

    }






    @Override
    public void onClick(View v) {
        isAccepted = false;
        isSelectAll=false;
        isRequestedAll = false;
        noDonorFound.setVisibility(View.GONE);


        switch (v.getId()) {

            case R.id.donor:

                highlight();


                if(donorName.size()>0&&userIds.size()>0) {

                    selectAll.setVisibility(View.VISIBLE);
                    requestAll.setVisibility(View.GONE);
                    showList(donorName, donorDistance, userIds, contactNumber, FireBaseTokens, isAccepted, isSelectAll, isRequestedAll, Track);

                }

                else
                {
                    selectAll.setVisibility(View.GONE);
                    mRecycler.setVisibility(View.GONE);
                    noDonorFound.setText("No Donors Available, Try increasing the distance");
                    noDonorFound.setVisibility(View.VISIBLE );
                }
                break;

            case R.id.accepted:
                isAccepted = true;
                highlight();
                selectAll.setChecked(false);
                selectAll.setVisibility(View.GONE);

                //AcceptedUsers();

                requestAll.setVisibility(View.GONE);

                if(acceptedDonorName.size()>0&&acceptedDonorDistance.size()>0&&accepteduserIds.size()>0) {

                    showList(acceptedDonorName, acceptedDonorDistance, accepteduserIds, acceptedcontactNumber, acceptedFireBaseTokens, isAccepted, isSelectAll, isRequestedAll, acceptedTrack);
                }
                else
                {
                    mRecycler.setVisibility(View.GONE);
                    noDonorFound.setText("No Accepted Requests");
                    noDonorFound.setVisibility(View.VISIBLE );
                }

                break;

            case R.id.checkbox:


                selectAll.setVisibility(View.VISIBLE);
                if(selectAll.isChecked()) {

                    requestAll.setVisibility(View.VISIBLE);
                    isSelectAll = true;
                    showList(donorName, donorDistance,userIds,contactNumber,FireBaseTokens,isAccepted,isSelectAll,isRequestedAll,Track);

                }

                else
                {

                    requestAll.setVisibility(View.GONE);
                    isSelectAll = false;
                    showList(donorName,donorDistance,userIds,contactNumber,FireBaseTokens,isAccepted,isSelectAll,isRequestedAll,Track);
                }
                break;

            case R.id.changePreferences:

                FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
                fragmentTransaction.replace(R.id.frameLayout, new RequestScreen());
                fragmentTransaction.commit(); // save the changes

                break;

            case R.id.requestall:
                isRequestedAll = true;
                showList(donorName,donorDistance,userIds,contactNumber,FireBaseTokens,isAccepted,isSelectAll,isRequestedAll,Track);
        }
    }

    public void showList(ArrayList<String> donorName, ArrayList<String> donorDistance, ArrayList<String> userid, ArrayList<String> contacts,ArrayList<String> tokens, Boolean isAccepted, Boolean isSelectAll, Boolean isEvertDonorRequested ,ArrayList<String> tracks)
    {
        mRecycler.setVisibility(View.VISIBLE);
        RequestListAdapter mRequestListAdapter = new RequestListAdapter(donorName, donorDistance,userid,contacts,tokens, getContext(), isAccepted, isSelectAll,isEvertDonorRequested,tracks);
        mRecycler.setAdapter(mRequestListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);

    }

    public static void highlight()
    {
        if(isAccepted)
        {
            accepted.setTextAppearance(MyApplication.getAppContext(),R.style.Donors);
            accepted.setBackgroundResource(R.drawable.redhighlighter);
            donor.setTextAppearance(MyApplication.getAppContext(),R.style.Accepted);
            donor.setBackgroundColor(Color.parseColor("#FFFFFF"));

        }

        else
        {
            donor.setTextAppearance(MyApplication.getAppContext(),R.style.Donors);
            donor.setBackgroundResource(R.drawable.redhighlighter);
            accepted.setTextAppearance(MyApplication.getAppContext(),R.style.Accepted);
            accepted.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }

    private boolean getDistanceInKm(Double lat1, Double lng1,GeoPoint p2, String Distance) {

        double latOne = lat1 / 1e6;
        double lngTwo = lng1 / 1e6;
        double lat2 = ((double)p2.getLatitude()) / 1e6;
        double lng2 = ((double)p2.getLongitude()) / 1e6;
        editor = pref.edit();
        editor.putFloat("seekerLocationLat", (float) lat2);
        editor.putFloat("seekerLocationLon", (float) lng2);
        editor.apply();

        float distance = Float.parseFloat(Distance);
        dist = new float[1];
        Log.i("destination coordinates", "Latitude:" + lat2 + ", Longitude: " + lng2);
        Location.distanceBetween(latOne, lngTwo, lat2, lng2, dist);
        distanceOfDonor = dist[0] * 1000;
        return distanceOfDonor <= distance;
    }

    class AsyncTaskRunner extends AsyncTask<String, String, String> {

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

            progressDialog.dismiss();

            if(!hasClearedQuery)
            {
                selectAll.setVisibility(View.GONE);
                noDonorFound.setText("No Donor Available");
                noDonorFound.setVisibility(View.VISIBLE );
                selectAll.setVisibility(View.GONE);
            }

            else if(hasEntered && !userIds.isEmpty() && !donorDistance.isEmpty() && !donorName.isEmpty()) {

                    showList(donorName, donorDistance, userIds, contactNumber,FireBaseTokens, isAccepted, isSelectAll, isRequestedAll,Track);
            }
            else if(hasEntered && userIds.isEmpty() && donorDistance.isEmpty() && donorName.isEmpty())
            {
                if(!SecondLoad) {
                    SecondLoad = true;
                    new AsyncTaskRunner().execute("5");
                }
                else
                {
                    selectAll.setVisibility(View.GONE);
                    noDonorFound.setText("No Donor Available");
                    noDonorFound.setVisibility(View.VISIBLE );
                }


            }

            else if(!hasEntered && hasClearedQuery)
            {
                selectAll.setVisibility(View.GONE);
                noDonorFound.setText("No Donors Available, Try increasing the distance");
                noDonorFound.setVisibility(View.VISIBLE );


            }

            else if(acceptedDonorName.size()>0&&acceptedDonorDistance.size()>0&&accepteduserIds.size()>0)
            {
                selectAll.setVisibility(View.GONE);
                noDonorFound.setText("No Donors Available, Check the Accepted Section for progress");
                noDonorFound.setVisibility(View.VISIBLE );
            }

            else
            {
                selectAll.setVisibility(View.GONE);
                noDonorFound.setText("Something went wrong, please try again later");
                noDonorFound.setVisibility(View.VISIBLE );
            }
        }


        @Override
        protected void onPreExecute() {
            if(!SecondLoad){
                progressDialog = ProgressDialog.show(getActivity(),
                        "Loading...",
                        "Getting your Donor List");

            }

            else
            {
                progressDialog = ProgressDialog.show(getActivity(),
                        "Loading...",
                        "It is taking longer than usual, Please Wait");

            }

        }


        @Override
        protected void onProgressUpdate(String... text) {


        }
    }

    private void loadData() {

        String bloodgroup =  pref.getString("bloodGroup", null); // getting String

        donorLat = (double) pref.getFloat("geopointLat", 0);
        donorLon = (double) pref.getFloat("geopointLon", 0);



        CollectionReference cref=db.collection("users");
        Query q1=cref.whereEqualTo("bloodGroup",bloodgroup).whereEqualTo("isAvailable",true);
        CollectionReference cref2=db.collection("seekerRequest").document(user.getUid()).collection(MainActivity.RequestPref.getString("noOfRequests",null));
        Query q2=   cref2.whereEqualTo("isAccepted",true).whereEqualTo("isCancelled",false);
        Query q3 = cref2.whereEqualTo("isRejected",true);
        Query q4 = cref2.whereEqualTo("isCancelled",true);
        q1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {


                        //collect ids,name & distance of person matching the search
                        hasClearedQuery = true;
                        seekerLocation = document.getGeoPoint("location");

                        assert seekerLocation != null;

                        isCoditionClear = getDistanceInKm(donorLat,donorLon,seekerLocation,pref.getString("distance","10"));

                        if (!document.getString("contactNumber").equals(user.getPhoneNumber()) && isCoditionClear)
                        {
                            hasEntered = true;

                            userIds.add(document.getId());
                            donorName.add(document.getString("name"));
                            FireBaseTokens.add(document.getString("token"));
                            donorDistance.add(String.valueOf((int)distanceOfDonor)+" kms");
                            contactNumber.add("0");
                            Track.add("");



                        }

                    }

                }
//

                else {


                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        q2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    query2 = true;

                    for (QueryDocumentSnapshot documents : task.getResult()) {
                        if (documents.getBoolean("isAccepted") && !documents.getBoolean("isCancelled") ) {
                            isInLoopAccepted=true;
                            acceptedList(documents.getId());
                        }
                        else
                        {

                            isInLoopAccepted = false;
                        }

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        q3.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {


            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    query3 = true;

                    for (QueryDocumentSnapshot documents : task.getResult()) {

                        if (user1 == null || !user1.equals(documents.getId())) {
                            user1 = documents.getId();
                            isInLoopAccepted = false;
                            acceptedList(user1);

                        }

                    }
                }
            }
        });

        q4.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {


            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    query3 = true;

                    for (QueryDocumentSnapshot documents : task.getResult()) {

                        if (user1 == null || !user1.equals(documents.getId())) {
                            user1 = documents.getId();
                            isInLoopAccepted = false;
                            acceptedList(user1);

                        }

                    }
                }
            }
        });

        System.out.println("Returning");










    }

    public void acceptedList(String documentId)
    {


        db.collection("users")
                .document(documentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {



                                DocumentSnapshot document = task.getResult();
                                if(isInLoopAccepted) {


                                    accepteduserIds.add(document.getId());
                                    acceptedDonorName.add(document.getString("name"));
                                    acceptedDonorDistance.add("has accepted request");
                                    acceptedcontactNumber.add(document.getString("contactNumber"));
                                    acceptedFireBaseTokens.add(document.getString("token"));
                                    acceptedTrack.add("Track");

                                    userIds.remove(document.getId());
                                    donorName.remove(document.getString("name"));
                                    donorDistance.remove(String.valueOf((int) distanceOfDonor));
                                    contactNumber.remove("0");
                                    FireBaseTokens.remove(document.getString("token"));
                                    Track.remove("");
                                }

                                else
                                {
                                    userIds.remove(document.getId());
                                    donorName.remove(document.getString("name"));
                                    donorDistance.remove(String.valueOf((int) distanceOfDonor));
                                    contactNumber.remove("0");
                                    FireBaseTokens.remove(document.getString("token"));
                                    Track.remove("");
                                }


                        }
                    }
                });


    }

    public void NewDrive()
    {





            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
// ...Irrelevant code for customizing the buttons and title
            LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.new_request_warning, null);
            dialogBuilder.setView(dialogView);
            final AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();

            TextView newdrive = dialogView.findViewById(R.id.continueDonation);
            TextView close = dialogView.findViewById(R.id.close);

            newdrive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    isNewDrive = true;


                    db.collection("seekerRequest").document(user.getUid()).update("isBloodDriveOver", true);
                    db.collection("seekerRequest").document(user.getUid()).update("isSearchedBlood", false);
                    editor = pref.edit();
                    editor.clear().apply();
                    MainActivity.editorRequest = MainActivity.RequestPref.edit();
                    MainActivity.editorRequest.clear().apply();
                    FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
                    fragmentTransaction.replace(R.id.frameLayout, new RequestScreen());
                    fragmentTransaction.commit();


                    alertDialog.dismiss();

                }
            });

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    isNewDrive = false;
                    alertDialog.dismiss();

                }
            });



    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }



}
