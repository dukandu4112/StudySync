StudySync 3.0.0 delivers the desktop student-productivity scope with courses,
assignments, calendar planning, study history and goals, smart study plans,
weighted assessment grades/charts, unified search, in-app reminders, preferences,
CSV reports, database backup/import, and transactional legacy upgrades.

Download the Windows ZIP or Linux TAR.GZ and extract the entire archive.
Windows: open `StudySync/StudySync.exe`. Linux: run `StudySync/bin/StudySync`.
A Java runtime is included. Verify downloads against SHA256SUMS.txt.

Validation includes automated domain/database tests, desktop interactions,
legacy upgrade/rollback, repeated use with a larger history, and real Windows
Save/Open dialogs with restore and repeat launch. Windows accessibility metadata
is checked separately from speech-reader testing.

Back up existing data before upgrading. The launcher reuses a working-directory
studysync.db when present, otherwise ~/.studysync/studysync.db. Recovery snapshots
are saved under ~/.studysync/recovery-backups. Do not run multiple instances
against the same database during restore.

Known limits: reminders are in-app and refresh when opened; CSV is a report,
not the restore format; Windows and Linux packages are OS-specific. Human
screen-reader evaluation and months of real-world use are not claimed by the
automated checks. See docs/RELEASE-VALIDATION.md for evidence and limitations.

This release is a milestone, not the endpoint for StudySync development.
