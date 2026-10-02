package com.studysync;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UiSupportTest {
    @Test void parseDateTimeCombinesDateAndTime(){assertEquals(LocalDateTime.of(2026,9,14,14,30),UiSupport.parseDateTime(LocalDate.of(2026,9,14),"14:30"));}
    @Test void parseDateTimeRejectsMissingOrInvalidInput(){assertThrows(IllegalArgumentException.class,()->UiSupport.parseDateTime(null,"14:30"));assertThrows(IllegalArgumentException.class,()->UiSupport.parseDateTime(LocalDate.now(),""));assertThrows(IllegalArgumentException.class,()->UiSupport.parseDateTime(LocalDate.now(),"2:30 PM"));}
    @Test void positiveMinutesAreParsedAndValidated(){assertEquals(45,UiSupport.parsePositiveMinutes(" 45 "));assertThrows(IllegalArgumentException.class,()->UiSupport.parsePositiveMinutes("0"));assertThrows(IllegalArgumentException.class,()->UiSupport.parsePositiveMinutes("-5"));assertThrows(IllegalArgumentException.class,()->UiSupport.parsePositiveMinutes("forty"));}
    @Test void courseFilteringSearchesCodeAndNameAndSortsByCode(){Course data=new Course(1,"Data Structures","CSCI 3300"),arch=new Course(2,"Computer Organization & Architecture","CSCI 3212"),linear=new Course(3,"Linear Algebra","MATH 2502");List<Course> courses=List.of(linear,data,arch);assertEquals(List.of(arch,data,linear),UiSupport.filterCourses(courses,""));assertEquals(List.of(data),UiSupport.filterCourses(courses,"3300"));assertEquals(List.of(linear),UiSupport.filterCourses(courses,"linear"));}
    @Test void assignmentFilteringSupportsSearchStatusAndPriority(){LocalDateTime now=LocalDateTime.now();Assignment high=new Assignment(1,"Architecture Review","Study pipeline",now.plusDays(2),Assignment.Priority.HIGH),low=new Assignment(1,"Old Homework","Matrix practice",now.minusDays(2),Assignment.Priority.LOW),done=new Assignment(1,"Finished Project","Java implementation",now.plusDays(3),Assignment.Priority.HIGH);done.markCompleted();List<Assignment> items=List.of(high,low,done);assertEquals(List.of(high),UiSupport.filterAssignments(items,"pipeline","All","All"));assertEquals(List.of(low),UiSupport.filterAssignments(items,"","Overdue","All"));assertEquals(List.of(high),UiSupport.filterAssignments(items,"","Pending","HIGH"));assertEquals(List.of(done),UiSupport.filterAssignments(items,"","Completed","HIGH"));}
    @Test void studySessionFilteringSupportsCourseNotesAndNewestFirst(){LocalDateTime now=LocalDateTime.now();StudySession older=new StudySession(1,now.minusDays(2),30,"Trees review"),newest=new StudySession(1,now.minusHours(1),45,"Graph practice"),other=new StudySession(2,now.minusMinutes(30),25,"Matrix practice");List<StudySession> sessions=List.of(older,other,newest);assertEquals(List.of(newest,older),UiSupport.filterStudySessions(sessions,1,""));assertEquals(List.of(newest),UiSupport.filterStudySessions(sessions,null,"graph"));assertEquals(List.of(other),UiSupport.filterStudySessions(sessions,2,"practice"));}

    @Test
    void courseAwareStudySessionFilteringSearchesCourseAndNotes() {
        LocalDateTime now=LocalDateTime.now();
        Course data=new Course(1,"Data Structures","CSCI 3300");
        Course mathCourse=new Course(2,"Linear Algebra","MATH 2502");
        StudySession older=new StudySession(1,now.minusHours(3),30,"Trees review");
        StudySession newest=new StudySession(1,now.minusHours(1),45,"Graph practice");
        StudySession math=new StudySession(2,now.minusMinutes(30),25,"Matrix practice");
        List<StudySession> sessions=List.of(older,math,newest); List<Course> courses=List.of(data,mathCourse);
        assertEquals(List.of(newest,older),UiSupport.filterStudySessions(sessions,courses,"","CSCI 3300"));
        assertEquals(List.of(newest,older),UiSupport.filterStudySessions(sessions,courses,"data","All Courses"));
        assertEquals(List.of(math),UiSupport.filterStudySessions(sessions,courses,"MATH 2502","All Courses"));
        assertEquals(List.of(newest),UiSupport.filterStudySessions(sessions,courses,"graph","All Courses"));
    }

    @Test void nullCollectionsAreRejected(){assertThrows(IllegalArgumentException.class,()->UiSupport.filterCourses(null,""));assertThrows(IllegalArgumentException.class,()->UiSupport.filterAssignments(null,"","All","All"));assertThrows(IllegalArgumentException.class,()->UiSupport.filterStudySessions(null,null,""));assertThrows(IllegalArgumentException.class,()->UiSupport.filterStudySessions(null,List.of(),"","All Courses"));assertThrows(IllegalArgumentException.class,()->UiSupport.filterStudySessions(List.of(),null,"","All Courses"));}
}
