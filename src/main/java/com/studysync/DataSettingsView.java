package com.studysync;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

/** Explicit local data actions; restore always preserves a recovery snapshot. */
public final class DataSettingsView extends VBox {
    private final DatabaseManager database;
    private final DataTransfer transfer;
    private final Label status = new Label();
    public DataSettingsView(DatabaseManager database, StudySyncService service) {
        super(16); this.database = database; this.transfer = new DataTransfer(database);
        getChildren().addAll(new Label("StudySync desktop — local data and preferences"), new Label("Data stays on this computer. Backups include courses, assignments, study sessions, grades, and settings."));
        TextField target = new TextField(Integer.toString(service.getDailyStudyTargetMinutes()));
        Button save = new Button("Save daily goal (minutes)");
        save.setOnAction(e -> action(() -> service.setDailyStudyTargetMinutes(Integer.parseInt(target.getText())), "Daily goal saved."));
        CheckBox reminders = new CheckBox("Show deadline reminders inside StudySync");
        reminders.setSelected(!"false".equals(database.getSetting("reminders_enabled")));
        reminders.setOnAction(e -> action(() -> database.setSetting("reminders_enabled", Boolean.toString(reminders.isSelected())), "Reminder preference saved."));
        Button backup = new Button("Create database backup"); backup.setOnAction(e -> {
            File file = choose(true, "Create backup", "studysync-backup.db", "*.db");
            if (file != null) action(() -> transfer.backup(file.toPath()), "Backup created: " + file.getName());
        });
        Button restore = new Button("Restore / import database backup"); restore.setOnAction(e -> {
            File file = choose(false, "Choose StudySync backup", null, "*.db"); if (file == null) return;
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Replace current data with this backup? A recovery backup will be created first. Backups must be created by this desktop version.", ButtonType.CANCEL, ButtonType.OK);
            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            action(() -> {
                Path recovery = Files.createDirectories(Path.of("recovery-backups")).resolve("before-restore-" + java.util.UUID.randomUUID() + ".db");
                transfer.backup(recovery); transfer.restore(file.toPath());
                target.setText(Integer.toString(service.getDailyStudyTargetMinutes()));
                reminders.setSelected(!"false".equals(database.getSetting("reminders_enabled")));
            }, "Data restored. Open any page to see refreshed data. Recovery backup saved in recovery-backups.");
        });
        Button export = new Button("Export report to CSV"); export.setOnAction(e -> {
            File file = choose(true, "Export report", "studysync-report.csv", "*.csv");
            if (file != null) action(() -> transfer.exportCsv(file.toPath()), "CSV report exported.");
        });
        status.setWrapText(true);
        getChildren().addAll(new HBox(12, target, save), reminders, backup, restore, export, status);
    }
    private File choose(boolean save, String title, String name, String pattern) {
        FileChooser chooser = new FileChooser(); chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("StudySync files", pattern));
        if (name != null) chooser.setInitialFileName(name);
        return save ? chooser.showSaveDialog(getScene().getWindow()) : chooser.showOpenDialog(getScene().getWindow());
    }
    private void action(CheckedAction action, String success) {
        try { action.run(); status.setText(success); }
        catch (Exception e) { status.setText("Action failed; check the selected file or entered value."); new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
    }
    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
}
