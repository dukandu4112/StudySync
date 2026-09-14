# StudySync

StudySync is a Java 17 student productivity desktop application for organizing courses, assignments, study sessions, deadlines, workload, and academic progress. StudySync 2.0 adds a JavaFX graphical interface while preserving the reusable service, domain, SQLite persistence, automated tests, and original CLI architecture.

**Current development version: 2.0.0-SNAPSHOT**

## Features

- JavaFX desktop interface with Dashboard, Courses, Assignments, Study Sessions, and Workload navigation
- Create and view academic courses
- Create, complete, reopen, delete, search, and filter assignments
- Track due dates, descriptions, priorities, and completion status
- Filter assignments by status and priority
- Plan upcoming workload using 3, 7, 14, or 30-day windows
- Record and review study sessions
- Track total study time
- View academic progress metrics for courses, assignments, overdue work, completion, and study time
- Persist application data locally with SQLite
- Validate dates, times, durations, and form input with clear UI feedback
- Preserve the original command-line interface for the StudySync 1.x workflow

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
│   └── DashboardSummary.java
├── main/resources/com/studysync/
│   └── studysync.css               # JavaFX styling
└── test/java/com/studysync/
    └── unit, integration, service, CLI, and UI-support tests
```

The JavaFX and CLI interfaces share the same service and persistence layers, keeping business logic out of the presentation layer and avoiding duplicated data-access code.

## Build and Verify

Requirements: JDK 17+ and Maven.

Run the complete Maven verification lifecycle:

```bash
mvn clean verify
```

GitHub Actions runs `mvn --batch-mode verify` for pushes and pull requests targeting `main`.

## Run the JavaFX Desktop Application

During StudySync 2.0 development, launch the desktop interface with the JavaFX Maven plugin:

```bash
mvn javafx:run
```

StudySync creates `studysync.db` in the working directory and uses it for persistent local application data.

## Run the Original CLI

The existing CLI remains available while the 2.0 desktop application is finalized. The Maven package configuration continues to support the CLI all-dependencies JAR during the 2.0 snapshot phase.

```bash
mvn clean package
java -jar target/studysync-2.0.0-SNAPSHOT-all.jar
```

## Testing

StudySync uses automated tests across the domain, persistence, service, CLI, and UI-support layers. JavaFX parsing and filtering logic is extracted into pure helper code so it can be verified in CI without requiring a graphical desktop session.

## Version Status

StudySync 1.0.0 established the core productivity platform and CLI. StudySync 2.0.0 is currently being prepared as the desktop release, adding JavaFX navigation, productivity screens, workload planning, stronger validation, and UI-focused test coverage.

No GitHub 2.0 release or tag is implied by the snapshot version until release preparation is complete.

## License

This project is licensed under the MIT License.
