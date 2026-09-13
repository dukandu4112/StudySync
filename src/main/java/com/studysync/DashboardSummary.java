package com.studysync;

/**
 * Immutable summary of StudySync productivity metrics.
 */
public record DashboardSummary(
        int totalCourses,
        int totalAssignments,
        int pendingAssignments,
        int completedAssignments,
        int overdueAssignments,
        int totalStudyMinutes,
        double completionPercentage) {

    public DashboardSummary {
        if (totalCourses < 0
                || totalAssignments < 0
                || pendingAssignments < 0
                || completedAssignments < 0
                || overdueAssignments < 0
                || totalStudyMinutes < 0) {
            throw new IllegalArgumentException(
                    "Dashboard counts cannot be negative.");
        }

        if (completionPercentage < 0.0
                || completionPercentage > 100.0) {
            throw new IllegalArgumentException(
                    "Completion percentage must be between 0 and 100.");
        }
    }
}
