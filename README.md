# StudySync

StudySync is a Java 17 student productivity desktop application for organizing courses, assignments, study sessions, deadlines, workload, and academic progress. StudySync 2.1 builds on the JavaFX desktop foundation with fuller lifecycle management, stronger planning analytics, improved discovery controls, and more resilient edit workflows while preserving the reusable service, domain, SQLite persistence, automated tests, and original CLI architecture.

**Current development version: 2.1.0-SNAPSHOT**

## Features

- JavaFX desktop interface with Dashboard, Courses, Assignments, Study Sessions, and Workload navigation
- Create, edit, delete, search, and organize academic courses
- Safely cascade course deletion to associated assignments and study sessions
- Create, edit, complete, reopen, delete, search, and filter assignments
- Track due dates, descriptions, priorities, completion status, and associated course codes
- Filter assignments by status and priority
- Plan upcoming workload using 3, 7, 14, or 30-day windows
- Review overdue assignments separately in the workload planner
- Record, edit, delete, search, filter, and review study sessions
- Filter study-session history by course and search study notes
- Track total study time
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
│   ├── StudySyncApplication.java   # JavaFX desktop UI
│   ├── UiSupport.java              # testable UI parsing/filtering helpers
│   ├── Main.java                   # CLI entry point
│   ├── StudySyncCli.java           # original command-line UI
│   ├── StudySyncService.java       # application workflows
│   ├── DatabaseManager.java        # SQLite persistence
│   ├── Course.java
│   ├── Assignment.java
│   ├── StudySession.java
│   ├── DashboardSummary.java
│   └── DashboardAnalytics.java
├── main/resources/com/studysync/
│   └── studysync.css               # JavaFX styling
└── test/java/com/studysync/
    └── unit, integration, service, lifecycle, CLI, and UI-support tests
```

The JavaFX and CLI interfaces share the same service and persistence layers, keeping business logic out of the presentation layer and avoiding duplicated data-access code. See `docs/ARCHITECTURE.md` for the architecture guide.

## Build and Verify

Requirements: JDK 17+ and Maven.

Run the complete Maven verification lifecycle:

```bash
mvn clean verify
```

GitHub Actions runs `mvn --batch-mode verify` for pushes to `main` and `develop-2.1`, and for configured pull requests.

## Run the JavaFX Desktop Application

Launch the primary StudySync desktop interface with:

```bash
mvn javafx:run
```

StudySync creates `studysync.db` in the working directory and uses it for persistent local application data.

## Run the Original CLI

The original CLI remains available as an alternate interface. During 2.1 development, the packaged all-dependencies JAR uses the snapshot version in its filename:

```bash
mvn clean package
java -jar target/studysync-2.1.0-SNAPSHOT-all.jar
```

## Testing

StudySync uses automated tests across the domain, persistence, service, lifecycle, CLI, and UI-support layers. JavaFX parsing and filtering logic is extracted into pure helper code so it can be verified in CI without requiring a graphical desktop session. Course and study-session discovery behavior is covered through these testable helpers, while service tests cover lifecycle and analytics behavior.

## Version Status

StudySync 2.1.0 is currently being finalized on the `develop-2.1` branch. The Maven version remains `2.1.0-SNAPSHOT` until final release-readiness documentation and verification are complete.

The stable `main` branch remains on StudySync 2.0.0 during this development cycle. A repository version does not by itself imply that a corresponding GitHub tag or GitHub Release has been published.

## License

This project is licensed under the MIT License.
