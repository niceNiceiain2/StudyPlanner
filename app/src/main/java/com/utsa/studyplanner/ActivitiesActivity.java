package com.utsa.studyplanner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActivitiesActivity extends AppCompatActivity {

    private List<Task> taskList;
    private List<Task> originalTaskList;
    private TaskAdapter taskAdapter;

    private static final String PREF_NAME = "UserPrefs";
    private static final String TASKS_KEY = "task_list";

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        originalTaskList = loadTasks();
        taskList = new ArrayList<>(originalTaskList);

        taskAdapter = new TaskAdapter(taskList, position -> {
            Task taskToRemove = taskList.get(position);
            originalTaskList.remove(taskToRemove);
            taskList.remove(position);
            taskAdapter.notifyItemRemoved(position);
            saveTasks(originalTaskList);
        }, () -> saveTasks(originalTaskList));

        RecyclerView recyclerView = findViewById(R.id.activityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        FloatingActionButton addButton = findViewById(R.id.addTaskButton);
        addButton.setOnClickListener(v -> showAddTaskDialog());

        ImageButton filterAll = findViewById(R.id.filterAll);
        ImageButton filterAssignments = findViewById(R.id.filterAssignments);
        ImageButton filterExams = findViewById(R.id.filterExams);

        filterAll.setOnClickListener(v -> {
            List<Task> allTasks = new ArrayList<>();
            for (Task task : originalTaskList) {
                if (task.type.equalsIgnoreCase("Assignment") || task.type.equalsIgnoreCase("Exam")) {
                    allTasks.add(task);
                }
            }
            taskAdapter.updateList(allTasks);
        });

        filterAssignments.setOnClickListener(v -> {
            List<Task> filtered = new ArrayList<>();
            for (Task task : originalTaskList) {
                if (task.type.equalsIgnoreCase("Assignment")) {
                    filtered.add(task);
                }
            }
            taskAdapter.updateList(filtered);
        });

        filterExams.setOnClickListener(v -> {
            List<Task> filtered = new ArrayList<>();
            for (Task task : originalTaskList) {
                if (task.type.equalsIgnoreCase("Exam")) {
                    filtered.add(task);
                }
            }
            taskAdapter.updateList(filtered);
        });
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(view);

        RadioGroup typeGroup = view.findViewById(R.id.taskTypeGroup);
        EditText titleInput = view.findViewById(R.id.titleInput);
        EditText descriptionInput = view.findViewById(R.id.descriptionInput);
        Button dateTimeButton = view.findViewById(R.id.dateTimeButton);
        Button saveButton = view.findViewById(R.id.saveTaskButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        dateTimeButton.setOnClickListener(v -> showDateTimePicker(dateTimeButton));

        saveButton.setOnClickListener(v -> {
            int selectedId = typeGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a type", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = ((RadioButton) view.findViewById(selectedId)).getText().toString();
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String time = dateTimeButton.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (time.equals("Pick Date And Time") || time.trim().isEmpty()) {
                Toast.makeText(this, "Please select a date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            Task newTask = new Task(type, title, description, time);
            originalTaskList.add(newTask);
            taskList.add(newTask);
            taskAdapter.notifyItemInserted(taskList.size() - 1);
            saveTasks(originalTaskList);
            updateStats(type);

            dialog.dismiss();
        });
    }

    private void showDateTimePicker(Button targetButton) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean useMilitaryTime = prefs.getBoolean("military_time_enabled", false);

        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                // If in 12-hour format, convert hourOfDay to correct AM/PM using HOUR and AM_PM
                                if (!useMilitaryTime) {
                                    int hour = hourOfDay % 12;
                                    selectedDateTime.set(Calendar.HOUR, hour == 0 ? 12 : hour);
                                    selectedDateTime.set(Calendar.AM_PM, hourOfDay >= 12 ? Calendar.PM : Calendar.AM);
                                } else {
                                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                }
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                String pattern = useMilitaryTime ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd hh:mm a";
                                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                                targetButton.setText(sdf.format(selectedDateTime.getTime()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            useMilitaryTime);
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void updateStats(String type) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int totalTasks = prefs.getInt("total_tasks", 0) + 1;
        int totalAssignments = prefs.getInt("total_assignments", 0);
        int totalExams = prefs.getInt("total_exams", 0);

        if (type.equalsIgnoreCase("Assignment")) {
            totalAssignments++;
        } else if (type.equalsIgnoreCase("Exam")) {
            totalExams++;
        }

        editor.putInt("total_tasks", totalTasks);
        editor.putInt("total_assignments", totalAssignments);
        editor.putInt("total_exams", totalExams);
        editor.apply();
    }

    private void saveTasks(List<Task> tasks) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        editor.putString(TASKS_KEY, json);
        editor.apply();
    }

    private List<Task> loadTasks() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String json = prefs.getString(TASKS_KEY, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Task>>() {}.getType();
            return gson.fromJson(json, type);
        }

        return new ArrayList<>();
    }
}

