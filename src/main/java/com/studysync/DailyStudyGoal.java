package com.studysync;

import java.time.LocalDate;

/**
 * A daily study-time goal and the progress made toward it.
 */
public record DailyStudyGoal(
        LocalDate date,
        int targetMinutes,
        int studiedMinutes) {

    public DailyStudyGoal {
        if (date == null) {
            throw new IllegalArgumentException("Study goal date cannot be null.");
        }
        if (targetMinutes <= 0) {
            throw new IllegalArgumentException("Study goal target must be greater than zero.");
        }
        if (studiedMinutes < 0) {
            throw new IllegalArgumentException("Studied minutes cannot be negative.");
        }
    }

    public int remainingMinutes() {
        return Math.max(0, targetMinutes - studiedMinutes);
    }

    public double completionPercentage() {
        return Math.min(100.0, studiedMinutes * 100.0 / targetMinutes);
    }

    public boolean completed() {
        return studiedMinutes >= targetMinutes;
    }
}
