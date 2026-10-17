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

## Remaining work to assess

- Native Windows Save/Open workflows are exercised in the desktop packaging job;
  cancellation, backup, export, restore, and repeat launch must pass.
- Desktop lifecycle tests now cover course, assignment, and study-session editing,
  cancellations, validation corrections, and deletion. Continue adding coverage
  for larger and more varied real-world workflows.
- Automated checks cover shortcuts, Tab traversal, accessible form labels, and
  minimum-window navigation/scrolling. Screen-reader evaluation and realistic
  larger datasets remain areas for further review.
- Automated checks cover legacy upgrades, failed upgrade rollback, repeated
  reopening, recovery snapshots, and rejection of newer schemas. Long-running
  real-world use remains to be evaluated.
- Further feature requirements from the original project goals and user feedback.

Do not describe these as completed simply because a build passes or a version
number has been reached. Add concrete requirements and evidence as work proceeds.
