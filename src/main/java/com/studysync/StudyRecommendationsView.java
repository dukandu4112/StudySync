package com.studysync;

import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** JavaFX presentation for ranked smart study recommendations. */
public final class StudyRecommendationsView {
    private static final int DASHBOARD_LIMIT = 3;

    private StudyRecommendationsView() {}

    public static VBox create(List<StudyRecommendation> recommendations) {
        if (recommendations == null) throw new IllegalArgumentException("Recommendations cannot be null.");

        VBox list = new VBox(12);
        if (recommendations.isEmpty()) {
            list.getChildren().add(styledLabel("Add a course to receive study recommendations.", "assignment-meta"));
            return list;
        }

        recommendations.stream().limit(DASHBOARD_LIMIT).forEach(recommendation -> list.getChildren().add(card(recommendation)));
        return list;
    }

    private static VBox card(StudyRecommendation recommendation) {
        String course = recommendation.course().getCode() + " — " + recommendation.course().getName();
        String workload = recommendation.pendingAssignments() + " pending • "
                + recommendation.highPriorityAssignments() + " high priority • "
                + recommendation.weeklyRemainingMinutes() + " study min remaining";
        String urgency = recommendation.hasDueSoonAssignment() ? " • Due within 48 hours" : "";

        VBox card = new VBox(6,
                styledLabel(course, "assignment-title"),
                styledLabel(recommendation.reason(), "metric-value"),
                styledLabel(workload + urgency, "assignment-meta"));
        card.setStyle("-fx-padding: 12;");
        card.getStyleClass().add("metric-card");
        return card;
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
