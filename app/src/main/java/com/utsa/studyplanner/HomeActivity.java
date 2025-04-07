package com.utsa.studyplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Welcome message setup
        String username = getIntent().getStringExtra("username");
        TextView welcomeText = findViewById(R.id.welcomeMessage);
        welcomeText.setText("Hi, " + username + "!");

        // Navigation buttons
        ImageButton calendarButton = findViewById(R.id.calendarButton);
        ImageButton activitiesButton = findViewById(R.id.activitiesButton);
        ImageButton profileButton = findViewById(R.id.profileButton);

        // Go to CalendarActivity
        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        // Go to ActivitiesActivity
        activitiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ActivitiesActivity.class);
            startActivity(intent);
        });

        // Go to ProfileActivity
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
