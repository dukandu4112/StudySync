package com.studysync;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Application service that coordinates StudySync's core productivity features.
 */
public class StudySyncService {

    private final DatabaseManager databaseManager;

    public StudySyncService(DatabaseManager databaseManager) {
        if (databaseManager == null) {
            throw new IllegalArgumentException(
                    "Database manager cannot be null.");
        }

        this.databaseManager = databaseManager;
    }

    public Course createCourse(String name, String code) {
        return databaseManager.addCourse(name, code);
    }

    public List<Course> getCourses() {
        return databaseManager.getAllCourses();
    }

    public Assignment createAssignment(
            int courseId,
            String title,
            String description,
            LocalDateTime dueDate,
            Assignment.Priority priority) {

        requireCourse(courseId);

        return databaseManager.addAssignment(
                courseId,
                title,
                description,
                dueDate,
                priority);
    }

    public List<Assignment> getAssignments() {
        return databaseManager.getAllAssignments();
    }

    public List<Assignment> getAssignmentsForCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.getAssignmentsByCourse(courseId);
    }

    public List<Assignment> getPendingAssignments() {
        return databaseManager.getAllAssignments()
                .stream()
                .filter(assignment -> !assignment.isCompleted())
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public List<Assignment> getOverdueAssignments() {
        return databaseManager.getAllAssignments()
                .stream()
                .filter(Assignment::isOverdue)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public boolean completeAssignment(int assignmentId) {
        requireAssignment(assignmentId);
        return databaseManager.setAssignmentCompleted(
                assignmentId,
                true);
    }

    public boolean reopenAssignment(int assignmentId) {
        requireAssignment(assignmentId);
        return databaseManager.setAssignmentCompleted(
                assignmentId,
                false);
    }

    public StudySession recordStudySession(
            int courseId,
            LocalDateTime startTime,
            int durationMinutes,
            String notes) {

        requireCourse(courseId);

        return databaseManager.addStudySession(
                courseId,
                startTime,
                durationMinutes,
                notes);
    }

    public List<StudySession> getStudySessions() {
        return databaseManager.getAllStudySessions();
    }

    public List<StudySession> getStudySessionsForCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.getStudySessionsByCourse(courseId);
    }

    public int getTotalStudyMinutes() {
        return databaseManager.getAllStudySessions()
                .stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    public int getTotalStudyMinutesForCourse(int courseId) {
        requireCourse(courseId);

        return databaseManager.getStudySessionsByCourse(courseId)
                .stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    public double getAssignmentCompletionPercentage() {
        List<Assignment> assignments =
                databaseManager.getAllAssignments();

        if (assignments.isEmpty()) {
            return 0.0;
        }

        long completed = assignments.stream()
                .filter(Assignment::isCompleted)
                .count();

        return completed * 100.0 / assignments.size();
    }

    private Course requireCourse(int courseId) {
        Course course = databaseManager.findCourseById(courseId);

        if (course == null) {
            throw new IllegalArgumentException(
                    "Course does not exist: " + courseId);
        }

        return course;
    }

    private Assignment requireAssignment(int assignmentId) {
        Assignment assignment =
                databaseManager.findAssignmentById(assignmentId);

        if (assignment == null) {
            throw new IllegalArgumentException(
                    "Assignment does not exist: " + assignmentId);
        }

        return assignment;
    }
}
