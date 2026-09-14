package com.studysync;

/**
 * Higher-level planning insights for the StudySync dashboard.
 */
public record DashboardAnalytics(
        int upcomingAssignments,
        int highPriorityPendingAssignments,
        Assignment nearestDeadline,
        Course mostStudiedCourse,
        int mostStudiedCourseMinutes) {

    public DashboardAnalytics {
        if (upcomingAssignments < 0
                || highPriorityPendingAssignments < 0
                || mostStudiedCourseMinutes < 0) {
            throw new IllegalArgumentException("Dashboard analytics values cannot be negative.");
        }
        if (mostStudiedCourse == null && mostStudiedCourseMinutes != 0) {
            throw new IllegalArgumentException("Study minutes require a most-studied course.");
        }
    }
}
