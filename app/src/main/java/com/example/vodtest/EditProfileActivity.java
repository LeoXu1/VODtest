package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.UpdateUserInput;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText editNameText = findViewById(R.id.editTxt_name);
        EditText editLocationText = findViewById(R.id.editTxt_location);

        Bundle profileInfo = getIntent().getExtras();
        String sub = profileInfo.getString("sub");

        editNameText.setText(profileInfo.getString("name"));
        if (profileInfo.getString("location") != null) {
            editLocationText.setText(profileInfo.getString("location"));
        }
        Button editProfileButton = findViewById(R.id.btn_save_profile);

        editProfileButton.setOnClickListener(v -> {

            UpdateUserInput updateUserInput = UpdateUserInput.builder()
                    .id(sub)
                    .name(editNameText.getText().toString())
                    .location(editLocationText.getText().toString())
                    .build();
            UpdateUserMutation updateUserMutation = UpdateUserMutation.builder()
                    .input(updateUserInput)
                    .build();
            ClientFactory.appSyncClient().mutate(updateUserMutation)
                    .refetchQueries(ListUsersQuery.builder().build())
                    .enqueue(mutateCallback);
            finish();
        });

    }

    private GraphQLCall.Callback<UpdateUserMutation.Data> mutateCallback = new GraphQLCall.Callback<UpdateUserMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateUserMutation.Data> response) {
            Log.i("VOD", "worked");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("VOD", e.toString());
        }
    };
}