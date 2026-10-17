# StudySync 3.0.0 release validation

## Scope review

The original productivity goals are represented by Courses, Assignments,
Study Sessions, Workload, and Dashboard. The subsequent desktop roadmap adds
Calendar, Grades, Search & Alerts, and Data & Settings. Smart planning, goals,
recommendations, SQLite persistence, and the original CLI are retained.

## Automated evidence

| Area | Checks |
|---|---|
| Core data | Models, validation, SQLite CRUD, search, filtering, cascading deletes |
| Desktop workflows | Create/edit/cancel/delete; invalid-input correction; completion/reopening; typed dates |
| Planning | Goals, recommendations, study blocks, workload ranking, progress/trend/streak metrics |
| Recovery | Native Windows Save/Open dialogs, cancellation, backup, export, restore, recovery snapshot, repeat launch |
| Upgrades | Legacy record retention, source-backup immutability, rollback on failed schema changes, newer-schema rejection |
| Larger history | 60 courses, 3,000 assignments, 6,000 sessions, 300 grades; 100 mutation cycles and repeated backup/restore/reopen; integrity and foreign-key checks |
| Long lists | 50-row paging with search reset and access to later pages; chart text alternatives |
| Keyboard/layout | Ctrl+1..9, Tab traversal, labels, minimum-window navigation and scrolling |
| Native accessibility | Windows UI Automation names, roles, and keyboard-focus eligibility for data controls |

The larger-history mutation test simulates repeated operations; it does not
represent months of elapsed human usage. Timing logs are observations from the
test machines, not universal performance guarantees.

Run `mvn -Pdesktop-qa clean verify`. Native Windows checks are in
`scripts/test-windows-dialogs.ps1` and `scripts/test-accessibility.ps1`.
GitHub's Desktop packages workflow builds Windows/Linux bundles and publishes
stable assets only from main after all package jobs pass.

## Known limitations

- No claim of human screen-reader validation or accessibility certification.
- No claim of months-long real-world testing or absence of every possible bug.
- Reminders are in-app, refreshed when opened, not background OS notifications.
- CSV is an export report; backup databases are used for restore/import.
- Native packages are platform-specific; macOS packaging is not verified.

These limits remain explicit while future issues and feature requests are
tracked. They do not remove implemented functionality from the established
release scope.
