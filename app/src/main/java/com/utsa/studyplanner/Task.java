package com.utsa.studyplanner;

public class Task {
    public String type;        // "Assignment" or "Exam"
    public String title;       // e.g., "CS Exam Review"
    public String description; // e.g., "Chapters 1-3"
    public String time;        // formatted string like "2025-04-10 14:00"
    public boolean completed;
    public boolean counted;

    public Task(String type, String title, String description, String time) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.time = time;
        this.completed = false;
        this.counted = false; // Initialize as not counted
    }

}

