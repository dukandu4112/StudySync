package com.studysync;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** JavaFX presentation component for the ranked assignment plan. */
public final class AssignmentPlanView {
    private static final DateTimeFormatter DUE_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    private static final int DASHBOARD_LIMIT = 5;

    private AssignmentPlanView() {}

    public static VBox create(List<AssignmentPlanItem> plan) {
        if (plan == null) throw new IllegalArgumentException("Assignment plan cannot be null.");
        VBox content = new VBox(10);
        content.setPadding(new Insets(2, 0, 2, 0));
        if (plan.isEmpty()) {
            Label empty = new Label("You're caught up — no pending assignments need attention.");
            empty.getStyleClass().add("assignment-meta");
            content.getChildren().add(empty);
            return content;
        }
        int visible = Math.min(plan.size(), DASHBOARD_LIMIT);
        for (int i = 0; i < visible; i++) content.getChildren().add(planRow(plan.get(i), i + 1));
        if (plan.size() > DASHBOARD_LIMIT) {
            Label remaining = new Label("+ " + (plan.size() - DASHBOARD_LIMIT) + " more pending assignment(s)");
            remaining.getStyleClass().add("assignment-meta");
            content.getChildren().add(remaining);
        }
        return content;
    }

    private static HBox planRow(AssignmentPlanItem item, int rank) {
        Assignment assignment = item.assignment();
        Label rankLabel = new Label(Integer.toString(rank));
        rankLabel.getStyleClass().add("metric-value");
        rankLabel.setMinWidth(28);
        Label title = new Label(assignment.getTitle());
        title.getStyleClass().add("assignment-title");
        Label details = new Label(urgencyText(item) + "  •  " + assignment.getPriority() + " priority  •  Due " + assignment.getDueDate().format(DUE_FORMAT));
        details.getStyleClass().add("assignment-meta");
        details.setWrapText(true);
        VBox text = new VBox(3, title, details);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox row = new HBox(12, rankLabel, text);
        row.setPadding(new Insets(8, 0, 8, 0));
        return row;
    }

    static String urgencyText(AssignmentPlanItem item) {
        if (item == null) throw new IllegalArgumentException("Assignment plan item cannot be null.");
        return switch (item.urgency()) {
            case OVERDUE -> "OVERDUE · " + overdueText(item.minutesUntilDue());
            case DUE_TODAY -> "DUE TODAY · " + remainingText(item.minutesUntilDue());
            case DUE_SOON -> "DUE SOON · " + remainingText(item.minutesUntilDue());
            case UPCOMING -> "UPCOMING · " + remainingText(item.minutesUntilDue());
        };
    }

    private static String remainingText(long minutes) {
        long safe = Math.max(0, minutes);
        if (safe < 60) return safe + " min left";
        long hours = safe / 60;
        if (hours < 24) return hours + " hr left";
        long days = hours / 24;
        return days + (days == 1 ? " day left" : " days left");
    }

    private static String overdueText(long minutes) {
        long late = Math.abs(Math.min(0, minutes));
        if (late < 60) return late + " min late";
        long hours = late / 60;
        if (hours < 24) return hours + " hr late";
        long days = hours / 24;
        return days + (days == 1 ? " day late" : " days late");
    }
}
