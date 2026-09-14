# StudySync Architecture

StudySync is organized as a layered Java application so the desktop and command-line interfaces can reuse the same productivity workflows and persistence code.

## Layers

### Presentation

`StudySyncApplication` is the JavaFX desktop entry point. It provides Dashboard, Courses, Assignments, Study Sessions, and Workload screens. `StudySyncCli` remains available as the original command-line interface.

Presentation code delegates application operations to `StudySyncService` rather than accessing SQLite directly.

`UiSupport` contains deterministic parsing, validation, and filtering helpers that can be tested without starting a graphical JavaFX session.

### Application Service

`StudySyncService` coordinates the application's productivity workflows. It handles course and assignment operations, assignment status transitions, search and filtering, upcoming workload queries, study-session recording, study-time totals, and dashboard calculations.

Both user interfaces depend on this layer, which keeps workflow behavior consistent between the CLI and JavaFX application.

### Domain

The core domain types are:

- `Course` — course identity, name, and code
- `Assignment` — course association, title, description, due date, priority, and completion state
- `StudySession` — course association, start time, duration, and notes
- `DashboardSummary` — immutable aggregate productivity metrics

Domain objects validate their own required state where appropriate.

### Persistence

`DatabaseManager` owns SQLite persistence and database initialization. It creates and accesses the course, assignment, and study-session tables, enables foreign-key enforcement, and uses prepared statements for database operations.

The default application database is `studysync.db`. Tests can supply a different database URL so integration tests do not depend on the user's local application database.

## Dependency Direction

```text
JavaFX UI ─────┐
               ├──> StudySyncService ───> DatabaseManager ───> SQLite
CLI ───────────┘          │
                          └──> Domain Models

UiSupport ───────────────> Domain Models
```

The presentation layers do not own persistence logic. This allows StudySync to add or replace interfaces without rewriting its core application workflows.

## Testing Strategy

StudySync uses JUnit 5 across several levels:

- domain-model unit tests
- SQLite integration tests
- service-layer workflow tests
- CLI behavior tests
- pure UI-support tests for JavaFX-related parsing, validation, and filtering logic

JavaFX-independent helper tests are intentionally used for behavior that does not require rendering a window. This keeps the CI environment reliable while still testing UI-facing logic.

## Build and CI

The project targets Java 17 and is built with Maven. GitHub Actions runs:

```bash
mvn --batch-mode verify
```

This executes the automated tests and Maven verification/package lifecycle on pushes and pull requests targeting `main`.

The JavaFX desktop application can be launched during development with:

```bash
mvn javafx:run
```

## Version 2 Direction

StudySync 2.0 keeps the proven v1 service, domain, persistence, and test foundations while making JavaFX the primary desktop experience. The CLI remains useful for compatibility and for demonstrating that the core architecture is interface-independent.
