package com.studysync;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Pure helper logic used by the JavaFX layer. Keeping parsing, filtering,
 * and sorting here makes UI behavior testable without launching JavaFX.
 */
public final class UiSupport {
    private UiSupport() { }

    public static LocalDateTime parseDateTime(LocalDate date, String timeText) {
        if (date == null) {
            throw new IllegalArgumentException("A date is required.");
        }
        if (timeText == null || timeText.isBlank()) {
            throw new IllegalArgumentException("A time is required in HH:mm format.");
        }
        try {
            return LocalDateTime.of(date, LocalTime.parse(timeText.trim()));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Time must use 24-hour HH:mm format, such as 14:30.");
        }
    }

    public static int parsePositiveMinutes(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Duration is required.");
        }
        try {
            int minutes = Integer.parseInt(value.trim());
            if (minutes <= 0) {
                throw new IllegalArgumentException("Duration must be greater than zero minutes.");
            }
            return minutes;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Duration must be a whole number of minutes.");
        }
    }

    public static List<Course> filterCourses(List<Course> courses, String query) {
        if (courses == null) {
            throw new IllegalArgumentException("Courses cannot be null.");
        }
        String normalizedQuery = normalize(query);
        return courses.stream()
                .filter(course -> normalizedQuery.isEmpty()
                        || course.getCode().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || course.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .sorted(Comparator.comparing(Course::getCode))
                .toList();
    }

    public static List<Assignment> filterAssignments(List<Assignment> assignments,
            String query, String status, String priority) {
        if (assignments == null) {
            throw new IllegalArgumentException("Assignments cannot be null.");
        }
        String normalizedQuery = normalize(query);
        String normalizedStatus = status == null ? "All" : status;
        String normalizedPriority = priority == null ? "All" : priority;

        return assignments.stream()
                .filter(a -> normalizedQuery.isEmpty()
                        || a.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || a.getDescription().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .filter(a -> switch (normalizedStatus) {
                    case "Pending" -> !a.isCompleted() && !a.isOverdue();
                    case "Completed" -> a.isCompleted();
                    case "Overdue" -> a.isOverdue();
                    default -> true;
                })
                .filter(a -> normalizedPriority.equals("All")
                        || a.getPriority().name().equals(normalizedPriority))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();
    }

    public static List<StudySession> filterStudySessions(List<StudySession> sessions,
            Integer courseId, String query) {
        if (sessions == null) {
            throw new IllegalArgumentException("Study sessions cannot be null.");
        }
        String normalizedQuery = normalize(query);
        return sessions.stream()
                .filter(session -> courseId == null || session.getCourseId() == courseId)
                .filter(session -> normalizedQuery.isEmpty()
                        || session.getNotes().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .sorted(Comparator.comparing(StudySession::getStartTime).reversed())
                .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
