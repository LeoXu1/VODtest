package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class MyProfileActivity extends AppCompatActivity {

    String[] items = new String[]{"Downloads", "Email", "Sign Out"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myprofile_activity);

        //Get user info
        SharedPreferences prefs = getSharedPreferences("VOD", MODE_PRIVATE);
        String name = prefs.getString("name", "UNKNOWN");
        String profile = prefs.getString("profile", "UNKNOWN");
        ImageView profileImage = findViewById(R.id.profilePicture);
        Picasso.get().load(profile).into(profileImage);
        TextView welcome = findViewById(R.id.welcome);
        String welcomeString = "Welcome " + name + "!";
        welcome.setText(welcomeString);
        items[1] = "Email: " + prefs.getString("email", "UNKNOWN");

        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (items[position] == "Sign Out") { //Sign out
                AWSMobileClient.getInstance().signOut();
                Intent authIntent = new Intent(MyProfileActivity.this, AuthenticationActivity.class);
                finish();
                startActivity(authIntent);
            }
            if (items[position] == "Downloads") { //List Downloads
                Intent downloadsIntent = new Intent(MyProfileActivity.this, DownloadsActivity.class);
                startActivity(downloadsIntent);
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    Intent a = new Intent(MyProfileActivity.this, HomeActivity.class);
                    startActivity(a);
                    overridePendingTransition(0, 0);
                    break;
                case R.id.action_profile:
                    break;
                case R.id.action_activity3:
                    Intent b = new Intent(MyProfileActivity.this, Activity3.class);
                    startActivity(b);
                    overridePendingTransition(0, 0);
                    break;
            }
            return true;
        });
        MenuItem item = navigation.getMenu().findItem(R.id.action_profile);
        item.setChecked(true);


    }
}
