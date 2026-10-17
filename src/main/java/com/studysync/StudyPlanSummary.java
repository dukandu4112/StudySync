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

    /** Minutes accounted for today by completed study plus the generated plan. */
    public int accountedMinutes() {
        return Math.min(dailyTargetMinutes, studiedTodayMinutes + plannedMinutes);
    }

    /** Percentage of today's study target accounted for by completed study and planned blocks. */
    public double coveragePercentage() {
        return accountedMinutes() * 100.0 / dailyTargetMinutes;
    }

    /** Minutes still needed from actual study, regardless of what is currently planned. */
    public int remainingStudyMinutes() {
        return Math.max(0, dailyTargetMinutes - studiedTodayMinutes);
    }

    /** Human-readable state used by the dashboard to explain today's workload. */
    public String workloadStatus() {
        if (studiedTodayMinutes >= dailyTargetMinutes) return "Goal complete";
        if (plannedMinutes == 0) return "Needs planning";
        if (coversRemainingDailyTarget()) return "Fully planned";
        return "Partially planned";
    }
}
