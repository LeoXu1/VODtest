package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateReviewMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateReviewInput;

public class WriteReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        String id = getIntent().getStringExtra("id");
        EditText content = findViewById(R.id.editTxt_content);
        Spinner spinner = (Spinner) findViewById(R.id.numStars);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.stars, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Button submitReview = findViewById(R.id.btn_submitReview);
        submitReview.setTransformationMethod(null);
        submitReview.setOnClickListener(v -> {
            int starNum = Integer.parseInt(spinner.getSelectedItem().toString());
            String contentInput = content.getText().toString();
            CreateReviewInput createReviewInput = CreateReviewInput.builder()
                    .videoID(id)
                    .content(contentInput)
                    .stars(starNum)
                    .build();

            CreateReviewMutation createReviewMutation = CreateReviewMutation.builder()
                    .input(createReviewInput)
                    .build();
            ClientFactory.appSyncClient().mutate(createReviewMutation)
                    .enqueue(mutateCallback);
        });
    }

    private GraphQLCall.Callback<CreateReviewMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateReviewMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateReviewMutation.Data> response) {
            runOnUiThread(() -> {
                Toast.makeText(WriteReviewActivity.this, "Added review", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(() -> Log.e("", "Failed to perform mutation", e));
        }
    };
}