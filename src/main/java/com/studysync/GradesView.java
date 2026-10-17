package com.studysync;

import java.util.Locale;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Persistent assessment editor and course-level weighted progress chart. */
public final class GradesView extends VBox {
    private final StudySyncService service;
    private AcademicGrade editing;
    private final VBox history = new VBox(12);
    public GradesView(StudySyncService service) { super(14); this.service = service; setPadding(new Insets(12)); render(); }
    private void render() {
        getChildren().clear();
        ComboBox<Course> course = new ComboBox<>();
        course.getItems().setAll(service.getCourses());
        course.setPromptText("Select course"); course.setId("grade-course");
        TextField title = new TextField(); title.setPromptText("Assessment title");
        TextField earned = new TextField(); earned.setPromptText("Points earned");
        TextField possible = new TextField("100");
        TextField weight = new TextField("1");
        title.setId("grade-title"); earned.setId("grade-earned"); possible.setId("grade-possible"); weight.setId("grade-weight");
        if (editing != null) {
            course.getItems().stream().filter(c -> c.getId() == editing.courseId()).findFirst().ifPresent(course::setValue);
            title.setText(editing.title()); earned.setText("" + editing.earned());
            possible.setText("" + editing.possible()); weight.setText("" + editing.weight());
        }
        GridPane form = new GridPane(); form.setHgap(12); form.setVgap(8);
        String[] labels = {"Course", "Assessment", "Earned points", "Possible points", "Relative weight"};
        javafx.scene.Node[] inputs = {course, title, earned, possible, weight};
        for (int i = 0; i < inputs.length; i++) { Label label = new Label(labels[i]); label.setLabelFor(inputs[i]); inputs[i].setAccessibleText(labels[i]); form.add(label, 0, i); form.add(inputs[i], 1, i); }
        Button save = new Button(editing == null ? "Add grade" : "Save changes");
        save.setId("grade-save");
        save.setOnAction(e -> {
            try {
                if (course.getValue() == null) throw new IllegalArgumentException("Select a course.");
                service.saveAcademicGrade(new AcademicGrade(editing == null ? 0 : editing.id(), course.getValue().getId(), title.getText(), Double.parseDouble(earned.getText()), Double.parseDouble(possible.getText()), Double.parseDouble(weight.getText())));
                editing = null; render();
            } catch (RuntimeException ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); }
        });
        Button cancel = new Button("Clear / Cancel edit"); cancel.setOnAction(e -> { editing = null; render(); });
        getChildren().addAll(new Label("Weights are relative to the other recorded assessments in the same course."), form, new HBox(12, save, cancel));
        CategoryAxis x = new CategoryAxis(); NumberAxis y = new NumberAxis(0, 100, 10);
        BarChart<String, Number> chart = new BarChart<>(x, y); chart.setTitle("Recorded course grades (%)"); chart.setLegendVisible(false); chart.setAnimated(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        var courses = service.getCourses(); var percentages = service.getCourseGradePercentages();
        StringBuilder summary = new StringBuilder("Recorded weighted course averages: ");
        for (Course c : courses) {
            Double value = percentages.get(c.getId());
            if (value != null) {
                series.getData().add(new XYChart.Data<>(c.getCode(), value));
                summary.append(String.format(Locale.ROOT, "%s %.1f%%. ", c.getCode(), value));
            }
        }
        if (percentages.isEmpty()) summary.append("No grades recorded.");
        chart.setAccessibleText(summary.toString());
        Label chartSummary = new Label(summary.toString()); chartSummary.setWrapText(true);
        chart.getData().add(series); chart.setMinHeight(250); getChildren().addAll(chart, chartSummary);
        if (service.getAcademicGrades().isEmpty()) getChildren().add(new Label("No grades recorded yet."));
        var page = DesktopPaging.slice(history, service.getAcademicGrades(), "grades");
        history.getChildren().clear();
        for (AcademicGrade g : page.items()) {
            String code = courses.stream().filter(c -> c.getId() == g.courseId()).map(Course::getCode).findFirst().orElse("");
            Label label = new Label(String.format(Locale.ROOT, "%s · %s — %.2f / %.2f (%.1f%%), weight %.2f", code, g.title(), g.earned(), g.possible(), g.percentage(), g.weight()));
            label.setWrapText(true); HBox.setHgrow(label, Priority.ALWAYS);
            Button edit = new Button("Edit"); edit.setOnAction(e -> { editing = g; render(); });
            Button delete = new Button("Delete"); delete.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this grade?", ButtonType.CANCEL, ButtonType.OK);
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try { service.deleteAcademicGrade(g.id()); if (editing != null && editing.id() == g.id()) editing = null; render(); }
                    catch (RuntimeException ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); }
                }
            });
            history.getChildren().add(new HBox(12, label, edit, delete));
        }
        DesktopPaging.controls(history, page, this::render);
        getChildren().add(history);
    }
}
