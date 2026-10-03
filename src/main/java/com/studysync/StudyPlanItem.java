package com.studysync;

/** One actionable block in StudySync's suggested study plan. */
public record StudyPlanItem(
        Course course,
        int suggestedMinutes,
        String reason,
        int recommendationScore) {

    public StudyPlanItem {
        if (course == null) throw new IllegalArgumentException("Course cannot be null.");
        if (suggestedMinutes <= 0) throw new IllegalArgumentException("Suggested minutes must be greater than zero.");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Study plan reason cannot be empty.");
        if (recommendationScore < 0) throw new IllegalArgumentException("Recommendation score cannot be negative.");
    }

    /** Simple user-facing urgency label derived from the recommendation score. */
    public String priorityLabel() {
        if (recommendationScore >= 80) return "High priority";
        if (recommendationScore >= 40) return "Medium priority";
        return "Low priority";
    }
}
