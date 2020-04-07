package com.asdtechlabs.bloodbank;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.Map;

import static com.asdtechlabs.bloodbank.SignIn.sharedPref;

/**
 * Created by Abhijeet on 8/21/2019.
 */

public class RequestListAdapter extends RecyclerView.Adapter {
    String TAG = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    ArrayList<String> Distance;
    ArrayList<String> Track;
    ArrayList<String> Title;
    ArrayList<String> Contact;
    ArrayList<String> UserIds;
    ArrayList<String> FireBaseTokens;
    ArrayList<String> storeCount = new ArrayList<>();
    Context context;
    Boolean isRequestAccepted,isListSelected,isRequestedAll;


    CardView cardView;
    int selectrequester = -1;
    FirebaseUser user;
    FirebaseFirestore db;
    LinearLayout seekerRequested,donorAccepted,donorLeaving;
    public RequestListAdapter(ArrayList<String> titles, ArrayList<String> distances,ArrayList<String> userIds,ArrayList<String> contacts,ArrayList<String> tokens , Context appContext, Boolean isAccepted,Boolean isSelectAll, Boolean isRequestedAllDonors,ArrayList<String> tracks) {
        Distance = distances;
        Title = titles;
        context = appContext;
        isRequestAccepted = isAccepted;
        isListSelected = isSelectAll;
        UserIds = userIds;
        isRequestedAll = isRequestedAllDonors;
        storeCount.add(String.valueOf(-450));
        Contact = contacts;
        FireBaseTokens = tokens;
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        Track = tracks;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_list, viewGroup, false);
        return new RequestListHolder(view);
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {

        //viewHolder.setIsRecyclable(false);

        String requestText;
        if(!storeCount.isEmpty() && storeCount.contains(String.valueOf(i))) {

            requestText = "Requested";

        }

        else
        {
            requestText = "Request";

        }

        String distance = Distance.get(i);
        String title = Title.get(i);
        String contact = Contact.get(i);
        String userId = UserIds.get(i);
        String tokens = FireBaseTokens.get(i);
        String track = Track.get(i);
        ((RequestListHolder) viewHolder).bind(title,distance,contact,userId,tokens,track);


    }

    @Override
    public int getItemCount() {
        return Title.size();
    }

    private class RequestListHolder extends RecyclerView.ViewHolder {
        TextView title, distance, request,trackDonor;
        ImageView callButton;
        Boolean isRequested = false;

        RequestListHolder(View itemView) {
            super(itemView);
            callButton = itemView.findViewById(R.id.callButton);
            trackDonor = (TextView) itemView.findViewById(R.id.track);
            title = (TextView) itemView.findViewById(R.id.title);
            distance = (TextView) itemView.findViewById(R.id.distance);
            request = itemView.findViewById(R.id.requestBlood);
            cardView = itemView.findViewById(R.id.card_view);



        }

        void bind(String name, final String Donordistance, String contact, String userids, String token,String tracker) {

            title.setText(name);
            distance.setText(Donordistance);
            trackDonor.setText(tracker);
            //trackDonor.setVisibility(View.GONE);
            request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    user = FirebaseAuth.getInstance().getCurrentUser();
                    db = FirebaseFirestore.getInstance();
                    selectrequester = getAdapterPosition();


                    DocumentReference docIdRef = db.collection("seekerRequest").document(user.getUid()).collection(MainActivity.RequestPref.getString("noOfRequests",null)).document(userids);
                    docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    deleteUser(userids);
                                    request.setText("Request");
                                    request.setTextColor(Color.parseColor("#FFFB0F0E"));

                                }

