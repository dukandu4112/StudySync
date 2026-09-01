package com.studysync;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a study session associated with an academic course.
 */
public class StudySession {

    private int id;
    private int courseId;
    private LocalDateTime startTime;
    private int durationMinutes;
    private String notes;

    public StudySession(
            int courseId,
            LocalDateTime startTime,
            int durationMinutes,
            String notes) {

        setCourseId(courseId);
        setStartTime(startTime);
        setDurationMinutes(durationMinutes);
        setNotes(notes);
    }

    public StudySession(
            int id,
            int courseId,
            LocalDateTime startTime,
            int durationMinutes,
            String notes) {

        this(courseId, startTime, durationMinutes, notes);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getCourseId() {
        return courseId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setCourseId(int courseId) {
        if (courseId <= 0) {
            throw new IllegalArgumentException(
                    "Course ID must be greater than zero.");
        }

        this.courseId = courseId;
    }

    public void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException(
                    "Study session start time cannot be null.");
        }

        this.startTime = startTime;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Study session duration must be greater than zero.");
        }

        this.durationMinutes = durationMinutes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }

    public double getDurationHours() {
        return durationMinutes / 60.0;
    }

    @Override
    public String toString() {
        return "Study Session"
                + " | Start: " + startTime
                + " | Duration: " + durationMinutes + " minutes"
                + (notes.isBlank() ? "" : " | Notes: " + notes);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof StudySession session)) {
            return false;
        }

        return id == session.id
                && courseId == session.courseId
                && durationMinutes == session.durationMinutes
                && Objects.equals(startTime, session.startTime)
                && Objects.equals(notes, session.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                courseId,
                startTime,
                durationMinutes,
                notes);
    }
}
