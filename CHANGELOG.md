# Changelog

All notable changes to StudySync are documented in this file.

## [2.2.0] - Unreleased

### Added

- Weekly study-progress analytics for a Monday-through-Sunday week
- Weekly metrics for total study minutes, session count, active study days, longest session, and average minutes per active study day
- JavaFX weekly study-progress dashboard presentation with consistency progress and active-day messaging
- Assignment urgency planning model for pending assignments
- Assignment classifications for Overdue, Due Today, Due Soon, and Upcoming work
- Deterministic assignment-plan ranking by urgency, priority, deadline, and assignment ID
- JavaFX assignment-plan dashboard preview showing the five highest-ranked assignments
- Human-readable assignment countdown context such as minutes/hours/days left or late
- StudySync 2.2 dashboard composition combining existing metrics, weekly progress, assignment planning, and planning insights
- Automated coverage for weekly progress analytics and assignment urgency/ranking behavior

### Changed

- Maven development version advanced to `2.2.0-SNAPSHOT`
- GitHub Actions development-branch verification moved from `develop-2.1` to `develop-2.2`
- `mvn javafx:run` launches the enhanced StudySync 2.2 desktop entry point
- Dashboard now surfaces weekly study consistency and ranked pending-work priorities alongside the existing academic metrics and planning insights
- README updated for the active 2.2 development milestone and current launch/build instructions

### Development Status

StudySync 2.2.0 remains unreleased on `develop-2.2`. The weekly study-progress and assignment-planning milestones are implemented, integrated into the desktop dashboard, and CI-verified. No 2.2 tag or GitHub Release is claimed by this entry.

## [2.1.0] - 2026-09-14

### Added

- Course editing and deletion in the JavaFX desktop interface
- Service-level course lifecycle validation with cascade-aware deletion behavior
- Study-session editing and deletion across the service and JavaFX layers
- Automated study-session lifecycle coverage
- Dashboard planning analytics for upcoming assignments, high-priority pending work, nearest pending deadline, and most-studied course
- JavaFX Planning Insights section backed by dashboard analytics
- Live course search by course code or name
- Study-session note search and course filtering
- Reusable `UiSupport` filtering and sorting helpers for courses and study sessions
- Automated coverage for course and study-session discovery behavior

### Changed

- Maven project version finalized as `2.1.0`
- Course lists are consistently sorted by course code in UI-support discovery logic
- Study-session discovery results are ordered newest first
- `StudySyncService` formatting was cleaned up for maintainability without changing its public workflows
- `StudySyncApplication` formatting and edit workflow structure were improved for maintainability
- Desktop documentation reflects the expanded 2.1 course, study-session, analytics, search, and filtering capabilities

### Fixed

- Course edit dialogs remain open when validation or service updates fail
- Assignment edit dialogs remain open when validation or service updates fail
- Study-session edit dialogs remain open when validation or service updates fail
- Invalid edit input can be corrected immediately without reopening the editor

### Release Status

StudySync 2.1.0 is the latest stable version on `main` and was published as the official GitHub Release `v2.1.0`.

## [2.0.0] - 2026-09-14

### Added

- JavaFX 17 desktop application foundation
- Main desktop navigation for Dashboard, Courses, Assignments, Study Sessions, and Workload
- JavaFX dashboard backed by real StudySync productivity metrics
- Course creation and persistent course listing in the desktop UI
- Assignment creation, editing, completion, reopening, deletion, search, status filtering, and priority filtering in the desktop UI
- Assignment course-code context in desktop assignment and workload views
- Study-session recording and history in the desktop UI
- Configurable 3, 7, 14, and 30-day workload planning interface
- Dedicated overdue-assignment section in the workload planner
- JavaFX CSS styling for navigation, cards, forms, assignment controls, study sessions, and workload planning
- UI validation and clearer desktop error feedback
- Testable `UiSupport` helpers for date/time parsing, duration validation, and assignment filtering
- JUnit coverage for UI-support behavior without requiring a graphical CI session
- Architecture documentation for the desktop, CLI, service, domain, persistence, and testing layers

### Changed

- StudySync now supports both the original CLI and the JavaFX desktop interface on the shared service and SQLite persistence layers
- Production JavaFX parsing, duration validation, and assignment filtering use the tested `UiSupport` helpers
- GitHub Actions runs the complete `mvn --batch-mode verify` lifecycle instead of stopping at `mvn test`
- Maven project version finalized as `2.0.0`
- Documentation updated for the finalized JavaFX desktop milestone, testing, architecture, and launch instructions

### Fixed

- JavaFX navigation initializes with only the Dashboard navigation item active
- Study-session totals refresh after a new session is recorded
- Study-session course lookups avoid repeatedly loading the course collection for every row
- UI-support tests use the public assignment completion API
- CLI assignment workflows include reopening completed assignments
- Local Maven output, SQLite data files, and common IDE metadata are excluded from version control

### Release Status

StudySync 2.0.0 is the finalized repository version for this milestone. This version history does not claim that a corresponding GitHub tag or GitHub Release has been published.

## [1.0.0] - 2026-09-14

### Added

- Java 17 and Maven project foundation
- SQLite persistence with foreign-key enforcement
- Course creation and retrieval support
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
