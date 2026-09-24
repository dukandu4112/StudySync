package com.studysync;

import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * JavaFX presentation component for weekly study-progress analytics.
 */
public final class StudyProgressView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");

    private StudyProgressView() {
    }

    public static VBox create(StudyProgressAnalytics progress) {
        if (progress == null) {
            throw new IllegalArgumentException("Study progress cannot be null.");
        }

        Label period = new Label(
                progress.weekStart().format(DATE_FORMAT)
                        + " – "
                        + progress.weekEnd().format(DATE_FORMAT));
        period.getStyleClass().add("assignment-meta");

        GridPane metrics = new GridPane();
        metrics.setHgap(14);
        metrics.setVgap(10);
        metrics.add(metric("Study Minutes", Integer.toString(progress.totalStudyMinutes())), 0, 0);
        metrics.add(metric("Sessions", Integer.toString(progress.sessionCount())), 1, 0);
        metrics.add(metric("Active Days", progress.studyDays() + " / 7"), 2, 0);
        metrics.add(metric("Longest Session", progress.longestSessionMinutes() + " min"), 0, 1);
        metrics.add(metric(
                "Daily Average",
                String.format("%.1f min", progress.averageMinutesPerStudyDay())), 1, 1);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.333);
            column.setHgrow(Priority.ALWAYS);
            metrics.getColumnConstraints().add(column);
        }

        ProgressBar activeDays = new ProgressBar(progress.studyDays() / 7.0);
        activeDays.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(activeDays, Priority.ALWAYS);
        Label consistency = new Label(consistencyMessage(progress.studyDays()));
        consistency.getStyleClass().add("assignment-meta");
        HBox weeklyConsistency = new HBox(12, activeDays, consistency);

        VBox content = new VBox(12, period, metrics, weeklyConsistency);
        content.setPadding(new Insets(2, 0, 2, 0));
        VBox.setVgrow(metrics, Priority.NEVER);
        return content;
    }

    static String consistencyMessage(int studyDays) {
        if (studyDays == 0) {
            return "No study activity recorded this week yet.";
        }
        if (studyDays == 1) {
            return "1 active study day this week.";
        }
        if (studyDays == 7) {
            return "Studied every day this week.";
        }
        return studyDays + " active study days this week.";
    }

    private static VBox metric(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("assignment-meta");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("assignment-title");
        VBox box = new VBox(3, titleLabel, valueLabel);
        box.setMinWidth(130);
        box.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }
}
