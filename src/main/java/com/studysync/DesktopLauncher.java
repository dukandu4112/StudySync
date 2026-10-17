package com.studysync;

import java.nio.file.*;
import javafx.application.Application;

/** Plain Java launcher for packaged JavaFX applications. */
public final class DesktopLauncher {
    private DesktopLauncher() { }
    public static void main(String[] args) throws Exception {
        if (System.getProperty("studysync.database") == null) {
            Path existing = Path.of("studysync.db").toAbsolutePath();
            Path folder = Path.of(System.getProperty("user.home"), ".studysync");
            Files.createDirectories(folder);
            Path database = Files.isRegularFile(existing) ? existing : folder.resolve("studysync.db");
            System.setProperty("studysync.database", "jdbc:sqlite:" + database);
        }
        Application.launch(StudySyncApplication.class, args);
    }
}
