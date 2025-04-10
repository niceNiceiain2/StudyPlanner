package com.utsa.studyplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout dueTodayContainer, dueLaterContainer;
    private static final String PREF_NAME = "UserPrefs";
    private static final String TASKS_KEY = "task_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String username = getIntent().getStringExtra("username");
        TextView welcomeText = findViewById(R.id.welcomeMessage);
        welcomeText.setText("Welcome, " + (username != null ? username : "User") + "!");

        dueTodayContainer = findViewById(R.id.dueTodayContainer);
        dueLaterContainer = findViewById(R.id.dueLaterContainer);

        ImageButton calendarButton = findViewById(R.id.calendarButton);
        ImageButton activitiesButton = findViewById(R.id.activitiesButton);
        ImageButton profileButton = findViewById(R.id.profileButton);

        calendarButton.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
        activitiesButton.setOnClickListener(v -> startActivity(new Intent(this, ActivitiesActivity.class)));
        profileButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayTasks();
    }

    private void loadAndDisplayTasks() {
        dueTodayContainer.removeAllViews();
        dueLaterContainer.removeAllViews();

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String json = prefs.getString(TASKS_KEY, null);
        if (json == null) return;

        Gson gson = new Gson();
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> taskList = gson.fromJson(json, type);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = dateFormat.format(Calendar.getInstance().getTime());

        for (Task task : taskList) {
            try {
                String taskDateStr = task.time.split(" ")[0];
                String formattedTime = TimeUtilities.formatTime(this, task.time);  // Format properly for AM/PM or military
                String display = "- " + task.title + " (" + task.type + ") on " + taskDateStr + " at " + formattedTime;

                TextView taskView = new TextView(this);
                taskView.setText(display);
                taskView.setTextColor(getResources().getColor(R.color.black)); // Always black for readability
                taskView.setTextSize(14);
                taskView.setPadding(8, 8, 8, 8);

                if (taskDateStr.equals(todayStr)) {
                    dueTodayContainer.addView(taskView);
                } else {
                    dueLaterContainer.addView(taskView);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

