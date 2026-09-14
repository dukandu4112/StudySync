package com.studysync;

import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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

        VBox content = new VBox(12, period, metrics);
        content.setPadding(new Insets(2, 0, 2, 0));
        VBox.setVgrow(metrics, Priority.NEVER);
        return content;
    }

    private static VBox metric(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("assignment-meta");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("assignment-title");
        VBox box = new VBox(3, titleLabel, valueLabel);
        box.setMinWidth(150);
        return box;
    }
}
