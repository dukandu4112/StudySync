# StudySync development goals

StudySync is a desktop student-productivity application. Continue development
until the intended workflows are complete, reliable, and usable. v3.0 is a
milestone, not a stopping point; future versions follow remaining requirements
and verified problems.

## Current scope

- Manage courses, assignments, due dates, priorities, completion, and study history.
- Turn goals, workload, and recommendations into useful study planning.
- Track assessment scores and understand course progress.
- Find work quickly and notice pending deadlines.
- Keep local data persistent, exportable, and recoverable.
- Provide a usable desktop application with windows and buttons.

## Evidence required

- Automated service and database checks, including failed restore rollback.
- Desktop interaction checks that verify saved results, not only process startup.
- Native packages and startup checks on supported operating systems.
- Coverage of migration from existing project databases and repeated use.
- Explicit reporting of untested flows and remaining limitations.

## Completion evidence

The current functional scope is implemented. Release validation covers 243 tests,
including 28 desktop interaction tests, legacy database migrations, recovery,
and 100 repeated-use cycles with thousands of saved records. Native Windows
Save/Open, cancellation, restore, export, accessibility metadata, and repeat
launch are release gates. See RELEASE-VALIDATION.md for the evidence and limits.

## Continuing evaluation

Human screen-reader evaluation and long-term use on students' own computers
remain ongoing evaluation. Automated accessibility metadata and synthetic
repeated-use tests do not substitute for those experiences. Future versions
should address concrete user feedback, verified defects, and new requirements;
the version number does not end development.
