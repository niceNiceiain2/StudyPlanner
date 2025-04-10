package com.utsa.studyplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {

    private TextView selectedDateText;
    private CalendarView calendarView;
    private ListView taskListView;

    private List<Task> allTasks = new ArrayList<>();
    private static final String PREF_NAME = "UserPrefs";
    private static final String TASKS_KEY = "task_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        selectedDateText = findViewById(R.id.selectedDateText);
        calendarView = findViewById(R.id.calendarView);
        taskListView = findViewById(R.id.taskListView);

        loadTasks();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = sdf.format(selectedDate.getTime());
            selectedDateText.setText("Tasks on " + dateStr);

            displayTasksForDate(dateStr);
        });
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String json = prefs.getString(TASKS_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Task>>() {}.getType();
            allTasks = gson.fromJson(json, type);
        }
    }

    private void displayTasksForDate(String dateStr) {
        List<String> tasksOnDate = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (Task task : allTasks) {
            try {
                Date taskDate = sdf.parse(task.time);
                if (taskDate != null) {
                    String taskDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(taskDate);
                    if (taskDateOnly.equals(dateStr)) {
                        // 👇 Format only the time part using user preference
                        String formattedTime = TimeUtilities.formatTime(this, task.time);
                        tasksOnDate.add(task.title + " (" + task.type + ") at " + formattedTime);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, tasksOnDate);
        taskListView.setAdapter(adapter);
    }
}
