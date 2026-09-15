package com.studysync;

/**
 * Ranked assignment-planning item used to surface the work that needs attention next.
 */
public record AssignmentPlanItem(
        Assignment assignment,
        Urgency urgency,
        long minutesUntilDue) {

    public enum Urgency {
        OVERDUE,
        DUE_TODAY,
        DUE_SOON,
        UPCOMING
    }

    public AssignmentPlanItem {
        if (assignment == null) {
            throw new IllegalArgumentException("Planned assignment cannot be null.");
        }
        if (assignment.isCompleted()) {
            throw new IllegalArgumentException("Completed assignments cannot appear in the active plan.");
        }
        if (urgency == null) {
            throw new IllegalArgumentException("Assignment urgency cannot be null.");
        }
    }
}
