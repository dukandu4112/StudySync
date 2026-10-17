package com.studysync;

import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RepeatedUseTest {
    @TempDir Path folder;
    @Test @Timeout(120) void largeHistorySurvivesRepeatedEditBackupRestoreAndReopen() throws Exception {
        long started = System.nanoTime();
        Path file = folder.resolve("history.db"); String url = "jdbc:sqlite:" + file;
        DatabaseManager database = new DatabaseManager(url);
        try (Connection c = database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement courses = c.prepareStatement("INSERT INTO courses(id,name,code) VALUES(?,?,?)");
                 PreparedStatement assignments = c.prepareStatement("INSERT INTO assignments(course_id,title,description,due_date,priority,completed) VALUES(?,?,?,?,?,?)");
                 PreparedStatement sessions = c.prepareStatement("INSERT INTO study_sessions(course_id,start_time,duration_minutes,notes) VALUES(?,?,?,?)");
                 PreparedStatement grades = c.prepareStatement("INSERT INTO academic_grades(course_id,title,earned,possible,weight) VALUES(?,?,?,?,?)")) {
                for (int i = 1; i <= 60; i++) {
                    courses.setInt(1,i); courses.setString(2,"Historical course " + i); courses.setString(3,String.format("COURSE%03d",i)); courses.addBatch();
                }
                courses.executeBatch();
                for (int i = 0; i < 3000; i++) {
                    assignments.setInt(1,i % 60 + 1); assignments.setString(2,"Assignment " + i); assignments.setString(3,"History");
                    assignments.setString(4,LocalDateTime.of(2021,1,1,12,0).plusDays(i).toString());
                    assignments.setString(5,i % 2 == 0 ? "HIGH" : "MEDIUM"); assignments.setInt(6,i % 3 == 0 ? 0 : 1); assignments.addBatch();
                }
                assignments.executeBatch();
                for (int i = 0; i < 6000; i++) {
                    sessions.setInt(1,i % 60 + 1); sessions.setString(2,LocalDateTime.of(2021,1,1,10,0).plusHours(i * 4L).toString());
                    sessions.setInt(3,30); sessions.setString(4,"Study history " + i); sessions.addBatch();
                }
                sessions.executeBatch();
                for (int i = 0; i < 300; i++) {
                    grades.setInt(1,i % 60 + 1); grades.setString(2,"Assessment " + i); grades.setDouble(3,80); grades.setDouble(4,100); grades.setDouble(5,1); grades.addBatch();
                }
                grades.executeBatch(); c.commit();
            }
        }
        var service = new StudySyncService(database); service.setDailyStudyTargetMinutes(45);
        for (int cycle = 0; cycle < 100; cycle++) {
            var temporary = service.createCourse("Cycle " + cycle,"TEMP" + cycle);
            service.createAssignment(temporary.getId(),"Temporary", "", LocalDateTime.now(), Assignment.Priority.HIGH);
            service.recordStudySession(temporary.getId(),LocalDateTime.now(),15,"Temporary");
            service.saveAcademicGrade(new AcademicGrade(0,temporary.getId(),"Temporary",9,10,1));
            service.deleteCourse(temporary.getId());
            var assignment = database.findAssignmentById(1);
            service.updateAssignment(1,assignment.getCourseId(),"Edited " + cycle,assignment.getDescription(),assignment.getDueDate(),assignment.getPriority());
            if (cycle % 20 == 0) {
                Path snapshot = folder.resolve("cycle-" + cycle + ".db");
                new DataTransfer(database).backup(snapshot); new DataTransfer(database).restore(snapshot);
                database = new DatabaseManager(url); service = new StudySyncService(database);
                assertEquals(60, service.getStudyRecommendations(LocalDateTime.of(2026,10,17,10,0)).size());
                assertTrue(service.getStudyPlanSummary(LocalDateTime.of(2026,10,17,10,0)).plannedMinutes() <= 45);
                assertEquals(3000, service.getDashboardSummary().totalAssignments());
            }
        }
        assertEquals(60, service.getCourses().size()); assertEquals(3000, service.getAssignments().size());
        assertEquals(6000, service.getStudySessions().size()); assertEquals(300, service.getAcademicGrades().size());
        assertEquals(180000, service.getTotalStudyMinutes()); assertEquals("Edited 99", database.findAssignmentById(1).getTitle());
        try (var c = database.getConnection(); var s = c.createStatement()) {
            try (var r = s.executeQuery("PRAGMA integrity_check")) { assertTrue(r.next()); assertEquals("ok",r.getString(1)); }
            try (var r = s.executeQuery("PRAGMA foreign_key_check")) { assertFalse(r.next()); }
        }
        System.out.printf("REPEATED_USE: 60 courses, 3000 assignments, 6000 sessions, 300 grades; 100 cycles; %.2f seconds%n",(System.nanoTime()-started)/1e9);
    }
}
