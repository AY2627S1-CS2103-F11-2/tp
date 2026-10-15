---
layout: page
title: User Guide
---

PropTrack is a **desktop client directory for insurance and property agents, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). It helps agents add, find and update client contact details quickly, reducing the time spent on contact administration. If you type quickly, PropTrack can help you manage your clients faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103-F11-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for PropTrack.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar proptrack.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all clients.

   * `add n/John Doe p/98765432 a/John street, block 123, #01-01 y/buyer` : Adds a client named `John Doe` without an email address.

   * `delete C3` : Deletes the 3rd client shown in the current list.

   * `clear` : Deletes all clients.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a client: `add`

Adds a client to PropTrack.

Format: `add n/NAME p/PHONE_NUMBER a/ADDRESS y/TYPE [e/EMAIL] [t/TAG]…​`

* `NAME` must contain 1–100 printable characters and at least one Unicode letter or digit.
  Punctuation is allowed, including apostrophes and hyphens.
* Leading and trailing spaces are removed, and repeated spaces are collapsed to one space.
  Letter case is preserved. For example, `n/  Anne-Marie   O'Connor  ` is stored as `Anne-Marie O'Connor`.
* The length limit applies after spaces are normalized. Unicode characters, including supplementary characters,
  each count as one character.
* Blank, punctuation-only, non-printable and overlong names are rejected with
  `Name must contain 1-100 characters and at least one letter or digit.` No client is added.
* `TYPE` must be `buyer`, `seller`, `landlord` or `tenant`, in lowercase.
* Email is optional. Omit the entire `e/EMAIL` field if the client's email is unknown.
  The client card and command feedback show `Not provided`, and the email remains absent after saving and reopening the app.
* If `e/` is supplied when adding a client, it must contain a valid email address. A bare `e/`,
  whitespace-only email or malformed address is rejected. Repeating `e/` is also rejected.

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A client can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 y/buyer`
* `add n/Alex Tan p/91234567 a/123 Clementi Road y/buyer` adds a client whose email is not yet known.
* `add n/Anne-Marie O'Connor p/91234567 e/anne@example.com a/123 Clementi Road y/seller`
* `add n/张伟 p/92345678 e/zhang@example.com a/456 Tampines Road y/tenant t/friend`

### Listing all clients: `list`

Shows every client in PropTrack, renumbered from `C1`, and clears any active filter.

Format: `list`

* The result box reports how many clients are shown, for example `Listed 14 clients`.
* Anything typed after `list` is ignored, so `list now` behaves the same as `list`.

### Editing a client: `edit`

