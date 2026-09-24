package com.studysync;

import java.time.LocalDate;

/**
 * Snapshot of a student's study streak as of a reference date.
 *
 * @param referenceDate date used to calculate the streak
 * @param currentStreakDays consecutive study days ending on the reference date, or on the
 *                          most recent study day when the reference date has no activity
 * @param longestStreakDays longest consecutive study-day streak recorded
 * @param studiedToday whether at least one study session was recorded on the reference date
 * @param lastStudyDate most recent study date on or before the reference date, or null when
 *                      no study activity exists
 */
public record StudyStreak(
        LocalDate referenceDate,
        int currentStreakDays,
        int longestStreakDays,
        boolean studiedToday,
        LocalDate lastStudyDate) {

    public StudyStreak {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Study streak reference date cannot be null.");
        }
        if (currentStreakDays < 0 || longestStreakDays < 0) {
            throw new IllegalArgumentException("Study streak lengths cannot be negative.");
        }
        if (currentStreakDays > longestStreakDays) {
            throw new IllegalArgumentException("Current study streak cannot exceed the longest streak.");
        }
        if (lastStudyDate != null && lastStudyDate.isAfter(referenceDate)) {
            throw new IllegalArgumentException("Last study date cannot be after the reference date.");
        }
        if (studiedToday && !referenceDate.equals(lastStudyDate)) {
            throw new IllegalArgumentException("A studied-today streak must end on the reference date.");
        }
        if (lastStudyDate == null && (currentStreakDays != 0 || longestStreakDays != 0 || studiedToday)) {
            throw new IllegalArgumentException("A streak without study activity must contain zero metrics.");
        }
    }
}
