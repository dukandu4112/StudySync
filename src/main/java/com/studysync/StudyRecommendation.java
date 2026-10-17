package com.studysync;

/** A ranked recommendation describing which course should receive study attention next. */
public record StudyRecommendation(
        Course course,
        int score,
        int weeklyRemainingMinutes,
        int pendingAssignments,
        int highPriorityAssignments,
        boolean hasDueSoonAssignment,
        String reason) {

    public StudyRecommendation {
        if (course == null) throw new IllegalArgumentException("Course cannot be null.");
        if (score < 0) throw new IllegalArgumentException("Recommendation score cannot be negative.");
        if (weeklyRemainingMinutes < 0) throw new IllegalArgumentException("Remaining minutes cannot be negative.");
        if (pendingAssignments < 0 || highPriorityAssignments < 0) throw new IllegalArgumentException("Assignment counts cannot be negative.");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Recommendation reason cannot be empty.");
    }
}
