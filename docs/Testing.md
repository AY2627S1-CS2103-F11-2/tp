---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Appointment smoke test

Run `./gradlew clean check coverage shadowJar` (Windows: `.\gradlew.bat clean check coverage shadowJar`).
This runs all JUnit tests, both Checkstyle tasks, coverage generation and the distributable JAR build.
Reports are in `build/reports/tests/test`, `build/reports/checkstyle` and `build/reports/jacoco/coverage`.

For GUI checks, copy `build/libs/addressbook.jar` into an empty temporary folder and run
`java -jar addressbook.jar` there. This keeps existing user data separate from test data.

1. Confirm that client and appointment panels are both visible, with `No appointments found.` initially.
2. Enter `appointment n/Viewing c/C1 loc/NUS s/20261015-1000 e/20261015-1100`.
   Expect a new `A1` card showing the client, Singapore start/end date-times and location. The command box clears.
3. Add a later appointment using
   `appointment n/Signing c/C2 loc/Clementi s/20261016-1400 e/20261016-1500`.
   Confirm chronological order and consecutive `A1`, `A2` references.
4. Run `appointments d/16102026`. Only Signing should appear, now as `A1`. The client list should not change.
5. Run `deleteappt A1` and `appointments d/30022026`. Each should show an error, retain the command text,
   and leave the Signing card and existing filter unchanged.
6. Run `deleteappt A1 confirm`. Signing should disappear; Viewing should reappear as `A1`.
   Both clients should remain, and the command box should clear.
7. Run `appointments c/C2`. Expect an empty state and `No appointments found.`
8. Run `appointments`, then `edit 1 n/Updated Client`. Viewing should show the updated client name.
9. Close and reopen the JAR in the same folder. Viewing and its client link should remain, and Signing
   should stay deleted. Run `deleteappt A1 yes`; confirm deletion feedback and the empty state.
10. For a longer list, add several adjacent, non-overlapping appointments and scroll the appointment panel.
    Check that reused cells keep the correct displayed references and text wraps within each card.

Appointment filters are not persisted between runs. Date filtering uses the start date, including for
appointments crossing midnight. Automated tests also cover empty lists, both filters together,
out-of-range indexes, duplicate/unknown fields, overflow, clashes with hidden appointments, client deletion,
`clear`, read-only viewing, invalid JSON entries and legacy client-only files.

## Undo and redo smoke test

Use a fresh temporary data folder for this sequence.

1. Run `edit 1 n/Updated Client`, then create Viewing using step 2 above. Run `undo` once:
   Viewing disappears and Updated Client remains. A second `undo` restores the original client.
   Two `redo` commands restore each change separately.
2. Run `undo`, then create an appointment at a different time. Run `redo`: expect `Nothing to redo.`
   and confirm that the newly created appointment remains, including after restarting later.
3. Filter both lists using `search n/Updated` and `appointments c/C1`, then run `undo` and `redo`.
   Both full lists should appear after each successful history operation, with correct client links.
4. Run `deleteappt A1 yes`. Both `undo` and `redo` should now report that nothing is available.
   Edit a client, then `undo`: that edit is reversed, while the deleted appointment stays absent.
5. Restart the JAR. Check saved clients and appointments; neither undo nor redo should be available.

`AppointmentHistoryIntegrationTest` checks parsed commands against real JSON persistence.
`AppointmentHistorySaveFailureTest` injects I/O and permission errors before writing and verifies
data, filters and both history directions are recovered, followed by a successful retry.
`AppointmentEditHistoryTest` covers the future `editappt` model operation, including changes to every
field, clashes, adjacent times, no-op edits, branching and client lifecycle changes.
`AppointmentHistorySequenceTest` checks 1,000 mixed operations against an independent expected timeline
using four fixed seeds, making any failure reproducible. `ModelCheckpointTest` also covers checkpoint
ownership and restoring a truncated or cleared timeline more than once.

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
