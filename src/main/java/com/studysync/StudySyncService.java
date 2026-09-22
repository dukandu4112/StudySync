package com.studysync;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Application service that coordinates StudySync's core productivity features. */
public class StudySyncService {
    private final DatabaseManager databaseManager;

    public StudySyncService(DatabaseManager databaseManager) {
        if (databaseManager == null) {
            throw new IllegalArgumentException("Database manager cannot be null.");
        }
        this.databaseManager = databaseManager;
    }

    public Course createCourse(String name, String code) {
        return databaseManager.addCourse(name, code);
    }

    public List<Course> getCourses() {
        return databaseManager.getAllCourses();
    }

    public boolean updateCourse(int courseId, String name, String code) {
        requireCourse(courseId);
        Course course = new Course(courseId, name, code);
        return databaseManager.updateCourse(courseId, course.getName(), course.getCode());
    }

    public boolean deleteCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.deleteCourse(courseId);
    }

    public Assignment createAssignment(int courseId, String title, String description, LocalDateTime dueDate, Assignment.Priority priority) {
        requireCourse(courseId);
        return databaseManager.addAssignment(courseId, title, description, dueDate, priority);
    }

    public List<Assignment> getAssignments() { return databaseManager.getAllAssignments(); }

    public List<Assignment> getAssignmentsForCourse(int courseId) {
        requireCourse(courseId);
        return databaseManager.getAssignmentsByCourse(courseId);
    }

    public List<Assignment> getPendingAssignments() {
        return databaseManager.getAllAssignments().stream().filter(a -> !a.isCompleted())
                .sorted(Comparator.comparing(Assignment::getDueDate)).toList();
    }

    public List<Assignment> getCompletedAssignments() {
        return databaseManager.getAllAssignments().stream().filter(Assignment::isCompleted)
                .sorted(Comparator.comparing(Assignment::getDueDate)).toList();
    }

    public List<Assignment> getOverdueAssignments() {
        return databaseManager.getAllAssignments().stream().filter(Assignment::isOverdue)
                .sorted(Comparator.comparing(Assignment::getDueDate)).toList();
    }

    public List<Assignment> getUpcomingAssignments(int days) {
        if (days <= 0) throw new IllegalArgumentException("Upcoming assignment window must be greater than zero days.");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);
        return databaseManager.getAllAssignments().stream()
                .filter(a -> !a.isCompleted())
                .filter(a -> !a.getDueDate().isBefore(now))
                .filter(a -> !a.getDueDate().isAfter(deadline))
                .sorted(Comparator.comparing(Assignment::getDueDate)).toList();
    }

    public List<Assignment> getAssignmentsByPriority(Assignment.Priority priority) {
        if (priority == null) throw new IllegalArgumentException("Assignment priority cannot be null.");
        return databaseManager.getAllAssignments().stream().filter(a -> a.getPriority() == priority)
                .sorted(Comparator.comparing(Assignment::getDueDate)).toList();
    }

    public List<Assignment> searchAssignments(String query) {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("Search query cannot be empty.");
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return databaseManager.getAllAssignments().stream()
                .filter(a -> a.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || a.getDescription().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .sorted(Comparator.comparing(Assignment::getDueDate)).toList();
    }

    public List<AssignmentPlanItem> getAssignmentPlan() {
        return getAssignmentPlan(LocalDateTime.now());
    }

    public List<AssignmentPlanItem> getAssignmentPlan(LocalDateTime referenceTime) {
        if (referenceTime == null) throw new IllegalArgumentException("Assignment plan reference time cannot be null.");
        return databaseManager.getAllAssignments().stream()
                .filter(a -> !a.isCompleted())
                .map(a -> new AssignmentPlanItem(a, classifyUrgency(a, referenceTime), Duration.between(referenceTime, a.getDueDate()).toMinutes()))
                .sorted(Comparator.comparingInt((AssignmentPlanItem item) -> urgencyRank(item.urgency()))
                        .thenComparingInt(item -> priorityRank(item.assignment().getPriority()))
                        .thenComparing(item -> item.assignment().getDueDate())
                        .thenComparingInt(item -> item.assignment().getId()))
                .toList();
    }

    private AssignmentPlanItem.Urgency classifyUrgency(Assignment assignment, LocalDateTime referenceTime) {
        if (assignment.getDueDate().isBefore(referenceTime)) return AssignmentPlanItem.Urgency.OVERDUE;
        if (assignment.getDueDate().toLocalDate().equals(referenceTime.toLocalDate())) return AssignmentPlanItem.Urgency.DUE_TODAY;
        if (!assignment.getDueDate().isAfter(referenceTime.plusHours(48))) return AssignmentPlanItem.Urgency.DUE_SOON;
        return AssignmentPlanItem.Urgency.UPCOMING;
    }

    private int urgencyRank(AssignmentPlanItem.Urgency urgency) {
        return switch (urgency) {
            case OVERDUE -> 0;
            case DUE_TODAY -> 1;
            case DUE_SOON -> 2;
            case UPCOMING -> 3;
        };
    }

    private int priorityRank(Assignment.Priority priority) {
        return switch (priority) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    public boolean updateAssignment(int assignmentId, int courseId, String title, String description, LocalDateTime dueDate, Assignment.Priority priority) {
        requireAssignment(assignmentId);
        requireCourse(courseId);
        Assignment assignment = new Assignment(courseId, title, description, dueDate, priority);
        return databaseManager.updateAssignment(assignmentId, assignment.getCourseId(), assignment.getTitle(), assignment.getDescription(), assignment.getDueDate(), assignment.getPriority());
    }

    public boolean deleteAssignment(int assignmentId) { requireAssignment(assignmentId); return databaseManager.deleteAssignment(assignmentId); }
    public boolean completeAssignment(int assignmentId) { requireAssignment(assignmentId); return databaseManager.setAssignmentCompleted(assignmentId, true); }
    public boolean reopenAssignment(int assignmentId) { requireAssignment(assignmentId); return databaseManager.setAssignmentCompleted(assignmentId, false); }

    public StudySession recordStudySession(int courseId, LocalDateTime startTime, int durationMinutes, String notes) {
        requireCourse(courseId);
        return databaseManager.addStudySession(courseId, startTime, durationMinutes, notes);
    }
    public List<StudySession> getStudySessions() { return databaseManager.getAllStudySessions(); }
    public List<StudySession> getStudySessionsForCourse(int courseId) { requireCourse(courseId); return databaseManager.getStudySessionsByCourse(courseId); }

    public boolean updateStudySession(int sessionId, int courseId, LocalDateTime startTime, int durationMinutes, String notes) {
        requireStudySession(sessionId); requireCourse(courseId);
        StudySession session = new StudySession(courseId, startTime, durationMinutes, notes);
        return databaseManager.updateStudySession(sessionId, session.getCourseId(), session.getStartTime(), session.getDurationMinutes(), session.getNotes());
    }
    public boolean deleteStudySession(int sessionId) { requireStudySession(sessionId); return databaseManager.deleteStudySession(sessionId); }
    public int getTotalStudyMinutes() { return databaseManager.getAllStudySessions().stream().mapToInt(StudySession::getDurationMinutes).sum(); }
    public int getTotalStudyMinutesForCourse(int courseId) { requireCourse(courseId); return databaseManager.getStudySessionsByCourse(courseId).stream().mapToInt(StudySession::getDurationMinutes).sum(); }

    public StudyProgressAnalytics getStudyProgressAnalytics() { return getStudyProgressAnalytics(LocalDate.now()); }
    public StudyProgressAnalytics getStudyProgressAnalytics(LocalDate referenceDate) {
        if (referenceDate == null) throw new IllegalArgumentException("Study progress reference date cannot be null.");
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime startInclusive = weekStart.atStartOfDay();
        LocalDateTime endExclusive = weekStart.plusDays(7).atStartOfDay();
        List<StudySession> weeklySessions = databaseManager.getAllStudySessions().stream()
                .filter(s -> !s.getStartTime().isBefore(startInclusive)).filter(s -> s.getStartTime().isBefore(endExclusive)).toList();
        int totalMinutes = weeklySessions.stream().mapToInt(StudySession::getDurationMinutes).sum();
        int longestSession = weeklySessions.stream().mapToInt(StudySession::getDurationMinutes).max().orElse(0);
        Set<LocalDate> activeDays = weeklySessions.stream().map(s -> s.getStartTime().toLocalDate()).collect(Collectors.toSet());
        double average = activeDays.isEmpty() ? 0.0 : totalMinutes / (double) activeDays.size();
        return new StudyProgressAnalytics(weekStart, weekEnd, totalMinutes, weeklySessions.size(), activeDays.size(), longestSession, average);
    }

    public StudyStreak getStudyStreak() { return getStudyStreak(LocalDate.now()); }

    public StudyStreak getStudyStreak(LocalDate referenceDate) {
        if (referenceDate == null) throw new IllegalArgumentException("Study streak reference date cannot be null.");

        List<LocalDate> studyDays = databaseManager.getAllStudySessions().stream()
                .map(session -> session.getStartTime().toLocalDate())
                .filter(date -> !date.isAfter(referenceDate))
                .distinct()
                .sorted()
                .toList();

        if (studyDays.isEmpty()) {
            return new StudyStreak(referenceDate, 0, 0, false, null);
        }

        int longestStreak = 1;
        int runningStreak = 1;
        for (int i = 1; i < studyDays.size(); i++) {
            if (studyDays.get(i).equals(studyDays.get(i - 1).plusDays(1))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }
            longestStreak = Math.max(longestStreak, runningStreak);
        }

        LocalDate lastStudyDate = studyDays.get(studyDays.size() - 1);
        int currentStreak = 1;
        for (int i = studyDays.size() - 1; i > 0; i--) {
            if (studyDays.get(i - 1).equals(studyDays.get(i).minusDays(1))) {
                currentStreak++;
            } else {
                break;
            }
        }

        return new StudyStreak(referenceDate, currentStreak, longestStreak,
                lastStudyDate.equals(referenceDate), lastStudyDate);
    }

    public double getAssignmentCompletionPercentage() {
        List<Assignment> assignments = databaseManager.getAllAssignments();
        if (assignments.isEmpty()) return 0.0;
        long completed = assignments.stream().filter(Assignment::isCompleted).count();
        return completed * 100.0 / assignments.size();
    }

    public DashboardSummary getDashboardSummary() {
        List<Course> courses = databaseManager.getAllCourses();
        List<Assignment> assignments = databaseManager.getAllAssignments();
        int completed = (int) assignments.stream().filter(Assignment::isCompleted).count();
        int pending = assignments.size() - completed;
        int overdue = (int) assignments.stream().filter(Assignment::isOverdue).count();
        return new DashboardSummary(courses.size(), assignments.size(), pending, completed, overdue, getTotalStudyMinutes(), getAssignmentCompletionPercentage());
    }

    public DashboardAnalytics getDashboardAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> pending = databaseManager.getAllAssignments().stream().filter(a -> !a.isCompleted()).toList();
        int upcoming = (int) pending.stream().filter(a -> !a.getDueDate().isBefore(now)).filter(a -> !a.getDueDate().isAfter(now.plusDays(7))).count();
        int highPriority = (int) pending.stream().filter(a -> a.getPriority() == Assignment.Priority.HIGH).count();
        Assignment nearest = pending.stream().filter(a -> !a.getDueDate().isBefore(now)).min(Comparator.comparing(Assignment::getDueDate)).orElse(null);
        Course mostStudied = null;
        int mostMinutes = 0;
        for (Course course : databaseManager.getAllCourses()) {
            int minutes = databaseManager.getStudySessionsByCourse(course.getId()).stream().mapToInt(StudySession::getDurationMinutes).sum();
            if (minutes > mostMinutes) { mostMinutes = minutes; mostStudied = course; }
        }
        return new DashboardAnalytics(upcoming, highPriority, nearest, mostStudied, mostMinutes);
    }

    private Course requireCourse(int id) {
        Course course = databaseManager.findCourseById(id);
        if (course == null) throw new IllegalArgumentException("Course does not exist: " + id);
        return course;
    }
    private Assignment requireAssignment(int id) {
        Assignment assignment = databaseManager.findAssignmentById(id);
        if (assignment == null) throw new IllegalArgumentException("Assignment does not exist: " + id);
        return assignment;
    }
    private StudySession requireStudySession(int id) {
        StudySession session = databaseManager.findStudySessionById(id);
        if (session == null) throw new IllegalArgumentException("Study session does not exist: " + id);
        return session;
    }
}
