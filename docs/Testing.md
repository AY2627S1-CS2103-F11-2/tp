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

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
