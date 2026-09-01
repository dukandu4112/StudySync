package com.studysync;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an academic assignment managed by StudySync.
 */
public class Assignment {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    private int id;
    private int courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Priority priority;
    private boolean completed;

    public Assignment(
            int courseId,
            String title,
            String description,
            LocalDateTime dueDate,
            Priority priority) {

        setCourseId(courseId);
        setTitle(title);
        setDescription(description);
        setDueDate(dueDate);
        setPriority(priority);
        this.completed = false;
    }

    public Assignment(
            int id,
            int courseId,
            String title,
            String description,
            LocalDateTime dueDate,
            Priority priority,
            boolean completed) {

        this(courseId, title, description, dueDate, priority);
        this.id = id;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCourseId(int courseId) {
        if (courseId <= 0) {
            throw new IllegalArgumentException(
                    "Course ID must be greater than zero.");
        }

        this.courseId = courseId;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Assignment title cannot be empty.");
        }

        this.title = title.trim();
    }

    public void setDescription(String description) {
        this.description =
                description == null ? "" : description.trim();
    }

    public void setDueDate(LocalDateTime dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "Assignment due date cannot be null.");
        }

        this.dueDate = dueDate;
    }

    public void setPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException(
                    "Assignment priority cannot be null.");
        }

        this.priority = priority;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void markIncomplete() {
        this.completed = false;
    }

    public boolean isOverdue() {
        return !completed && dueDate.isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        String status = completed ? "Completed" : "Pending";

        return title
                + " | Due: " + dueDate
                + " | Priority: " + priority
                + " | Status: " + status;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Assignment assignment)) {
            return false;
        }

        return id == assignment.id
                && courseId == assignment.courseId
                && completed == assignment.completed
                && Objects.equals(title, assignment.title)
                && Objects.equals(description, assignment.description)
                && Objects.equals(dueDate, assignment.dueDate)
                && priority == assignment.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                courseId,
                title,
                description,
                dueDate,
                priority,
                completed);
    }
}
