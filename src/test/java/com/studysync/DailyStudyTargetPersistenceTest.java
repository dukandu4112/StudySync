package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DailyStudyTargetPersistenceTest {
    @TempDir Path tempDir;

    @Test
    void defaultsToTwoHoursWhenNoTargetHasBeenSaved() {
        StudySyncService service = service("default.db");
        assertEquals(120, service.getDailyStudyTargetMinutes());
        assertEquals(120, service.getDailyStudyGoal().targetMinutes());
    }

    @Test
    void savedTargetSurvivesServiceAndDatabaseRecreation() {
        String url = "jdbc:sqlite:" + tempDir.resolve("persistent.db");
        StudySyncService first = new StudySyncService(new DatabaseManager(url));
        first.setDailyStudyTargetMinutes(180);

        StudySyncService reopened = new StudySyncService(new DatabaseManager(url));
        assertEquals(180, reopened.getDailyStudyTargetMinutes());
        assertEquals(180, reopened.getDailyStudyGoal().targetMinutes());
    }

    @Test
    void savedTargetCanBeChanged() {
        StudySyncService service = service("change.db");
        service.setDailyStudyTargetMinutes(90);
        assertEquals(90, service.getDailyStudyTargetMinutes());
        service.setDailyStudyTargetMinutes(150);
        assertEquals(150, service.getDailyStudyTargetMinutes());
    }

    @Test
    void rejectsNonPositiveTargets() {
        StudySyncService service = service("invalid.db");
        assertThrows(IllegalArgumentException.class, () -> service.setDailyStudyTargetMinutes(0));
        assertThrows(IllegalArgumentException.class, () -> service.setDailyStudyTargetMinutes(-15));
    }

    private StudySyncService service(String fileName) {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve(fileName)));
    }
}
