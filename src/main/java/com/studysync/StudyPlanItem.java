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

    /** Short focus guidance so a suggested block communicates how to use the time. */
    public String focusGuidance() {
        if (suggestedMinutes <= 30) {
            return "Focused sprint — work on the highest-priority task for this course.";
        }
        if (suggestedMinutes <= 60) {
            return "Deep study block — focus on one major task and avoid switching topics.";
        }
        return "Extended study block — split the time into focused segments with a short break.";
    }
}
