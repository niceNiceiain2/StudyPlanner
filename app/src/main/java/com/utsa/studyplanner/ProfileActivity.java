package com.utsa.studyplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_BIO = "user_bio";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_SETTINGS_VISIBLE = "settings_visible";

    private static final String KEY_TOTAL_TASKS = "total_tasks";
    private static final String KEY_TOTAL_ASSIGNMENTS = "total_assignments";
    private static final String KEY_TOTAL_EXAMS = "total_exams";
    private static final String KEY_COMPLETED_TASKS = "tasks_completed";
    private static final String KEY_STREAK_DAYS = "streak_days";
    private static final String KEY_MILITARY_TIME = "military_time_enabled"; // ⏰ New setting

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load bio
        EditText bioInput = findViewById(R.id.bioInput);
        String savedBio = prefs.getString(KEY_BIO, "");
        bioInput.setText(savedBio);

        bioInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                prefs.edit().putString(KEY_BIO, s.toString()).apply();
            }
        });

        // Dark Mode Toggle
        SwitchCompat darkSwitch = findViewById(R.id.darkModeSwitch);
        boolean darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        darkSwitch.setChecked(darkModeEnabled);

        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit()
                    .putBoolean(KEY_DARK_MODE, isChecked)
                    .putBoolean(KEY_SETTINGS_VISIBLE, true)
                    .apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            recreate();
        });

        // Military Time Toggle
        SwitchCompat militarySwitch = findViewById(R.id.militaryTimeSwitch);
        boolean militaryEnabled = prefs.getBoolean(KEY_MILITARY_TIME, false);
        militarySwitch.setChecked(militaryEnabled);

        militarySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_MILITARY_TIME, isChecked).apply();
        });

        // Toggle Settings Panel
        ImageButton settingsBtn = findViewById(R.id.settingsButton);
        LinearLayout settingsPanel = findViewById(R.id.settingsPanel);

        settingsBtn.setOnClickListener(v -> {
            boolean isVisible = settingsPanel.getVisibility() == View.VISIBLE;
            settingsPanel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            prefs.edit().putBoolean(KEY_SETTINGS_VISIBLE, !isVisible).apply();
        });

        // Restore panel state
        boolean wasPanelVisible = prefs.getBoolean(KEY_SETTINGS_VISIBLE, false);
        settingsPanel.setVisibility(wasPanelVisible ? View.VISIBLE : View.GONE);

        // Load stats
        int totalTasks = prefs.getInt(KEY_TOTAL_TASKS, 0);
        int totalAssignments = prefs.getInt(KEY_TOTAL_ASSIGNMENTS, 0);
        int totalExams = prefs.getInt(KEY_TOTAL_EXAMS, 0);
        int completedTasks = prefs.getInt(KEY_COMPLETED_TASKS, 0);
        int currentStreak = prefs.getInt(KEY_STREAK_DAYS, 0);

        // Set stats text
        ((TextView) findViewById(R.id.totalTasksCount)).setText("Tasks Created: " + totalTasks);
        ((TextView) findViewById(R.id.totalAssignmentsCount)).setText("Assignments Created: " + totalAssignments);
        ((TextView) findViewById(R.id.totalExamsCount)).setText("Exams Created: " + totalExams);
        ((TextView) findViewById(R.id.tasksCompletedText)).setText("Tasks Completed: " + completedTasks);
        ((TextView) findViewById(R.id.currentStreakCount)).setText("Current Day Streak: " + currentStreak + " days");

        // Reset Stats Button
        Button resetStatsBtn = findViewById(R.id.resetStatsButton);
        if (resetStatsBtn != null) {
            resetStatsBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Reset Stats")
                        .setMessage("Are you sure you want to wipe your stats data?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt(KEY_TOTAL_TASKS, 0);
                            editor.putInt(KEY_TOTAL_ASSIGNMENTS, 0);
                            editor.putInt(KEY_TOTAL_EXAMS, 0);
                            editor.putInt(KEY_COMPLETED_TASKS, 0);
                            editor.putInt(KEY_STREAK_DAYS, 0);
                            editor.apply();
                            recreate(); // Refresh UI
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
}

