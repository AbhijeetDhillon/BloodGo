package com.asdtechlabs.bloodbank;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Objects;


import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import static com.asdtechlabs.bloodbank.PostSignIn.MyPREFERENCES;

public class SignIn extends AppCompatActivity implements FirebaseAuthProvider {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "Error" ;
    Boolean isPostsignIN;
    EditText phoneNumberField;
    String phoneNumber;
    ImageView logo_slogan,hands;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean isPresent;
    ProgressBar progressBar;
    Button logIn;
    LinearLayout conditions;
    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editorName;
    static Boolean isPostSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_sign_in);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        progressBar = findViewById(R.id.progress_bar);
        logo_slogan = findViewById(R.id.logoSlogan);
        hands = findViewById(R.id.hands);
        conditions = findViewById(R.id.acceptTerms);
        logIn = findViewById(R.id.phone_button);
        logo_slogan.getLayoutParams().height = (int) (0.30 * height);
        hands.getLayoutParams().height = (int) (0.5 * height);
        isPostSignIn = getIntent().getBooleanExtra("isPostSignIn",false);
        sharedPref = getSharedPreferences("Pref", Context.MODE_PRIVATE);
        phoneNumberField = findViewById(R.id.phone_number);



        if (auth.getCurrentUser() != null && !isPostSignIn) {
            progressBar.setVisibility(View.VISIBLE);
            checkifUserExists();

        }
        else
        {
            progressBar.setVisibility(View.GONE);
            conditions.setVisibility(View.VISIBLE);
            logIn.setVisibility(View.VISIBLE);
        }


    }




    public void ContinueSignIn(View view)
    {
        progressBar.setVisibility(View.VISIBLE);
        conditions.setVisibility(View.GONE);
        logIn.setVisibility(View.GONE);
                    startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(true)
                            .setTheme(R.style.BlackTheme)
                            .setAvailableProviders(Arrays.asList(
                                    //new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    // new AuthUI.IdpConfig.FacebookBuilder().build(),
                                    //new AuthUI.IdpConfig.TwitterBuilder().build(),
                                    //ew AuthUI.IdpConfig.EmailBuilder().build()))
                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                            //new AuthUI.IdpConfig.AnonymousBuilder().build()

                            .build(),
                    RC_SIGN_IN);





    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                checkifUserExists();



            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(SignIn.this,R.string.sign_in_cancelled,Toast.LENGTH_SHORT).show();
                    //showSnackbar(R.string.sign_in_cancelled);
                    this.finish();
                    System.exit(0);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(SignIn.this,R.string.no_internet_connection,Toast.LENGTH_SHORT).show();
                    // showSnackbar(R.string.no_internet_connection);
                    return;
                }

                Toast.makeText(SignIn.this,R.string.unknown_error,Toast.LENGTH_SHORT).show();
                // showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    public void checkifUserExists()
    {
        isPresent = false;

        user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            if (task.getResult().exists()) {

                                String name = task.getResult().getString("name");
                                editorName = sharedPref.edit();
                                editorName.putString("UserName",name);
                                editorName.apply();
                                Intent intent = new Intent(SignIn.this, MainActivity.class);

                                startActivity(intent);
                                Toast.makeText(SignIn.this, "Welcome Back " + name,
                                        Toast.LENGTH_SHORT).show();



                            }
                            else
                            {

                                Intent intent = new Intent(SignIn.this, PostSignIn.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(SignIn.this, "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }

    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
            moveTaskToBack(true);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            // finish activity
        } else {
            Toast.makeText(this, "Press back again to CANCEL Sign In",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }


}
