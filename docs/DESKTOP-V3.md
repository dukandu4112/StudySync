# StudySync desktop 3.0 release candidate

Continues `develop-2.3`; retains its smart study plans, daily and weekly goals,
recommendations, dashboard, course/assignment editing, study history, and workload.

## Milestones

- 2.3: existing smart planning and goals retained; upstream CI passed.
- 2.4: month calendar, Today/previous/next navigation, pending deadline counts,
  daily agenda, complete/reopen assignments directly from the calendar.
- 2.5: persistent course assessments, edit/delete, relative weighted percentages,
  and course progress charts. Empty courses have no recorded grade.
- 2.6: cross-feature search, type filter, in-app overdue/48-hour reminders,
  and saved reminder preferences. Existing assignment priority/status filters remain.
- 2.7: SQLite backup/import, automatic pre-restore recovery backup, transactional
  restore, CSV report export, daily target settings. CSV is a report, not a
  round-trip import format; import uses a database snapshot from this version.
- 2.8: visible keyboard focus, descriptive controls, empty states, scrollable
  content, and a desktop-first executable entry point.
- 2.9: schema migration without replacing old tables, query indexes, finite grade
  validation, CSV formula protection, and backup/restore regression tests.
- 3.0: executable desktop JAR and native app-image packaging scripts, release
  candidate documentation. Final release awaits platform acceptance.

## Run

Java 17 and Maven are required for source builds:

```sh
mvn clean verify
mvn javafx:run
```

The executable JAR opens the desktop interface:

```sh
java -jar target/studysync-3.0.0-rc.2-all.jar
```

The original CLI remains available:

```sh
java -cp target/studysync-3.0.0-rc.2-all.jar com.studysync.Main
```

The plain Java desktop launcher reuses `studysync.db` in the working directory
when present. Otherwise it creates `~/.studysync/studysync.db`. Source
`javafx:run` and the CLI preserve their previous working-directory database.
For a shared explicit location use `-Dstudysync.database=jdbc:sqlite:/path/file.db`.
Back up existing data before switching launch methods. Do not run multiple
instances during a restore.

## Native application

Build on the target OS with a JDK that includes `jpackage`:

- Windows: `powershell -ExecutionPolicy Bypass -File scripts/package-desktop.ps1`
- Linux/macOS: `bash scripts/package-desktop.sh`

Output: `target/desktop/StudySync` (or the OS's corresponding app bundle).
An app-image includes Java and opens via its launcher; no separate JDK is
required on the recipient's computer. JavaFX native libraries are OS-specific,
so build Windows packages on Windows. These scripts create an app image,
not an installer. Remove or rename an earlier `target/desktop` before rebuilding.

## Automated desktop interaction checks

```sh
mvn -Pdesktop-qa clean verify
```

This opt-in profile uses a test-only headless JavaFX backend. It clicks real
controls and checks the SQLite results. It covers grade create/edit/validation,
delete/cancel confirmation, clear-edit behavior, calendar complete/reopen and
month/leap-date navigation, preference persistence, and search/clear. CI and
native packaging run this profile automatically. The native file chooser itself
is not automated; backup/restore is covered at the data layer.

Recovery snapshots are saved in `~/.studysync/recovery-backups`.

Development continues beyond v3.0 whenever the intended goals remain unmet;
version numbers do not define project completion. See
[DEVELOPMENT-GOALS.md](DEVELOPMENT-GOALS.md).

## Acceptance before final release

- Open all pages at the minimum supported window size and using keyboard focus.
- Create/edit/delete a course, assignment, study session, and grade.
- Test month changes, leap dates, calendar completion, search, and chart refresh.
- Change goals and reminder preferences, restart, and verify persistence.
- Back up, change data, restore, and verify recovery snapshot and all records.
- Export quoted/newline-containing input and inspect the CSV in a spreadsheet.
- Build and launch the native Windows app image on the intended Windows PC.

The release candidate must not be described as a verified Windows release
until those Windows checks have been completed.
