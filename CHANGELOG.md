# Changelog

All notable changes to StudySync are documented in this file.

## [2.0.0] - Unreleased

### Added

- JavaFX 17 desktop application foundation
- Main desktop navigation for Dashboard, Courses, Assignments, Study Sessions, and Workload
- JavaFX dashboard backed by real StudySync productivity metrics
- Course creation and persistent course listing in the desktop UI
- Assignment creation, completion, reopening, deletion, search, status filtering, and priority filtering in the desktop UI
- Study-session recording and history in the desktop UI
- Configurable 3, 7, 14, and 30-day workload planning interface
- JavaFX CSS styling for navigation, cards, forms, assignment controls, study sessions, and workload planning
- UI validation and clearer desktop error feedback
- Testable `UiSupport` helpers for date/time parsing, duration validation, and assignment filtering
- JUnit coverage for UI-support behavior without requiring a graphical CI session

### Changed

- StudySync now supports both the original CLI and the JavaFX desktop interface on the shared service and SQLite persistence layers
- GitHub Actions now runs the complete `mvn --batch-mode verify` lifecycle instead of stopping at `mvn test`
- Maven project version advanced to `2.0.0-SNAPSHOT` during desktop development
- Documentation updated for JavaFX development, testing, and launch instructions

### Fixed

- JavaFX navigation now initializes with only the Dashboard navigation item active
- Study-session totals refresh after a new session is recorded
- Study-session course lookups avoid repeatedly loading the course collection for every row
- UI-support tests use the public assignment completion API

### Release Status

StudySync 2.0.0 is still in release preparation. This changelog does not indicate that a GitHub tag or release has been published.

## [1.0.0] - 2026-09-14

### Added

- Java 17 and Maven project foundation
- SQLite persistence with foreign-key enforcement
- Course creation, retrieval, update, and deletion support
- Assignment creation, editing, completion, reopening, deletion, search, and filtering
- Assignment priority and due-date tracking
- Pending, completed, overdue, and upcoming assignment views
- Configurable upcoming-workload planning windows
- Study-session recording and history
- Total study-time tracking
- Academic progress dashboard and completion percentage
- Interactive command-line interface with input validation and error recovery
- Unit, integration, service, and CLI automated tests
- GitHub Actions continuous integration
- Executable all-dependencies JAR packaging
- Portfolio-ready project documentation

### Architecture

- Domain models for courses, assignments, and study sessions
- Application service layer for productivity workflows
- SQLite database manager for persistence
- Separate CLI layer for user interaction
- Reusable architecture intended to support future graphical interfaces

### Version History

StudySync 1.0.0 established the first stable version of the core student productivity application. Repository documentation distinguishes version history from whether a corresponding GitHub release/tag has been published.
