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

    /** Practical recovery guidance to keep longer study plans sustainable. */
    public String breakGuidance() {
        if (suggestedMinutes <= 30) return "Break: optional 5-minute reset after the block.";
        if (suggestedMinutes <= 60) return "Break: take 5–10 minutes after the block.";
        return "Break: take 5–10 minutes near the midpoint, then reset before continuing.";
    }

    /** Quick effort cue combining the block duration with its recommendation urgency. */
    public String effortLabel() {
        if (suggestedMinutes > 60 || recommendationScore >= 80) return "Heavy focus";
        if (suggestedMinutes > 30 || recommendationScore >= 40) return "Moderate focus";
        return "Light focus";
    }

    /** Compact block description used by planner summaries and accessibility-friendly UI. */
    public String planLabel() {
        return course.getCode() + " — " + suggestedMinutes + " min — " + priorityLabel() + " — " + effortLabel();
    }

    /** Short sequencing cue for scanning the suggested plan in execution order. */
    public String sequenceCue(int blockNumber, int totalBlocks) {
        if (blockNumber <= 0 || totalBlocks <= 0 || blockNumber > totalBlocks) {
            throw new IllegalArgumentException("Study plan sequence values are invalid.");
        }
        if (totalBlocks == 1) return "Only block — complete this study block to finish the plan.";
        if (blockNumber == 1) return "First block — begin here.";
        if (blockNumber == totalBlocks) return "Final block — finish here.";
        return "Block " + blockNumber + " of " + totalBlocks + " — continue in plan order.";
    }

    /** Accessible summary that includes the block's position in the plan. */
    public String accessiblePlanLabel(int blockNumber, int totalBlocks) {
        return planLabel() + ". " + sequenceCue(blockNumber, totalBlocks);
    }

    /** Immediate action cue that turns the recommendation into a clear next step. */
    public String actionCue() {
        return switch (priorityLabel()) {
            case "High priority" -> "Start next — protect this block from interruptions.";
            case "Medium priority" -> "Schedule today — complete it after higher-priority work.";
            default -> "Fit in when available — use it to maintain course momentum.";
        };
    }
}
