package com.studysync;

import java.nio.file.*;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class UpgradePersistenceTest {
    @TempDir Path folder;
    private String url(Path path) { return "jdbc:sqlite:" + path; }
    private void legacy(Path path) throws Exception {
        try (Connection c = DriverManager.getConnection(url(path)); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE courses(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,code TEXT NOT NULL UNIQUE COLLATE NOCASE)");
            s.execute("CREATE TABLE assignments(id INTEGER PRIMARY KEY AUTOINCREMENT,course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,title TEXT NOT NULL,description TEXT NOT NULL DEFAULT '',due_date TEXT NOT NULL,priority TEXT NOT NULL,completed INTEGER NOT NULL DEFAULT 0)");
            s.execute("CREATE TABLE study_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE,start_time TEXT NOT NULL,duration_minutes INTEGER NOT NULL,notes TEXT NOT NULL DEFAULT '')");
            s.execute("INSERT INTO courses VALUES(41,'Legacy course','LEGACY')");
            s.execute("INSERT INTO assignments VALUES(7,41,'Original task','Keep notes','2026-12-20T12:00','HIGH',1)");
            s.execute("INSERT INTO study_sessions VALUES(8,41,'2026-10-01T10:00',45,'Original session')");
        }
    }
    @Test void olderDatabaseUpgradePreservesRecordsAndRepeatedLaunches() throws Exception {
        Path file = folder.resolve("legacy.db"); legacy(file);
        for (int launch = 0; launch < 10; launch++) {
            var db = new DatabaseManager(url(file)); var service = new StudySyncService(db);
            assertEquals(41, service.getCourses().get(0).getId());
            assertEquals("Original task", service.getAssignments().get(0).getTitle());
            assertTrue(service.getAssignments().get(0).isCompleted()); assertEquals(45, service.getTotalStudyMinutes());
            if (launch == 0) {
                service.setDailyStudyTargetMinutes(85);
                service.saveAcademicGrade(new AcademicGrade(0, 41, "Migrated grade", 90, 100, 1));
            }
            assertEquals(85, service.getDailyStudyTargetMinutes()); assertEquals(1, service.getAcademicGrades().size());
        }
    }
    @Test void legacyBackupImportsWithoutChangingSource() throws Exception {
        Path source = folder.resolve("legacy.db"); legacy(source);
        byte[] original = Files.readAllBytes(source);
        var live = new DatabaseManager(url(folder.resolve("live.db")));
        new DataTransfer(live).restore(source);
        assertEquals("LEGACY", live.getAllCourses().get(0).getCode());
        assertEquals(45, new StudySyncService(live).getTotalStudyMinutes());
        assertArrayEquals(original, Files.readAllBytes(source));
    }
    @Test void newerDatabaseIsRejectedWithoutSchemaOrDataChanges() throws Exception {
        Path file = folder.resolve("future.db"); legacy(file);
        try (var c = DriverManager.getConnection(url(file)); var s = c.createStatement()) { s.execute("PRAGMA user_version=999"); }
        byte[] before = Files.readAllBytes(file);
        assertThrows(IllegalStateException.class, () -> new DatabaseManager(url(file)));
        assertArrayEquals(before, Files.readAllBytes(file));
        var live = new DatabaseManager(url(folder.resolve("live.db"))); live.addCourse("Current", "CURRENT");
        assertThrows(java.io.IOException.class, () -> new DataTransfer(live).restore(file));
        assertEquals("CURRENT", live.getAllCourses().get(0).getCode());
    }
    @Test void failedSchemaUpgradeRollsBackAndLeavesLegacyDataUsable() throws Exception {
        Path file = folder.resolve("bad-schema.db"); legacy(file);
        try (var c = DriverManager.getConnection(url(file)); var s = c.createStatement()) {
            s.execute("DROP TABLE study_sessions"); s.execute("CREATE TABLE study_sessions(id INTEGER PRIMARY KEY,wrong_column TEXT)");
        }
        assertThrows(IllegalStateException.class, () -> new DatabaseManager(url(file)));
        try (var c = DriverManager.getConnection(url(file)); var s = c.createStatement()) {
            try (var r = s.executeQuery("SELECT name FROM courses WHERE id=41")) { assertTrue(r.next()); assertEquals("Legacy course", r.getString(1)); }
            try (var r = s.executeQuery("PRAGMA user_version")) { assertTrue(r.next()); assertEquals(0, r.getInt(1)); }
            try (var r = s.executeQuery("SELECT count(*) FROM sqlite_master WHERE name='academic_grades'")) { assertTrue(r.next()); assertEquals(0, r.getInt(1)); }
        }
    }
}
