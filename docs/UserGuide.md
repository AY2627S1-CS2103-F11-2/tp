---
layout: page
title: User Guide
---

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 a/John street, block 123, #01-01 y/buyer` : Adds a client named `John Doe` without an email address.

   * `delete C3` : Deletes the 3rd client shown in the current list.

   * `clear` : Deletes all contacts.

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

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Edited names follow the same validation and space normalization rules as [adding a client](#adding-a-client-add).
* Omit `e/EMAIL` to retain the client's current email. To clear an existing email, supply a bare `e/`;
  the client card and command feedback then show `Not provided`.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.
*  `edit 1 e/` Clears the email address of the 1st person.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a client: `delete`

Deletes the specified client from the address book.

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

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

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
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List** | `list`
**Help** | `help`