Edits an existing client in PropTrack.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [y/TYPE] [t/TAG]…​`

* Edits the client at the specified `INDEX`. The index refers to the index number shown in the displayed client list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Edited names follow the same validation and space normalization rules as [adding a client](#adding-a-client-add).
* If `y/TYPE` is supplied, it must be `buyer`, `seller`, `landlord` or `tenant`, in lowercase.
* Omit `e/EMAIL` to retain the client's current email. To clear an existing email, supply a bare `e/`;
  the client card and command feedback then show `Not provided`.
* When editing tags, all of the client's existing tags are removed; adding tags is not cumulative.
* To remove all of a client's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st client to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd client to be `Betsy Crower` and clears all existing tags.
*  `edit 1 e/` Clears the email address of the 1st client.

### Finding clients by name: `find`

Finds clients whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Clients matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a client: `delete`

Deletes the specified client from PropTrack.

Format: `delete CLIENT_ID`

* `CLIENT_ID` is uppercase `C` followed directly by a positive integer, e.g. `C1`, `C4`, or `C12`.
* The number refers to the client's position in the **currently displayed list**, including search results.
  It is not a permanent unique ID. Client cards display these references as `C1`, `C2`, etc.
* The selected card disappears and subsequent clients are renumbered. Repeating `delete C1` deletes the
  new first client, or reports an error if the list is empty.
* Missing arguments, invalid references (`C0`, `C-2`, `C3.5`, `Cabc`), and extra arguments are rejected.
  A reference beyond the displayed list size also reports an error without deleting a client.
* Successful deletion displays `Deleted Client: ` followed by the client's current details.
* Other commands such as `edit` still use the numeric position without `C` (e.g. `edit 1 n/Alice`).

Examples:
* `list` followed by `delete C2` deletes the 2nd client in the address book.
* `find Betsy` followed by `delete C1` deletes the 1st client in the results of the `find` command.

### Adding an appointment: `appointment`

Creates an appointment for a client in the currently displayed client list.

Format: `appointment n/NAME c/CLIENT_INDEX loc/LOCATION s/YYYYMMDD-HHMM e/YYYYMMDD-HHMM`

* Every field is required and may appear only once. Fields may be supplied in any order.
* `CLIENT_INDEX` is a displayed client reference such as `C1`; it is not a permanent ID.
* The name contains letters and spaces, with 1-80 characters. The location contains letters,
  digits and spaces and cannot be blank. Leading and trailing spaces are removed.
* Start and end must be real calendar date-times in 24-hour Singapore local time.
  The end must be strictly after the start. Appointments may cross midnight.
* An appointment that overlaps any stored appointment is rejected, including appointments hidden by a filter.
  Adjacent appointments, such as 10:00-11:00 and 11:00-12:00, are allowed.
* On success, all appointments appear in chronological order and the new appointment's displayed reference
  is included in the result. The client list stays as it was.

Example: `appointment n/Viewing c/C1 loc/Sentosa Cove 123456 s/20261015-1000 e/20261015-1100`

### Viewing appointments: `appointments`

![Client and appointment panels showing the schedule](images/appointments.png)

Format: `appointments [c/CLIENT_INDEX] [d/DDMMYYYY]`

* With no fields, displays all stored appointments. Each command replaces the previous appointment filter.
* `c/C1` selects the first client in the **currently displayed client list**, including search results.
  Viewing appointments does not change that client list.
* `d/15102026` selects appointments whose **start date** is 15 October 2026 in Singapore local time.
  Past, present and future dates are accepted. For an appointment that crosses midnight, use its start date.
* When both fields are supplied, an appointment must match both. Field order does not matter.
* Results are sorted by start date and time, earliest first, and numbered `A1`, `A2`, etc.
  Each card shows the appointment name, client name, start, end, location and any remarks.
* The result is `Showing N appointments.`, or `No appointments found.` with an empty panel.
  An empty result is successful. Viewing appointments does not write to the data file.
* Each field may appear only once. Empty values, unknown fields and extra arguments are rejected.
  `C0`, `c1`, `C01` and client references outside the displayed list are invalid.
* Invalid or impossible dates such as `30022026` produce `Enter a valid date in DDMMYYYY format.`
  An empty `d/` produces `DATE cannot be empty.` Invalid commands leave both lists unchanged.

Examples:

* `appointments`
* `appointments c/C1`
* `appointments d/15102026`
* `appointments c/C2 d/15102026`

### Deleting an appointment: `deleteappt`

Format: `deleteappt A<INDEX> yes` or `deleteappt A<INDEX> confirm`

* The reference identifies one appointment in the **currently displayed appointment list**.
  Use the reference shown on its card, including after filtering. It is not a permanent ID.
* A final, lowercase `yes` or `confirm` is required. Type one of those words, not `yes/confirm`.
* Success displays `Deleted appointment A2. This action cannot be undone.` when deleting `A2`.
  All remaining appointments are then displayed in chronological order and renumbered from `A1`.
  If none remain, `No appointments found.` is also displayed.
* The associated client and the client list remain unchanged.
* Missing references, invalid references, missing confirmation, extra arguments and references outside
  the displayed appointment list are rejected without deleting anything or changing the filters.
  A missing confirmation produces `Deletion requires yes/confirm. Nothing was deleted.`
* Appointment deletion is saved automatically. Undo and redo are not available in this version.
* Editing a client's details retains their linked appointments. Deleting a client removes all of
  that client's appointments. `clear` removes both clients and appointments.

Examples:

* `appointments d/15102026` followed by `deleteappt A1 yes` deletes the first appointment in those results.
* `deleteappt A3 confirm` deletes the third appointment currently shown.

### Clearing all entries: `clear`

Clears all clients and appointments from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

PropTrack automatically saves clients and appointments after changes. You do not need to save manually.
Existing client-only data files remain readable and start with an empty appointment list.

### Editing the data file

PropTrack data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If the file contains invalid client or appointment data, PropTrack rejects the whole file and starts empty.
It attempts to preserve the unreadable file under a name ending in `.corrupt-TIMESTAMP`; the log records
the preserved path or any failure to rename it. Back up the data file before editing it.<br>
Appointment `clientIndex` fields refer to positions in the saved client array. Reordering that array by hand
without updating those references can associate appointments with the wrong clients.
</div>

 --------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous PropTrack home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER a/ADDRESS y/TYPE [e/EMAIL] [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 a/123, Clementi Rd, 1234665 y/buyer t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete CLIENT_ID`<br> e.g., `delete C3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [y/TYPE] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List** | `list`
**Add appointment** | `appointment n/NAME c/CLIENT_INDEX loc/LOCATION s/YYYYMMDD-HHMM e/YYYYMMDD-HHMM`
**View appointments** | `appointments [c/CLIENT_INDEX] [d/DDMMYYYY]`
**Delete appointment** | `deleteappt A<INDEX> yes` or `deleteappt A<INDEX> confirm`
**Help** | `help`
