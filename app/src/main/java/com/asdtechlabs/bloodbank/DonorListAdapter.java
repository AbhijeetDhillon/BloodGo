package com.asdtechlabs.bloodbank;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import static com.asdtechlabs.bloodbank.MainActivity.editors;
import static com.asdtechlabs.bloodbank.MainActivity.sharedpreferences;
import static com.asdtechlabs.bloodbank.SignIn.sharedPref;

/**
 * Created by Abhijeet on 8/21/2019.
 */

public class DonorListAdapter extends RecyclerView.Adapter {

    ArrayList<String> SeekerName;
    ArrayList<String> NoOfBottles;
    ArrayList<Float> PlaceofDonationLat;
    ArrayList<Float> PlaceofDonationLon;
    ArrayList<String> dateofDonation;
    ArrayList<String> seekerId;
    ArrayList<String> ContactNumber;
    ArrayList<String> Time;
    ArrayList<String> Token;
    ArrayList<String> DonationPlace;
    Context context;
    Boolean isRequestAccepted,isListSelected;
    ImageView callButton;
    TextView trackDonor,request;
    FirebaseUser user;
    FirebaseFirestore db;
    CardView cardView;
    int globalPosition, selectSeker;
    String TAG = "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS";
    TextView newRequests,accepted;
    LinearLayout seekerRequested,donorAccepted,donorLeaving;
    public DonorListAdapter(ArrayList<String> seekerName, ArrayList<String> noOfbottles , ArrayList<String> Date , ArrayList<Float> LocationLat,ArrayList<Float> LocationLon,ArrayList<String> PlaceOfDonationName,ArrayList<String> seekerIds,ArrayList<String> time,ArrayList<String> contactno,ArrayList<String> token,Boolean hasAcceptedRequest ,Context appContext) {
        SeekerName = seekerName;
        NoOfBottles = noOfbottles;
        dateofDonation=Date;
        PlaceofDonationLat = LocationLat;
        PlaceofDonationLon = LocationLon;
        DonationPlace = PlaceOfDonationName;
        seekerId=seekerIds;
        context = appContext;
        ContactNumber = contactno;
        Time = time;
        isRequestAccepted = hasAcceptedRequest;
        user= FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        Token = token;
       // isRequestAccepted = isAccepted;
       // isListSelected = isSelectAll;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.donor_requests, viewGroup, false);
        return new RequestListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        String date = dateofDonation.get(i);
        String seekerName = SeekerName.get(i);
        float locationLat = PlaceofDonationLat.get(i);
        float locationLon = PlaceofDonationLon.get(i);
        String noOfBottles = NoOfBottles.get(i);
        String seekerid = seekerId.get(i);
        String time = Time.get(i);
        String contact = ContactNumber.get(i);
        String token = Token.get(i);
        String placeOfDonation = DonationPlace.get(i);
        selectSeker = i;
        ((RequestListHolder) viewHolder).bind(seekerName,date,locationLat,locationLon,noOfBottles,seekerid,time,contact,token,placeOfDonation);
    }

    @Override
    public int getItemCount() {
        return SeekerName.size();
    }

    private class RequestListHolder extends RecyclerView.ViewHolder {
        TextView nameOfSeeker, showLocation, date, bottles,acceptRequest,declineRequest,time,nameOfPLace;
        CardView cardViewofRequest;

        RequestListHolder(View itemView) {
            super(itemView);
            callButton = itemView.findViewById(R.id.callButton);
            nameOfSeeker = (TextView) itemView.findViewById(R.id.seekerName);
            //distance = (TextView) itemView.findViewById(R.id.location);
            nameOfPLace = itemView.findViewById(R.id.nameOfPlace);
            showLocation = itemView.findViewById(R.id.showOnMap);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);;
            bottles = itemView.findViewById(R.id.noOfBottles);
            acceptRequest = itemView.findViewById(R.id.acceptRequest);
            declineRequest = itemView.findViewById(R.id.declineRequest);
            cardViewofRequest = itemView.findViewById(R.id.card_view_Donor);
            newRequests = itemView.findViewById(R.id.donor);
            accepted = itemView.findViewById(R.id.acceptedRequest);
            seekerRequested = itemView.findViewById(R.id.donorRequested);
            donorAccepted = itemView.findViewById(R.id.donorAccepted);
            donorLeaving = itemView.findViewById(R.id.donorDonated);
        }

