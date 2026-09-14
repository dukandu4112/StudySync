# Changelog

All notable changes to StudySync are documented in this file.

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

### Release

StudySync v1.0.0 is the first stable portfolio release of the core student productivity application.