                                else
                                {
                                    addUser(userids);
                                    SendNotification sendNotification = new SendNotification();
                                    sendNotification.getAppReadyForNotification(sharedPref.getString("UserName","Seeker"),"has requested blood",token);
                                    request.setText("Requested");
                                    request.setTextColor(Color.parseColor("#FF898989"));
                                }
                            }}});


                }
            });




            if (isRequestAccepted) {
                trackDonor.setEnabled(false);
                trackDonor.setTextColor(Color.parseColor("#D3D3D3"));
                callButton.setVisibility(View.VISIBLE);
                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       // Intent intent = new Intent(Intent.ACTION_DIAL);
                        Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                                "tel", contact, null));
                        phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MyApplication.getAppContext().startActivity(phoneIntent);

                    }});
                DocumentReference dref=db.collection("seekerRequest").document(user.getUid()).collection(MainActivity.RequestPref.getString("noOfRequests",null)).document(userids);
                dref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                    if (document.getBoolean("isCompleted")){

                                        distance.setText("has Donated Blood");
                                        trackDonor.setVisibility(View.GONE);
                                        callButton.setVisibility(View.GONE);



                                    Log.d(TAG, "your field exist");
                                } else if (document.getBoolean("isComing")) {


                                            //Write a code here for getting the donor geopoint to track
                                            distance.setText("has left for Donation");
                                            trackDonor.setEnabled(true);
                                            Log.d("ProgressXXX", String.valueOf(getAdapterPosition()));
                                            trackDonor.setTextColor(Color.parseColor("#fb0f0e"));
                                            trackDonor.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {


                                                Intent intent = new Intent(context , MapsActivity.class);
                                                intent.putExtra("DonorId",userids);
                                                intent.putExtra("SeekerId",user.getUid());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent);
                                            }
                                        });

                                    //Create the filed
                                }


                            }
                        }
                    }
                });
                request.setTextColor(Color.parseColor("#ffffff"));
            }

            else
            {
                if(isRequestedAll)
                {
                    addUser(userids);
                    SendNotification sendNotification = new SendNotification();
                    sendNotification.getAppReadyForNotification(sharedPref.getString("UserName","Seeker"),"has requested blood",token);
                    request.setText("Requested");
                    request.setTextColor(Color.parseColor("#FF898989"));
                }

                else if(isListSelected)
                {
                    cardView.setBackgroundResource(R.drawable.request_all);
                    callButton.setVisibility(View.GONE);

                    request.setTextColor(Color.parseColor("#ffffff"));


                }
                else {


                    //display users and show if requested or not

                    check(userids);


                }
            }







            Log.d("Progress","Reached Bind");
        }

        public void addUser(String userids)
        {

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("isAccepted", false);
            userDetails.put("isCancelled", false);
            userDetails.put("isComing", false);
            userDetails.put("isRejected", false);
            userDetails.put("isCompleted", false);

            db.collection("seekerRequest").document(user.getUid()).collection(MainActivity.RequestPref.getString("noOfRequests",null)).document(userids)
                    .set(userDetails)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written! requester side");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document requester side", e);
                        }
                    });

            // Adding details to donor side

            Map<String, Object> data = new HashMap<>();

            db.collection("users").document(userids).collection("requests").document(user.getUid())
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written! donor side");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document donor side", e);
                        }
                    });







            //Adding details to requester side


        }

        public void check(String userID)


        {
            final String[] valueExists = new String[1];
            DocumentReference docIdRef = db.collection("seekerRequest").document(user.getUid()).collection(MainActivity.RequestPref.getString("noOfRequests",null)).document(userID);
            docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        callButton.setVisibility(View.GONE);
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {


                            request.setText("Requested");
                            request.setTextColor(Color.parseColor("#FF898989"));

                        }

                        else
                        {
                            request.setText("Request");
                            request.setTextColor(Color.parseColor("#FFFB0F0E"));
                        }
                    }}});



        }

        public void deleteUser(String userids)
        {
            //Deleting details to requester side

            db.collection("seekerRequest").document(user.getUid()).collection(MainActivity.RequestPref.getString("noOfRequests",null)).document(userids)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted! requester side");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document requester side", e);
                        }
                    });

            // Deleting details to donor side

            db.collection("users").document(userids).collection("requests").document(user.getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted! donor side");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document donor side", e);
                        }
                    });


        }


    }


}