        void bind(String seekerName, final String dateofDonation, float locationLat,float locationLon, String noOfBottles, String seekersId,String timeofDonation,String contactNo,String token,String nameOfPlace) {

            nameOfSeeker.setText(seekerName);
            //distance.setText("null");
            nameOfPLace.setText(nameOfPlace);
            date.setText(dateofDonation);
            time.setText(timeofDonation);
            bottles.setText(noOfBottles);

            showLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String strUri = "http://maps.google.com/maps?q=loc:" + locationLat + "," + locationLon + " (" + "Donation Destination" + ")";
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

                    context.startActivity(intent);
                }
            });

            acceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!isRequestAccepted) {

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
// ...Irrelevant code for customizing the buttons and title
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View dialogView = inflater.inflate(R.layout.donor_condition_dialog, null);
                        dialogBuilder.setView(dialogView);
                        final AlertDialog alertDialog = dialogBuilder.create();
                        alertDialog.show();

                        TextView continueDonation = dialogView.findViewById(R.id.continueDonation);
                        TextView close = dialogView.findViewById(R.id.close);

                        continueDonation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                db.collection("seekerRequest").document(seekersId).collection(sharedpreferences.getString("noOfRequests",null)).document(user.getUid()).update("isAccepted", true);
                                String name = sharedPref.getString("UserName","A Donor");
                                SendNotification sendNotification = new SendNotification();
                                sendNotification.getAppReadyForNotification(name,"has accepted your request",token);

                                globalPosition = getAdapterPosition();
                                notifyDataSetChanged();

                                alertDialog.dismiss();
                                showAccepted(v);
                            }
                        });

                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                alertDialog.dismiss();

                            }
                        });

                    }

                    else
                    {
                        Toast.makeText(MyApplication.getAppContext(),"Ongoing Drive, Cannot Accept New Requests",Toast.LENGTH_LONG).show();
                    }
            }});

            declineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    db.collection("seekerRequest").document(seekersId).collection(sharedpreferences.getString("noOfRequests",null)).document(user.getUid()).update("isRejected",true);
                    db.collection("users").document(user.getUid()).collection("requests").document(seekersId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                declineRequest.setText("Request Declined");
                acceptRequest.setVisibility(View.GONE);

                }
            });



            Log.d("Progress","Reached Bind");
        }
    }

    public void showAccepted(View v)
    {
//        accepted.setTextAppearance(getContext(), R.style.Donors);
//        accepted.setBackgroundResource(R.drawable.redhighlighter);
//        newRequests.setTextAppearance(getContext(), R.style.Accepted);
//        newRequests.setBackgroundColor(Color.parseColor("#FFFAFAFA"));

//        if(globalPosition == selectSeker)
//        {



            DonorCardView donorCardView = new DonorCardView();
            editors.putBoolean("hasAccepted",true );
            editors.putString("SeekerName",SeekerName.get(globalPosition) );
            editors.putString("dateofDonation", dateofDonation.get(globalPosition));
            editors.putFloat("PlaceofDonationLat",PlaceofDonationLat.get(globalPosition) );
            editors.putFloat("PlaceofDonationLon",PlaceofDonationLon.get(globalPosition) );
            editors.putString("NoOfBottles", NoOfBottles.get(globalPosition));
            editors.putString("seekerId",seekerId.get(globalPosition) );
            editors.putString("contact", ContactNumber.get(globalPosition));
            editors.putString("time",Time.get(globalPosition) );
            editors.putString("token",Token.get(globalPosition));
            editors.apply();
            Bundle args = new Bundle();
            args.putString("seekerName", SeekerName.get(globalPosition));
            args.putString("date", dateofDonation.get(globalPosition));
            //args.putString("location", PlaceofDonation.get(globalPosition));
            args.putString("bottles", NoOfBottles.get(globalPosition));
            args.putString("seekerId",seekerId.get(globalPosition));
            args.putString("time",Time.get(globalPosition));
            args.putString("contactNo",ContactNumber.get(globalPosition));
            donorCardView.setArguments(args);
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            RecyclerView recyclerView = v.findViewById(R.id.recylerviewDonor);
//            recyclerView.setVisibility(View.GONE);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.replace, donorCardView).commit();

//        }


    }
}
