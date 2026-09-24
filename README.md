# StudySync

StudySync is a Java 17 student productivity desktop application for organizing courses, assignments, study sessions, deadlines, workload, and academic progress. StudySync 2.2 builds on the JavaFX desktop foundation with weekly study-progress analytics, ranked assignment planning, study streak tracking, and week-over-week study trends while preserving the reusable service, domain, SQLite persistence, automated tests, and original CLI architecture.

**Release candidate: 2.2.0**  
**Latest stable release: 2.1.0**

## Features

- JavaFX desktop interface with Dashboard, Courses, Assignments, Study Sessions, and Workload navigation
- Create, edit, delete, search, and organize academic courses
- Safely cascade course deletion to associated assignments and study sessions
- Create, edit, complete, reopen, delete, search, and filter assignments
- Track due dates, descriptions, priorities, completion status, and associated course codes
- Rank pending assignments by urgency, priority, deadline, and assignment ID
- Classify pending work as Overdue, Due Today, Due Soon, or Upcoming
- Preview the five highest-ranked assignments directly on the dashboard with time-left/time-late context
- Filter assignments by status and priority
- Plan upcoming workload using 3, 7, 14, or 30-day windows
- Review overdue assignments separately in the workload planner
- Record, edit, delete, search, filter, and review study sessions
- Filter study-session history by course and search study notes
- Track total study time
- Review weekly study progress from Monday through Sunday, including minutes, session count, active study days, longest session, and daily average
- Track current and longest study streaks with last-study-day and continue/restart guidance
- Compare this week's study time with the previous week using minute change, percentage change, and up/down/steady trend direction
- View academic progress metrics for courses, assignments, overdue work, completion, and study time
- Review planning insights for upcoming work, high-priority pending work, the nearest deadline, and the most-studied course
- Persist application data locally with SQLite
- Validate dates, times, durations, and form input with clear UI feedback
- Keep edit dialogs open when validation fails so input can be corrected immediately
- Preserve the original command-line interface

## Technology

- Java 17
- JavaFX 17
- Maven
- SQLite / sqlite-jdbc
- JUnit 5
- GitHub Actions

## Architecture

StudySync separates the user interface from reusable application logic:

```text
src/
├── main/java/com/studysync/
│   ├── StudySyncApplication.java        # JavaFX desktop entry point
│   ├── DashboardView.java               # 2.2 dashboard composition
│   ├── StudyProgressView.java
│   ├── StudyStreakView.java
│   ├── StudyTrendView.java
│   ├── AssignmentPlanView.java
│   ├── UiSupport.java
│   ├── Main.java                        # CLI entry point
│   ├── StudySyncCli.java
│   ├── StudySyncService.java
│   ├── DatabaseManager.java
│   ├── Course.java
│   ├── Assignment.java
│   ├── AssignmentPlanItem.java
│   ├── StudySession.java
│   ├── StudyStreak.java
│   ├── StudyTrend.java
│   ├── DashboardSummary.java
│   ├── DashboardAnalytics.java
│   └── StudyProgressAnalytics.java
├── main/resources/com/studysync/
│   └── studysync.css
└── test/java/com/studysync/
    └── unit, integration, service, lifecycle, analytics, planning, CLI, and UI-support tests
```

The JavaFX and CLI interfaces share the same service and persistence layers, keeping business logic out of the presentation layer and avoiding duplicated data-access code. See `docs/ARCHITECTURE.md` for the architecture guide.

## Build and Verify

Requirements: JDK 17+ and Maven.

```bash
mvn clean verify
```

GitHub Actions runs `mvn --batch-mode verify` for pushes to `main` and `develop-2.2`, and for configured pull requests.

## Run the JavaFX Desktop Application

```bash
mvn javafx:run
```

StudySync creates `studysync.db` in the working directory and uses it for persistent local application data.

## Run the Original CLI

The original CLI remains available as an alternate interface. For the 2.2 release candidate, the packaged all-dependencies JAR uses the finalized version:

```bash
mvn clean package
java -jar target/studysync-2.2.0-all.jar
```

## Testing

StudySync uses automated tests across the domain, persistence, service, lifecycle, analytics, planning, CLI, and UI-support layers. Weekly progress calculations, assignment urgency/ranking behavior, study streak calculations, and weekly trend comparisons have dedicated automated coverage. JavaFX parsing and filtering logic remains extracted into pure helper code where appropriate so it can be verified in CI without requiring a graphical desktop session.

## Version Status

StudySync 2.2.0 is currently a release candidate on `develop-2.2`. The weekly study-progress, assignment-planning, study-streak, and weekly study-trend milestones are implemented and integrated into the enhanced desktop dashboard. The release-candidate commit must pass CI before promotion to `main`.

StudySync 2.1.0 remains the latest stable version on `main` and has an official GitHub Release tagged `v2.1.0`.

## License

This project is licensed under the MIT License.
