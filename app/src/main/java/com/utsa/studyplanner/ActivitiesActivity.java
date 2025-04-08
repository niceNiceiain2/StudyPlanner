package com.utsa.studyplanner;

import android.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.List;

public class ActivitiesActivity extends AppCompatActivity {

    private List<Task> taskList;           // Adapter list (filtered)
    private List<Task> originalTaskList;   // Full, master list

    private TaskAdapter taskAdapter;

    private static final String PREF_NAME = "TaskPrefs";
    private static final String TASKS_KEY = "task_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        originalTaskList = loadTasks();
        taskList = new ArrayList<>(originalTaskList); // start with all tasks
        taskAdapter = new TaskAdapter(taskList, position -> {
            originalTaskList.remove(taskList.get(position)); // remove from master
            taskList.remove(position);                       // remove from current list
            taskAdapter.notifyItemRemoved(position);
            saveTasks(originalTaskList); // Save master list
        });


        RecyclerView recyclerView = findViewById(R.id.activityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        FloatingActionButton addButton = findViewById(R.id.addTaskButton);
        addButton.setOnClickListener(v -> showAddTaskDialog());

        // 🔍 NEW: Filter logic setup
        ImageButton filterAll = findViewById(R.id.filterAll);
        ImageButton filterAssignments = findViewById(R.id.filterAssignments);
        ImageButton filterExams = findViewById(R.id.filterExams);

        filterAll.setOnClickListener(v -> {
            List<Task> allTasks = new ArrayList<>();
            for (Task task : originalTaskList) {
                if (task.type.equalsIgnoreCase("Assignment")) {
                    allTasks.add(task);
                }
            }
            for (Task task : originalTaskList) {
                if (task.type.equalsIgnoreCase("Exam")) {
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
        EditText timeInput = view.findViewById(R.id.timeInput);
        Button saveButton = view.findViewById(R.id.saveTaskButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            int selectedId = typeGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a type", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = ((RadioButton) view.findViewById(selectedId)).getText().toString();
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String time = timeInput.getText().toString();

            if (title.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Title and Time are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Task newTask = new Task(type, title, description, time);

// Add to both master list and filtered list
            originalTaskList.add(newTask);
            taskList.add(newTask);

// Update the adapter and save the updated master list
            taskAdapter.notifyItemInserted(taskList.size() - 1);
            saveTasks(originalTaskList);

            dialog.dismiss();
        });
    }

    private void saveTasks(List<Task> tasks) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        editor.putString(TASKS_KEY, json);
        editor.apply();
    }

    private List<Task> loadTasks() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(TASKS_KEY, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Task>>(){}.getType();
            return gson.fromJson(json, type);
        }

        return new ArrayList<>();
    }
}


