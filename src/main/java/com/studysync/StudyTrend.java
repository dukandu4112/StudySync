package com.studysync;

import java.time.LocalDate;

/**
 * Study-time comparison between the current week and the previous week.
 *
 * @param currentWeekStart Monday that begins the current comparison week
 * @param currentWeekMinutes study minutes recorded in the current week
 * @param previousWeekMinutes study minutes recorded in the previous week
 * @param minuteChange current-week minutes minus previous-week minutes
 * @param percentageChange percentage change from the previous week, or 0 when the previous week has no study time
 */
public record StudyTrend(
        LocalDate currentWeekStart,
        int currentWeekMinutes,
        int previousWeekMinutes,
        int minuteChange,
        double percentageChange) {

    public StudyTrend {
        if (currentWeekStart == null) {
            throw new IllegalArgumentException("Study trend week start cannot be null.");
        }
        if (currentWeekMinutes < 0 || previousWeekMinutes < 0) {
            throw new IllegalArgumentException("Study trend minutes cannot be negative.");
        }
        if (minuteChange != currentWeekMinutes - previousWeekMinutes) {
            throw new IllegalArgumentException("Study trend minute change must match the weekly totals.");
        }
        if (!Double.isFinite(percentageChange)) {
            throw new IllegalArgumentException("Study trend percentage change must be finite.");
        }
    }

    public Direction direction() {
        if (minuteChange > 0) return Direction.UP;
        if (minuteChange < 0) return Direction.DOWN;
        return Direction.SAME;
    }

    public enum Direction {
        UP,
        DOWN,
        SAME
    }
}
