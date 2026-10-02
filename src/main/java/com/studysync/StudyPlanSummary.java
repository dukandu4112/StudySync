package com.studysync;

/** Summary of today's generated study plan and remaining daily target. */
public record StudyPlanSummary(
        int plannedMinutes,
        int blockCount,
        int dailyTargetMinutes,
        int studiedTodayMinutes,
        int remainingAfterPlanMinutes) {

    public StudyPlanSummary {
        if (plannedMinutes < 0 || blockCount < 0 || dailyTargetMinutes <= 0
                || studiedTodayMinutes < 0 || remainingAfterPlanMinutes < 0) {
            throw new IllegalArgumentException("Study plan summary values are invalid.");
        }
    }

    public boolean coversRemainingDailyTarget() {
        return remainingAfterPlanMinutes == 0;
    }
}
