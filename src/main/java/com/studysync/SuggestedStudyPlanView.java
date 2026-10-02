package com.studysync;

import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** JavaFX presentation for StudySync's actionable suggested study plan. */
public final class SuggestedStudyPlanView {
    private SuggestedStudyPlanView() {}

    public static VBox create(List<StudyPlanItem> plan) {
        if (plan == null) throw new IllegalArgumentException("Study plan cannot be null.");
        VBox list = new VBox(12);
        if (plan.isEmpty()) {
            list.getChildren().add(styledLabel("Today's study target is complete, or no study blocks are needed right now.", "assignment-meta"));
            return list;
        }

        int totalMinutes = plan.stream().mapToInt(StudyPlanItem::suggestedMinutes).sum();
        list.getChildren().add(styledLabel("Suggested total: " + totalMinutes + " minutes", "assignment-title"));
        for (int i = 0; i < plan.size(); i++) {
            list.getChildren().add(block(i + 1, plan.get(i)));
        }
        return list;
    }

    private static VBox block(int number, StudyPlanItem item) {
        Label order = styledLabel("Block " + number, "metric-title");
        Label minutes = styledLabel(item.suggestedMinutes() + " min", "metric-value");
        HBox heading = new HBox(12, order, minutes);
        Label course = styledLabel(item.course().getCode() + " — " + item.course().getName(), "assignment-title");
        Label reason = styledLabel(item.reason(), "assignment-meta");
        VBox card = new VBox(6, heading, course, reason);
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
