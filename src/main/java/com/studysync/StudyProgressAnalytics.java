package com.studysync;

import java.time.LocalDate;

/**
 * Weekly study-activity metrics used by StudySync planning views.
 */
public record StudyProgressAnalytics(
        LocalDate weekStart,
        LocalDate weekEnd,
        int totalStudyMinutes,
        int sessionCount,
        int studyDays,
        int longestSessionMinutes,
        double averageMinutesPerStudyDay) {

    public StudyProgressAnalytics {
        if (weekStart == null || weekEnd == null) {
            throw new IllegalArgumentException("Study progress week dates cannot be null.");
        }
        if (weekEnd.isBefore(weekStart)) {
            throw new IllegalArgumentException("Study progress week end cannot be before week start.");
        }
        if (totalStudyMinutes < 0
                || sessionCount < 0
                || studyDays < 0
                || longestSessionMinutes < 0
                || averageMinutesPerStudyDay < 0.0) {
            throw new IllegalArgumentException("Study progress values cannot be negative.");
        }
        if (studyDays > 7) {
            throw new IllegalArgumentException("Study days cannot exceed seven in a weekly summary.");
        }
        if (sessionCount == 0
                && (totalStudyMinutes != 0
                        || studyDays != 0
                        || longestSessionMinutes != 0
                        || averageMinutesPerStudyDay != 0.0)) {
            throw new IllegalArgumentException("Empty study progress must contain zero-valued metrics.");
        }
    }
}
