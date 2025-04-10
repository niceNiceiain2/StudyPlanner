package com.utsa.studyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final OnDeleteClickListener deleteClickListener;
    private final Runnable saveTasksCallback;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public TaskAdapter(List<Task> taskList, OnDeleteClickListener deleteClickListener, Runnable saveTasksCallback) {
        this.taskList = taskList;
        this.deleteClickListener = deleteClickListener;
        this.saveTasksCallback = saveTasksCallback;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.title.setText(task.title);
        holder.description.setText(task.description);
        holder.time.setText(TimeUtilities.formatTime(holder.itemView.getContext(), task.time));
        holder.type.setText(task.type);

        holder.completedCheckBox.setOnCheckedChangeListener(null);
        holder.completedCheckBox.setChecked(task.completed);

        holder.completedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !task.counted) {
                SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                int completedCount = prefs.getInt("tasks_completed", 0);
                prefs.edit().putInt("tasks_completed", completedCount + 1).apply();
                task.counted = true; // Prevent future re-counting
            }

            task.completed = isChecked;
            saveTasksCallback.run();
        });

        holder.deleteButton.setOnClickListener(v -> deleteClickListener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateList(List<Task> newList) {
        taskList.clear();
        taskList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, time, type;
        ImageButton deleteButton;
        CheckBox completedCheckBox;

        public TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            description = itemView.findViewById(R.id.taskDescription);
            time = itemView.findViewById(R.id.taskTime);
            type = itemView.findViewById(R.id.taskType);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            completedCheckBox = itemView.findViewById(R.id.taskCompletedCheckbox);
        }
    }
}
