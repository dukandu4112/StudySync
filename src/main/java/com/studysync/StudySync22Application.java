package com.studysync;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** StudySync 2.2 desktop entry point with the enhanced dashboard wired into the existing application. */
public final class StudySync22Application extends StudySyncApplication {
    private StudySyncService dashboardService;
    private BorderPane applicationRoot;

    @Override
    public void start(Stage stage) {
        super.start(stage);
        dashboardService = new StudySyncService(new DatabaseManager());
        applicationRoot = (BorderPane) stage.getScene().getRoot();
        wireDashboardNavigation();
        showEnhancedDashboard();
    }

    private void wireDashboardNavigation() {
        if (!(applicationRoot.getLeft() instanceof VBox navigation) || navigation.getChildren().isEmpty()) return;
        Node first = navigation.getChildren().get(0);
        if (!(first instanceof Button dashboardButton)) return;
        dashboardButton.setOnAction(event -> {
            navigation.getChildren().stream()
                    .filter(Button.class::isInstance)
                    .map(Button.class::cast)
                    .forEach(button -> button.getStyleClass().remove("navigation-button-active"));
            dashboardButton.getStyleClass().add("navigation-button-active");
            showEnhancedDashboard();
        });
    }

    private void showEnhancedDashboard() {
        VBox page = new VBox(18,
                styledLabel("Dashboard", "page-title"),
                styledLabel("A quick view of your courses, assignments, study progress, and planning priorities.", "page-subtitle"),
                DashboardView.create(dashboardService));
        page.setPadding(new Insets(28));
        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("page-scroll");
        applicationRoot.setCenter(scroll);
    }

    private Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
