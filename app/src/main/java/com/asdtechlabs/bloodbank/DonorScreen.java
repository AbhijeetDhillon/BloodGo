package com.asdtechlabs.bloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.asdtechlabs.bloodbank.MainActivity.isGpsOn;
import static com.asdtechlabs.bloodbank.MainActivity.sharedpreferences;
import static com.asdtechlabs.bloodbank.PostSignIn.MyPREFERENCES;

public class DonorScreen extends Fragment implements View.OnClickListener {

    static Switch available;
    static TextView accepted, newRequests, noRequest;;
    View v,vs;
    static CardView cardView;
    static FrameLayout frame;
    static Boolean isAccepted;
    static RecyclerView mRecycler;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR";
    String SeekerName,dateofDonation,NoOfBottles,seekerId,contact,time,token;
    float PlaceofDonationLat,PlaceofDonationLon;

    static FrameLayout frameLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (!isNetworkConnected()) {
            v = inflater.inflate(R.layout.no_internet, container, false);
        } else
        {
            v = inflater.inflate(R.layout.fragment_donor_screen, container, false);
        vs = inflater.inflate(R.layout.donor_requests, container, false);//isSelectAll = false;
        List<DocumentSnapshot> myListOfDocuments;
        frame = v.findViewById(R.id.replace);
        noRequest = v.findViewById(R.id.noRequests);
        newRequests = v.findViewById(R.id.donor);
        cardView = (CardView) vs.findViewById(R.id.card_view_Donor);
        accepted = v.findViewById(R.id.acceptedRequest);
        available = v.findViewById(R.id.availabilitySwitch);
        available.setOnClickListener((View.OnClickListener) this);
        newRequests.setOnClickListener((View.OnClickListener) this);
        accepted.setOnClickListener((View.OnClickListener) this);


        loadFragment(new NewRequestDonation());
    }

        return v;

    }


    @Override
    public void onClick(View v) {
        AppCompatActivity activity;

        frame.setVisibility(View.GONE);
        noRequest.setVisibility(View.GONE);
        cardView.setVisibility(View.GONE);
//        mRecycler.setVisibility(View.VISIBLE);
        isAccepted = false;
        switch (v.getId()) {

            case R.id.donor:
                highlight();


                if(!sharedpreferences.getBoolean("hasAccepted",false) || sharedpreferences.getBoolean("isCompleted",false)) {

                    loadFragment(new NewRequestDonation());
                }

                else
                {

                    noRequest.setText("Ongoing Blood Drive, No new requests will be shown");
                    noRequest.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.acceptedRequest:

                cardView.setVisibility(View.VISIBLE);
                noRequest.setText("No Ongoing drive");
                noRequest.setVisibility(View.VISIBLE);
                isAccepted = true;
                highlight();


                DonorCardView donorCardView = new DonorCardView();
                Bundle args;

                SeekerName = sharedpreferences.getString("SeekerName",null);
                dateofDonation = sharedpreferences.getString("dateofDonation",null);
                PlaceofDonationLat = sharedpreferences.getFloat("PlaceofDonationLat",0);
                PlaceofDonationLon = sharedpreferences.getFloat("PlaceofDonationLon",0);
                NoOfBottles = sharedpreferences.getString("NoOfBottles",null);
                seekerId = sharedpreferences.getString("seekerId",null);
                contact = sharedpreferences.getString("contact",null);
                time = sharedpreferences.getString("time",null);
                token = sharedpreferences.getString("token",null);
                if(SeekerName!=null &&dateofDonation!=null&&PlaceofDonationLat!=0
                    &&NoOfBottles!=null&&seekerId!=null&&time!=null&&contact!=null) {
                    args = new Bundle();
                    noRequest.setVisibility(View.GONE);
                    donorCardView.setArguments(args);
                    loadFragment(new DonorCardView());
                }



                 break;

            case R.id.availabilitySwitch:

                if (!available.isChecked())
                {
                    db
                            .collection("users")
                            .document(user.getUid())
                            .update("isAvailable",false);
                }
                else
                {
                    db
                            .collection("users")
                            .document(user.getUid())
                            .update("isAvailable",true);
                }

        }
    }

    // This method highlights the New Request and Accepted Request
    public static void highlight()
    {
       if(isAccepted)
        {
            accepted.setTextAppearance(MyApplication.getAppContext(),R.style.Donors);
            accepted.setBackgroundResource(R.drawable.redhighlighter);
            newRequests.setTextAppearance(MyApplication.getAppContext(),R.style.Accepted);
            newRequests.setBackgroundResource(R.color.colorWhites);

        }

        else
        {
            newRequests.setTextAppearance(MyApplication.getAppContext(),R.style.Donors);
            newRequests.setBackgroundResource(R.drawable.redhighlighter);
            accepted.setTextAppearance(MyApplication.getAppContext(),R.style.Accepted);
            accepted.setBackgroundResource(R.color.colorWhites);
        }
    }




    private void loadFragment(Fragment fragment) {
// create a FragmentManager
        frame.setVisibility(View.VISIBLE);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment

        //fragmentTransaction.remove(fragment);
        fragmentTransaction.replace(R.id.replace, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


}