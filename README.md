# StudySync

StudySync is a Java 17 student productivity application for organizing courses, assignments, study sessions, deadlines, workload, and academic progress. It uses SQLite for persistent local storage and provides an interactive command-line interface backed by automated JUnit tests and GitHub Actions CI.

**Current release: v1.0.0**

## Features

- Create and view academic courses
- Create, edit, complete, reopen, delete, search, and filter assignments
- Track due dates, descriptions, priorities, and completion status
- View pending, completed, overdue, and upcoming assignments
- Plan workload using configurable upcoming due-date windows
- Record and review study sessions
- Track total study time
- View an academic progress dashboard with completion metrics
- Persist application data locally with SQLite
- Validate user input and recover cleanly from application errors

## Technology

- Java 17
- Maven
- SQLite / sqlite-jdbc
- JUnit 5
- GitHub Actions

## Project Structure

```text
src/
├── main/java/com/studysync/
│   ├── Main.java
│   ├── StudySyncCli.java
│   ├── StudySyncService.java
│   ├── DatabaseManager.java
│   ├── Course.java
│   ├── Assignment.java
│   ├── StudySession.java
│   └── DashboardSummary.java
└── test/java/com/studysync/
    └── automated unit and integration tests
```

StudySync separates domain models, application/service logic, SQLite persistence, and the user-facing CLI so the project can evolve toward additional interfaces without duplicating core business logic.

## Build and Test

Requirements: JDK 17+ and Maven.

```bash
mvn clean test
```

To create the executable application package:

```bash
mvn clean package
```

The packaged all-dependencies JAR is created in `target/` with the `-all.jar` classifier.

## Run

After packaging, run StudySync v1.0.0 with:

```bash
java -jar target/studysync-1.0.0-all.jar
```

StudySync creates `studysync.db` in the working directory and uses it for persistent application data.

## Continuous Integration

GitHub Actions runs the Maven test suite automatically for pushes and pull requests targeting `main`. This helps catch regressions across the domain models, SQLite persistence layer, service layer, and interactive CLI.

## Roadmap

Version 1.0.0 establishes the core StudySync productivity platform. Future development can build on the same service and persistence layers with a graphical interface, richer analytics, notifications, scheduling enhancements, and additional productivity tools.

## License

This project is licensed under the MIT License.
