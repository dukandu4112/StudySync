package com.studysync;

import java.time.LocalDateTime;
import java.util.Locale;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Cross-feature search and in-app deadline notifications. */
public final class FindView extends VBox {
    public FindView(StudySyncService service, DatabaseManager database) {
        super(14);
        TextField query = new TextField(); query.setPromptText("Search courses, assignments, study notes, and grades");
        query.setAccessibleText("Search all StudySync data");
        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().addAll("All", "Courses", "Assignments", "Study notes", "Grades"); filter.setValue("All");
        VBox results = new VBox(10);
        Runnable refresh = () -> {
            results.getChildren().clear(); String q = query.getText().strip().toLowerCase(Locale.ROOT);
            String type = filter.getValue();
            if (type.equals("All") || type.equals("Courses")) for (Course c : service.getCourses())
                if ((c.getCode() + " " + c.getName()).toLowerCase(Locale.ROOT).contains(q)) results.getChildren().add(new Label("Course · " + c));
            if (type.equals("All") || type.equals("Assignments")) for (Assignment a : service.getAssignments())
                if ((a.getTitle() + " " + a.getDescription()).toLowerCase(Locale.ROOT).contains(q)) {
                    Label text = new Label("Assignment · " + a.getTitle() + " · " + a.getDueDate() + (a.isCompleted() ? " · Completed" : " · " + a.getPriority()));
                    text.setWrapText(true); results.getChildren().add(text);
                }
            if (type.equals("All") || type.equals("Study notes")) for (StudySession s : service.getStudySessions())
                if (s.getNotes().toLowerCase(Locale.ROOT).contains(q)) { Label text = new Label("Study · " + s); text.setWrapText(true); results.getChildren().add(text); }
            if (type.equals("All") || type.equals("Grades")) for (AcademicGrade g : service.getAcademicGrades())
                if (g.title().toLowerCase(Locale.ROOT).contains(q)) results.getChildren().add(new Label("Grade · " + g.title() + " · " + String.format(Locale.ROOT, "%.1f%%", g.percentage())));
            if (results.getChildren().isEmpty()) results.getChildren().add(new Label("No results match your search."));
        };
        query.textProperty().addListener((o, a, b) -> refresh.run());
        filter.valueProperty().addListener((o, a, b) -> refresh.run());
        Button clear = new Button("Clear"); clear.setOnAction(e -> { query.clear(); filter.setValue("All"); });
        getChildren().addAll(new HBox(12, query, filter, clear), new Label("Deadline reminders"));
        if ("false".equals(database.getSetting("reminders_enabled"))) getChildren().add(new Label("Reminders are disabled in Data & Settings."));
        else {
            var due = service.getPendingAssignments().stream().filter(a -> !a.getDueDate().isAfter(LocalDateTime.now().plusHours(48))).toList();
            if (due.isEmpty()) getChildren().add(new Label("No pending deadlines within 48 hours."));
            for (Assignment a : due) {
                Label text = new Label((a.isOverdue() ? "Overdue · " : "Due soon · ") + a.getTitle() + " · " + a.getDueDate());
                text.setWrapText(true); getChildren().add(text);
            }
        }
        getChildren().addAll(new Separator(), new Label("Search results"), results); refresh.run();
    }
}
