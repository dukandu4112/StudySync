package com.studysync;

import java.nio.file.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DesktopMilestonesTest {
    @TempDir Path directory;
    DatabaseManager db(String name) { return new DatabaseManager("jdbc:sqlite:" + directory.resolve(name)); }
    @Test void weightedGradesPersistAndCascade() {
        var db = db("grades.db"); var service = new StudySyncService(db);
        var course = service.createCourse("Math", "MAT");
        assertTrue(service.getCourseGradePercentage(course.getId()).isEmpty());
        service.saveAcademicGrade(new AcademicGrade(0, course.getId(), "Exam", 90, 100, 3));
        service.saveAcademicGrade(new AcademicGrade(0, course.getId(), "Quiz", 10, 20, 1));
        assertEquals(80, service.getCourseGradePercentage(course.getId()).orElseThrow(), 0.001);
        var grade = service.getAcademicGrades().get(0);
        service.saveAcademicGrade(new AcademicGrade(grade.id(), course.getId(), "Quiz", 20, 20, 1));
        assertEquals(92.5, service.getCourseGradePercentage(course.getId()).orElseThrow(), 0.001);
        service.deleteCourse(course.getId()); assertTrue(service.getAcademicGrades().isEmpty());
    }
    @Test void rejectsInvalidAndNonFiniteGrades() {
        assertEquals(100, new AcademicGrade(0, 1, "Large values", 1e308, 1e308, 1).percentage());
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -1, 101})
            assertThrows(IllegalArgumentException.class, () -> new AcademicGrade(0, 1, "Exam", value, 100, 1));
        assertThrows(IllegalArgumentException.class, () -> new AcademicGrade(0, 1, "Exam", 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new AcademicGrade(0, 1, "Exam", 1, 100, Double.NaN));
    }
    @Test void backupRestoresAllDataAndRefusesOverwrite() throws Exception {
        var db = db("live.db"); var service = new StudySyncService(db); var transfer = new DataTransfer(db);
        var c = service.createCourse("Physics", "PHY");
        service.createAssignment(c.getId(), "Lab", "notes", LocalDateTime.of(2026, 11, 1, 12, 0), Assignment.Priority.HIGH);
        service.recordStudySession(c.getId(), LocalDateTime.of(2026, 10, 1, 12, 0), 30, "chapter");
        service.saveAcademicGrade(new AcademicGrade(0, c.getId(), "Lab", 8, 10, 1));
        service.setDailyStudyTargetMinutes(90);
        Path backup = directory.resolve("backup with spaces.db"); transfer.backup(backup);
        assertThrows(Exception.class, () -> transfer.backup(backup));
        service.deleteCourse(c.getId()); service.setDailyStudyTargetMinutes(10);
        transfer.restore(backup);
        assertEquals(1, service.getCourses().size()); assertEquals(1, service.getAssignments().size());
        assertEquals(30, service.getTotalStudyMinutes()); assertEquals(1, service.getAcademicGrades().size());
        assertEquals(90, service.getDailyStudyTargetMinutes());
    }
    @Test void invalidImportLeavesLiveDataIntact() throws Exception {
        var db = db("live.db"); var transfer = new DataTransfer(db);
        db.addCourse("Original", "ORG");
        Path invalid = directory.resolve("invalid.db"); Files.writeString(invalid, "not a database");
        assertThrows(Exception.class, () -> transfer.restore(invalid));
        assertEquals("ORG", db.getAllCourses().get(0).getCode());
    }
    @Test void reportEscapesQuotesNewlinesAndFormulas() throws Exception {
        var db = db("report.db"); db.addCourse("=SUM(1,2)\n\"quoted\"", "CSV");
        Path report = directory.resolve("report.csv"); new DataTransfer(db).exportCsv(report);
        String csv = Files.readString(report);
        assertTrue(csv.contains("\"'=SUM(1,2)\n\"\"quoted\"\"\""));
        assertThrows(Exception.class, () -> new DataTransfer(db).exportCsv(report));
    }

    @Test void rejectedImportRollsBackDeletesAndInserts() throws Exception {
        var live = db("live.db"); live.addCourse("Original", "ORG");
        var bad = db("bad.db"); bad.addCourse("Valid", "BAD");
        try (var c = bad.getConnection(); var statement = c.createStatement()) {
            statement.executeUpdate("UPDATE courses SET name='' WHERE code='BAD'");
        }
        assertThrows(Exception.class, () -> new DataTransfer(live).restore(directory.resolve("bad.db")));
        assertEquals(1, live.getAllCourses().size());
        assertEquals("ORG", live.getAllCourses().get(0).getCode());
    }
}
