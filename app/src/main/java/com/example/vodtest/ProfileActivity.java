package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    String[] items = new String[]{"Username", "Email", "Sign Out"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        SharedPreferences prefs = getSharedPreferences("VOD", MODE_PRIVATE);
        String name = prefs.getString("name", "UNKNOWN");
        TextView welcome = findViewById(R.id.welcome);
        String welcomeString = "Welcome " + name + "!";
        welcome.setText(welcomeString);
        items[0] = "Username: " + prefs.getString("username", "UNKNOWN");
        items[1] = "Email: " + prefs.getString("email", "UNKNOWN");

        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (items[position] == "Sign Out") {
                AWSMobileClient.getInstance().signOut();
                Intent authIntent = new Intent(ProfileActivity.this, AuthenticationActivity.class);
                finish();
                startActivity(authIntent);
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    Intent a = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(a);
                    overridePendingTransition(0, 0);
                    break;
                case R.id.action_profile:
                    break;
                case R.id.action_activity3:
                    Intent b = new Intent(ProfileActivity.this, Activity3.class);
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
