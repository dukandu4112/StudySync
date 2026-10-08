package com.studysync;

import java.time.LocalDateTime;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** JavaFX presentation for StudySync's actionable suggested study plan. */
public final class SuggestedStudyPlanView {
    private SuggestedStudyPlanView() {}

    public static VBox create(List<StudyPlanItem> plan) {
        return create(plan, null, null);
    }

    /**
     * Builds the plan and optionally wires completion actions to the StudySync service.
     * The refresh callback lets the owning dashboard rebuild itself after a block is logged.
     */
    public static VBox create(List<StudyPlanItem> plan, StudySyncService service, Runnable refresh) {
        if (plan == null) throw new IllegalArgumentException("Study plan cannot be null.");
        VBox list = new VBox(12);
        if (plan.isEmpty()) {
            list.getChildren().add(styledLabel("Today's study target is complete, or no study blocks are needed right now.", "assignment-meta"));
            return list;
        }

        int totalMinutes = plan.stream().mapToInt(StudyPlanItem::suggestedMinutes).sum();
        list.getChildren().add(styledLabel("Suggested plan: " + plan.size() + (plan.size() == 1 ? " block" : " blocks") + " · " + totalMinutes + " minutes", "assignment-title"));
        for (int i = 0; i < plan.size(); i++) {
            list.getChildren().add(block(i + 1, plan.size(), plan.get(i), service, refresh));
        }
        return list;
    }

    private static VBox block(int number, int totalBlocks, StudyPlanItem item, StudySyncService service, Runnable refresh) {
        Label order = styledLabel("Block " + number, "metric-title");
        Label minutes = styledLabel(item.suggestedMinutes() + " min", "metric-value");
        Label priority = styledLabel(item.priorityLabel(), "assignment-meta");
        Label effort = styledLabel(item.effortLabel(), "assignment-meta");
        HBox heading = new HBox(12, order, minutes, priority, effort);
        Label course = styledLabel(item.course().getCode() + " — " + item.course().getName(), "assignment-title");
        course.setAccessibleText(item.accessiblePlanLabel(number, totalBlocks));
        Label reason = styledLabel(item.reason(), "assignment-meta");
        Label sequence = styledLabel(item.sequenceCue(number, totalBlocks), "assignment-meta");
        Label action = styledLabel("Next step: " + item.actionCue(), "assignment-title");
        Label guidance = styledLabel("Focus: " + item.focusGuidance(), "assignment-meta");
        Label breakGuidance = styledLabel(item.breakGuidance(), "assignment-meta");
        VBox card = new VBox(6, heading, course, sequence, reason, action, guidance, breakGuidance);

        if (service != null) {
            Button complete = new Button("Complete " + item.suggestedMinutes() + " min block");
            complete.getStyleClass().add("primary-button");
            Label status = styledLabel("", "assignment-meta");
            complete.setOnAction(event -> {
                try {
                    service.recordStudySession(
                            item.course().getId(),
                            LocalDateTime.now().minusMinutes(item.suggestedMinutes()),
                            item.suggestedMinutes(),
                            "Completed suggested study plan block: " + item.reason());
                    complete.setDisable(true);
                    status.setText("Completed and added to Study Sessions.");
                    if (refresh != null) refresh.run();
                } catch (RuntimeException ex) {
                    status.setText("Could not record this study block: " + ex.getMessage());
                }
            });
            card.getChildren().addAll(complete, status);
        }

        card.setStyle("-fx-padding: 12;");
        card.getStyleClass().add("metric-card");
        return card;
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
