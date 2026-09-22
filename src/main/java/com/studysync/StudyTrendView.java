package com.studysync;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** JavaFX presentation for the week-over-week study-time trend. */
public final class StudyTrendView {
    private StudyTrendView() {}

    public static Node create(StudyTrend trend) {
        VBox root = new VBox(8);
        root.setPadding(new Insets(4, 0, 4, 0));

        Label headline = new Label(headline(trend));
        headline.getStyleClass().add("assignment-title");

        HBox totals = new HBox(22);
        totals.getChildren().addAll(
                metric("This Week", trend.currentWeekMinutes() + " min"),
                metric("Last Week", trend.previousWeekMinutes() + " min"),
                metric("Change", signedMinutes(trend.minuteChange())));

        Label detail = new Label(detail(trend));
        detail.setWrapText(true);
        detail.getStyleClass().add("assignment-meta");

        root.getChildren().addAll(headline, totals, detail);
        return root;
    }

    private static VBox metric(String label, String value) {
        Label name = new Label(label);
        name.getStyleClass().add("assignment-meta");
        Label amount = new Label(value);
        amount.getStyleClass().add("assignment-title");
        return new VBox(2, name, amount);
    }

    private static String headline(StudyTrend trend) {
        return switch (trend.direction()) {
            case UP -> "Study time is up this week";
            case DOWN -> "Study time is down this week";
            case SAME -> "Study time is steady this week";
        };
    }

    private static String detail(StudyTrend trend) {
        if (trend.previousWeekMinutes() == 0) {
            return trend.currentWeekMinutes() == 0
                    ? "No study time has been recorded in either week yet."
                    : "You have started this week with " + trend.currentWeekMinutes()
                            + " minutes; there was no study time last week.";
        }
        double magnitude = Math.abs(trend.percentageChange());
        return String.format("%.1f%% %s than last week.", magnitude,
                trend.direction() == StudyTrend.Direction.UP ? "more"
                        : trend.direction() == StudyTrend.Direction.DOWN ? "less" : "change");
    }

    private static String signedMinutes(int minutes) {
        if (minutes > 0) return "+" + minutes + " min";
        return minutes + " min";
    }
}
