package com.studysync;

import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** JavaFX summary showing how today's generated study plan relates to the daily study target. */
public final class StudyPlanSummaryView {
    private StudyPlanSummaryView() {}

    public static VBox create(StudyPlanSummary summary) {
        if (summary == null) throw new IllegalArgumentException("Study plan summary cannot be null.");

        GridPane metrics = new GridPane();
        metrics.setHgap(12);
        metrics.setVgap(12);
        metrics.add(metric("Studied Today", summary.studiedTodayMinutes() + " min"), 0, 0);
        metrics.add(metric("Planned", summary.plannedMinutes() + " min"), 1, 0);
        metrics.add(metric("Daily Goal", summary.dailyTargetMinutes() + " min"), 2, 0);
        metrics.add(metric("Study Blocks", summary.blockCount()), 0, 1);
        metrics.add(metric("Still Uncovered", summary.remainingAfterPlanMinutes() + " min"), 1, 1);
        metrics.add(metric("Plan Coverage", summary.coversRemainingDailyTarget() ? "Covered" : "Needs more time"), 2, 1);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.333);
            column.setHgrow(Priority.ALWAYS);
            metrics.getColumnConstraints().add(column);
        }

        String message;
        if (summary.studiedTodayMinutes() >= summary.dailyTargetMinutes()) {
            message = "Today's study goal is complete.";
        } else if (summary.coversRemainingDailyTarget()) {
            message = "Today's suggested plan covers the rest of your daily study goal.";
        } else if (summary.plannedMinutes() == 0) {
            message = "No study blocks are available yet for the remaining " + summary.remainingAfterPlanMinutes() + " minutes.";
        } else {
            message = "The current plan leaves " + summary.remainingAfterPlanMinutes() + " minutes to schedule after these blocks.";
        }

        return new VBox(12, metrics, styledLabel(message, "assignment-meta"));
    }

    private static VBox metric(String title, Object value) {
        VBox box = new VBox(5,
                styledLabel(title, "metric-title"),
                styledLabel(String.valueOf(value), "metric-value"));
        box.setStyle("-fx-padding: 12;");
        box.getStyleClass().add("metric-card");
        return box;
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
