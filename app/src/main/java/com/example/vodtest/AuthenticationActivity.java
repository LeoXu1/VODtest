package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

import java.util.HashMap;

public class AuthenticationActivity extends AppCompatActivity {

    private final String TAG = AuthenticationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //If the user has logged in before
        if(AWSMobileClient.getInstance().getConfiguration() != null) {
            UserStateDetails userStateDetails = AWSMobileClient.getInstance().currentUserState();
            showSignInForUser(userStateDetails);
        } else { //If no previous login is detected

            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    showSignInForUser(userStateDetails);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
        }
    }

    private void showSignInForUser(UserStateDetails userStateDetails) {
        Log.i(TAG, userStateDetails.getUserState().toString());
        switch (userStateDetails.getUserState()){
            case SIGNED_IN:
                Intent i = new Intent(AuthenticationActivity.this, HomeActivity.class);
                try {

                    //Put user info into SharedPreferences
                    String name = AWSMobileClient.getInstance().getUserAttributes().get("given_name");
                    String email = AWSMobileClient.getInstance().getUserAttributes().get("email");
                    SharedPreferences prefs = getSharedPreferences("VOD", MODE_PRIVATE);
                    prefs.edit().putString("name", name).apply();
                    prefs.edit().putString("email", email).apply();
                    if (AWSMobileClient.getInstance().getUserAttributes().get("profile") == null) {
                        //Add blank profile picture
                        HashMap<String, String> map = new HashMap<>();
                        map.put("profile", "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png");
                        AWSMobileClient.getInstance().updateUserAttributes(map);
                        String profile = AWSMobileClient.getInstance().getUserAttributes().get("profile");
                        prefs.edit().putString("profile", profile).apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(i);
                break;
            case SIGNED_OUT:
                showSignIn();
                break;
            default:
                AWSMobileClient.getInstance().signOut();
                showSignIn();
                break;
        }
    }

    private void showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder().nextActivity(HomeActivity.class).build(),
                    new Callback<UserStateDetails>() {
                        @Override
                        public void onResult(UserStateDetails result) {
                            Log.d(TAG, "Showing Signin UI: ");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: ", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
