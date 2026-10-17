package com.studysync;

import java.io.File;
import java.nio.file.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

/** Explicit local data actions; restore always preserves a recovery snapshot. */
public final class DataSettingsView extends VBox {
    private final DatabaseManager database;
    private final DataTransfer transfer;
    private final Label status = new Label();
    private final FileSelection fileSelection;
    private final Path recoveryDirectory;
    @FunctionalInterface interface FileSelection {
        File choose(javafx.stage.Window owner, boolean save, String title, String name, String pattern);
    }
    public DataSettingsView(DatabaseManager database, StudySyncService service) {
        this(database, service, DataSettingsView::nativeChoose,
                Path.of(System.getProperty("user.home"), ".studysync", "recovery-backups"));
    }
    DataSettingsView(DatabaseManager database, StudySyncService service, FileSelection fileSelection, Path recoveryDirectory) {
        super(16); this.database = database; this.transfer = new DataTransfer(database);
        this.fileSelection = java.util.Objects.requireNonNull(fileSelection);
        this.recoveryDirectory = java.util.Objects.requireNonNull(recoveryDirectory);
        getChildren().addAll(new Label("StudySync desktop — local data and preferences"), new Label("Data stays on this computer. Backups include courses, assignments, study sessions, grades, and settings."));
        TextField target = new TextField(Integer.toString(service.getDailyStudyTargetMinutes()));
        Label targetLabel = new Label("Daily study goal (minutes)"); targetLabel.setLabelFor(target);
        target.setAccessibleText("Daily study goal in minutes");
        Button save = new Button("Save daily goal (minutes)");
        save.setOnAction(e -> action(() -> service.setDailyStudyTargetMinutes(Integer.parseInt(target.getText())), "Daily goal saved."));
        CheckBox reminders = new CheckBox("Show deadline reminders inside StudySync");
        reminders.setSelected(!"false".equals(database.getSetting("reminders_enabled")));
        reminders.setOnAction(e -> action(() -> database.setSetting("reminders_enabled", Boolean.toString(reminders.isSelected())), "Reminder preference saved."));
        Button backup = new Button("Create database _backup"); backup.setOnAction(e -> {
            File file = choose(true, "Create backup", "studysync-backup.db", "*.db");
            if (file != null) action(() -> transfer.backup(file.toPath()), "Backup created: " + file.getName());
        });
        Button restore = new Button("_Restore / import database backup"); restore.setOnAction(e -> {
            File file = choose(false, "Choose StudySync backup", null, "*.db"); if (file == null) return;
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Replace current data with this backup? A recovery backup will be created first. Compatible backups from older StudySync versions are supported.", ButtonType.CANCEL, ButtonType.OK);
            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            action(() -> {
                Path recovery = Files.createDirectories(recoveryDirectory).resolve("before-restore-" + java.util.UUID.randomUUID() + ".db");
                transfer.backup(recovery); transfer.restore(file.toPath());
                target.setText(Integer.toString(service.getDailyStudyTargetMinutes()));
                reminders.setSelected(!"false".equals(database.getSetting("reminders_enabled")));
            }, "Data restored. Open any page to see refreshed data. Recovery backup saved in your home folder under .studysync/recovery-backups.");
        });
        Button export = new Button("_Export report to CSV"); export.setOnAction(e -> {
            File file = choose(true, "Export report", "studysync-report.csv", "*.csv");
            if (file != null) action(() -> transfer.exportCsv(file.toPath()), "CSV report exported.");
        });
        backup.setAccessibleText("Create database backup"); restore.setAccessibleText("Restore database backup"); export.setAccessibleText("Export CSV report");
        backup.setMnemonicParsing(true); restore.setMnemonicParsing(true); export.setMnemonicParsing(true);
        status.setAccessibleRole(javafx.scene.AccessibleRole.TEXT); status.setWrapText(true);
        getChildren().addAll(new VBox(6, targetLabel, new HBox(12, target, save)), reminders, backup, restore, export, status);
    }
    private File choose(boolean save, String title, String name, String pattern) {
        return fileSelection.choose(getScene().getWindow(), save, title, name, pattern);
    }
    private static File nativeChoose(javafx.stage.Window owner, boolean save, String title, String name, String pattern) {
        FileChooser chooser = new FileChooser(); chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("StudySync files", pattern));
        if (name != null) chooser.setInitialFileName(name);
        return save ? chooser.showSaveDialog(owner) : chooser.showOpenDialog(owner);
    }
    private void action(CheckedAction action, String success) {
        try { action.run(); status.setText(success); }
        catch (Exception e) { status.setText("Action failed; check the selected file or entered value."); new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
    }
    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
}
