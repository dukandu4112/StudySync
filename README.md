# StudySync

StudySync is a Java desktop student-productivity application for courses,
assignments, study sessions, deadlines, study planning, and academic progress.

## Download and open

Get the Windows ZIP or Linux TAR.GZ from
[GitHub Releases](https://github.com/dukandu4112/StudySync/releases/latest).
Extract the entire archive. Windows: open `StudySync/StudySync.exe`.
Linux: run `StudySync/bin/StudySync` from the extracted directory.
The desktop packages include Java; a separate JDK is not required.

Release assets include `SHA256SUMS.txt` for download verification. Keep all files
in the extracted application folder together.

## Features

- Course creation, editing, searching, and confirmed cascading deletion.
- Assignment deadlines, priorities, editing, completion/reopening, filtering,
  workload ranking, and a monthly calendar with a daily agenda.
- Study-session history, weekly progress/trends, streaks, daily/course goals,
  recommendations, and suggested study blocks.
- Assessment grades with relative weights, course charts, and text alternatives.
- Unified search, in-app deadline reminders, and persistent preferences.
- Paged histories, keyboard navigation, local SQLite storage, CSV reports,
  backup/import, pre-restore recovery snapshots, and compatible legacy upgrades.

## Keyboard

Ctrl+1 through Ctrl+9 open pages in navigation order. In Data & Settings,
Alt+B opens backup, Alt+R opens restore, and Alt+E opens export. Tab moves
between controls; Escape cancels dialogs.

## Data and upgrades

The desktop launcher reuses `studysync.db` in the working directory when one
exists; otherwise it stores data at `~/.studysync/studysync.db`.
Back up existing data before changing launch methods or upgrading.
Recovery snapshots are stored at `~/.studysync/recovery-backups`.
Compatible older databases upgrade transactionally; unsupported newer schemas
are rejected without alteration. CSV exports are reports; database backups are
the import/restore format. Reminder updates occur when their page is opened.

See [desktop guide](docs/DESKTOP-V3.md) and
[release validation](docs/RELEASE-VALIDATION.md) for detail and test limitations.

## Build from source

Requires JDK 17+ and Maven 3.6+:

```sh
mvn -Pdesktop-qa clean verify
mvn javafx:run
```

Executable desktop JAR and original CLI:

```sh
java -jar target/studysync-3.0.0-all.jar
java -cp target/studysync-3.0.0-all.jar com.studysync.Main
```

Build native packages on the target OS with `scripts/package-desktop.ps1`
(Windows) or `scripts/package-desktop.sh` (Linux). Packages are OS-specific.

## Project status

The v3.0.0 release implements the established desktop scope. Development is
not capped at this version: new requirements and verified issues drive future
releases. See [development goals](docs/DEVELOPMENT-GOALS.md).

## Author and license

Daniel Ukandu (`dukandu4112`). MIT License; see [LICENSE](LICENSE).
