package com.studysync;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    public Assignment createAssignment(int courseId, String title,
            String description, LocalDateTime dueDate,
            Assignment.Priority priority) {
        requireCourse(courseId);
        return databaseManager.addAssignment(
                courseId, title, description, dueDate, priority);
    }

    public List<Assignment> getAssignments() {
        return databaseManager.getAllAssignments();
    }

    public List<Assignment> getAssignmentsForCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.getAssignmentsByCourse(courseId);
    }

    public List<Assignment> getPendingAssignments() {
        return databaseManager.getAllAssignments().stream()
                .filter(assignment -> !assignment.isCompleted())
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public List<Assignment> getCompletedAssignments() {
        return databaseManager.getAllAssignments().stream()
                .filter(Assignment::isCompleted)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public List<Assignment> getOverdueAssignments() {
        return databaseManager.getAllAssignments().stream()
                .filter(Assignment::isOverdue)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    /**
     * Returns pending assignments due from now through the requested number
     * of days, ordered by due date.
     */
    public List<Assignment> getUpcomingAssignments(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException(
                    "Upcoming assignment window must be greater than zero days.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);

        return databaseManager.getAllAssignments().stream()
                .filter(assignment -> !assignment.isCompleted())
                .filter(assignment -> !assignment.getDueDate().isBefore(now))
                .filter(assignment -> !assignment.getDueDate().isAfter(deadline))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public List<Assignment> getAssignmentsByPriority(
            Assignment.Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException(
                    "Assignment priority cannot be null.");
        }
        return databaseManager.getAllAssignments().stream()
                .filter(assignment -> assignment.getPriority() == priority)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public List<Assignment> searchAssignments(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be empty.");
        }
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return databaseManager.getAllAssignments().stream()
                .filter(assignment -> assignment.getTitle()
                        .toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || assignment.getDescription()
                        .toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public boolean updateAssignment(int assignmentId, int courseId,
            String title, String description, LocalDateTime dueDate,
            Assignment.Priority priority) {
        requireAssignment(assignmentId);
        requireCourse(courseId);
        Assignment validatedAssignment = new Assignment(
                courseId, title, description, dueDate, priority);
        return databaseManager.updateAssignment(
                assignmentId,
                validatedAssignment.getCourseId(),
                validatedAssignment.getTitle(),
                validatedAssignment.getDescription(),
                validatedAssignment.getDueDate(),
                validatedAssignment.getPriority());
    }

    public boolean deleteAssignment(int assignmentId) {
        requireAssignment(assignmentId);
        return databaseManager.deleteAssignment(assignmentId);
    }

    public boolean completeAssignment(int assignmentId) {
        requireAssignment(assignmentId);
        return databaseManager.setAssignmentCompleted(assignmentId, true);
    }

    public boolean reopenAssignment(int assignmentId) {
        requireAssignment(assignmentId);
        return databaseManager.setAssignmentCompleted(assignmentId, false);
    }

    public StudySession recordStudySession(int courseId,
            LocalDateTime startTime, int durationMinutes, String notes) {
        requireCourse(courseId);
        return databaseManager.addStudySession(
                courseId, startTime, durationMinutes, notes);
    }

    public List<StudySession> getStudySessions() {
        return databaseManager.getAllStudySessions();
    }

    public List<StudySession> getStudySessionsForCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.getStudySessionsByCourse(courseId);
    }

    public int getTotalStudyMinutes() {
        return databaseManager.getAllStudySessions().stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    public int getTotalStudyMinutesForCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.getStudySessionsByCourse(courseId).stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    public double getAssignmentCompletionPercentage() {
        List<Assignment> assignments = databaseManager.getAllAssignments();
        if (assignments.isEmpty()) {
            return 0.0;
        }
        long completed = assignments.stream()
                .filter(Assignment::isCompleted)
                .count();
        return completed * 100.0 / assignments.size();
    }

    public DashboardSummary getDashboardSummary() {
        List<Course> courses = databaseManager.getAllCourses();
        List<Assignment> assignments = databaseManager.getAllAssignments();
        int completedAssignments = (int) assignments.stream()
                .filter(Assignment::isCompleted).count();
        int pendingAssignments = assignments.size() - completedAssignments;
        int overdueAssignments = (int) assignments.stream()
                .filter(Assignment::isOverdue).count();
        return new DashboardSummary(
                courses.size(), assignments.size(), pendingAssignments,
                completedAssignments, overdueAssignments,
                getTotalStudyMinutes(), getAssignmentCompletionPercentage());
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
        Assignment assignment = databaseManager.findAssignmentById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException(
                    "Assignment does not exist: " + assignmentId);
        }
        return assignment;
    }
}
