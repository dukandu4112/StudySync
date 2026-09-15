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

    private AssignmentPlanView() {
    }

    public static VBox create(List<AssignmentPlanItem> plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Assignment plan cannot be null.");
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(2, 0, 2, 0));

        if (plan.isEmpty()) {
            Label empty = new Label("You're caught up — no pending assignments need attention.");
            empty.getStyleClass().add("assignment-meta");
            content.getChildren().add(empty);
            return content;
        }

        int visible = Math.min(plan.size(), DASHBOARD_LIMIT);
        for (int i = 0; i < visible; i++) {
            content.getChildren().add(planRow(plan.get(i), i + 1));
        }

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
        Label details = new Label(
                urgencyLabel(item.urgency())
                        + "  •  " + assignment.getPriority() + " priority"
                        + "  •  Due " + assignment.getDueDate().format(DUE_FORMAT));
        details.getStyleClass().add("assignment-meta");

        VBox text = new VBox(3, title, details);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox row = new HBox(12, rankLabel, text);
        row.setPadding(new Insets(8, 0, 8, 0));
        return row;
    }

    static String urgencyLabel(AssignmentPlanItem.Urgency urgency) {
        if (urgency == null) {
            throw new IllegalArgumentException("Assignment urgency cannot be null.");
        }
        return switch (urgency) {
            case OVERDUE -> "Overdue";
            case DUE_TODAY -> "Due today";
            case DUE_SOON -> "Due soon";
            case UPCOMING -> "Upcoming";
        };
    }
}
