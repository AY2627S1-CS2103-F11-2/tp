---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is an insurance or property agent managing a large, frequently changing list of clients
* needs to add, find and update client contact details regularly
* prefers a desktop application for managing client records
* can type quickly and prefers typing to mouse interactions
* is reasonably comfortable using a command-line interface

**Value proposition**: PropTrack helps insurance and property agents manage client contacts efficiently.
Its command-based desktop interface lets agents add, find and update client details quickly,
reducing time spent on contact administration.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​                                                  | I want to …​                                                                          | So that I can…​                                                          |
| -------- | -------------------------------------------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `* * *`  | user                                                     | add a client with phone, email, address and tags                                       | keep all my contacts in one place                                          |
| `* * *`  | agent on a call with 200+ clients stored                 | find a client by typing part of their name                                             | get their details in seconds                                               |
| `* *`    | user who cannot recall the exact command or spelling     | still reach the right client                                                           | keep going when a typo happens mid-call                                    |
| `* *`    | user                                                     | edit a client's details                                                                | correct mistakes I made when adding them                                   |
| `* * *`  | user                                                     | delete a client I no longer track                                                      | keep my list current                                                       |
| `* * *`  | user who made a wrong change                             | undo my last change                                                                    | stop one wrong command from destroying my data                             |
| `* *`    | user who deleted a client by mistake                     | get that client back                                                                   | stop one slip from costing me a client                                     |
| `* *`    | user                                                     | tag a client                                                                           | group clients that share a location, a budget or a source                  |
| `* *`    | user                                                     | find all clients that carry a given tag                                                | work one group at a time                                                   |
| `* * *`  | agent tracking deals                                     | label each client with their deal stage and pull up everyone in one stage               | see at a glance who is a lead, viewing, offer or closed                    |
| `* * *`  | user                                                     | record notes about my interactions with a client                                        | recall previous discussions before following up                            |
| `* * *`  | forgetful agent                                          | see which clients I still owe a follow-up                                               | stop a lead going cold                                                     |
| `* *`    | user                                                     | associate an appointment with a client's record                                         | reach their details while preparing for it                                 |
| `* * *`  | user                                                     | see all my appointments in chronological order                                          | plan my schedule                                                           |
| `* *`    | user                                                     | add an appointment and be warned when it clashes with an existing one                   | avoid scheduling conflicts                                                 |
| `* *`    | user                                                     | find a specific appointment                                                            | change or remove its contents                                              |
| `* *`    | user                                                     | mark an appointment as completed                                                        | tell past meetings from ones that still need attention                     |
| `* *`    | user                                                     | see the appointments and follow-ups coming up when I open the app                       | not forget something that is already scheduled                             |
| `* * *`  | user                                                     | retain my contacts and appointments when I close and reopen the program                 | continue without re-entering data                                          |
| `* * *`  | user                                                     | rely on my changes being saved as I work                                                | know that closing the app never loses an entry                             |
| `* * *`  | cautious user                                            | back up my client data and restore it when the data file breaks                          | never lose months of contacts                                              |
| `* *`    | user comfortable with text files                         | hand-edit the saved data file                                                           | fix or bulk-edit data without the app                                      |
| `* * *`  | new user                                                 | view a guided introduction to the available functions                                    | learn the app without prior command-line experience                        |
| `* * *`  | user                                                     | get a clear explanation of invalid input and how to correct it                          | resolve mistakes without guessing                                          |
| `* *`    | user who mistyped a command                              | be pointed at the closest valid command                                                 | recover without reading the whole help page                                |
| `* * *`  | potential user exploring the app                         | see it populated with sample data                                                        | see how it will look in real use                                           |
| `* * *`  | user ready to start for real                             | purge all sample data                                                                   | clear the experimental entries                                             |
| `* *`    | expert user                                              | shorten the commands I run all day                                                      | save time on frequently performed tasks                                    |
| `*`      | long-time user                                           | archive clients I no longer track                                                        | stop closed deals cluttering my list                                       |
| `*`      | user                                                     | import my existing contacts in bulk                                                     | avoid entering each client manually                                        |
| `*`      | user coming back after months away                       | pick up where I left off                                                                | avoid relearning the app                                                   |

### Use cases

(For all use cases below, the **System** is `PropTrack` and the **Actor** is the `real estate agent`, unless specified otherwise.)

**Use case: UC01 - Register a new client and schedule a viewing**

**MSS**

1. Agent requests to add a new client, providing the client's name, phone number, client type, and optionally email address.
2. PropTrack adds the client and displays the client's assigned ID and details.
3. Agent requests to create a viewing appointment for that client, providing the appointment name, location, start date and time, and end date and time.
4. PropTrack creates the appointment and displays its details, including the associated client.

   Use case ends.

**Extensions**

* 1a. The client details are invalid or required information is missing.

    * 1a1. PropTrack explains the problem and the accepted values. No client is added.

      Use case resumes at step 1.

* 1b. The supplied name matches an existing client.

    * 1b1. PropTrack identifies the existing client and reports that no client was added.

      Use case ends.

* 3a. The appointment details are invalid or incomplete.

    * 3a1. PropTrack explains the problem. No appointment is created.

      Use case resumes at step 3.

* 3b. The requested appointment overlaps an existing appointment.

    * 3b1. PropTrack reports the scheduling conflict and identifies the conflicting appointment or appointments. No appointment is created.

      Use case resumes at step 3.

**Use case: UC02 - Update a client's contact details**

**MSS**

1. Agent requests to search for a client using the client's name, phone number, or client type.
2. PropTrack displays the matching clients.
3. Agent identifies the intended client and requests to update the client's phone number, email address, or both.
4. PropTrack updates the supplied contact details and displays the updated client record, retaining the client's ID and linked appointments.

   Use case ends.

**Extensions**

* 1a. The search criteria are invalid or empty.

    * 1a1. PropTrack explains the accepted search criteria.

      Use case resumes at step 1.

* 2a. No clients match the search criteria.

  Use case ends.

* 3a. The supplied contact details are invalid.

    * 3a1. PropTrack explains the problem and leaves the client record unchanged.

      Use case resumes at step 3.

* 3b. The supplied contact details are identical to the existing values.

    * 3b1. PropTrack informs the agent that no changes were made.

      Use case ends.

* 3c. The supplied client reference is invalid or does not identify an existing client.

    * 3c1. PropTrack reports the problem. No client record is changed.

      Use case resumes at step 3.

**Use case: UC03 - Change an appointment's meeting location**

**MSS**

1. Agent requests to view appointments for a specified date.
2. PropTrack displays the matching appointments in chronological order.
3. Agent identifies the intended appointment and requests to replace its meeting location.
4. PropTrack updates the appointment and displays the revised details.

   Use case ends.

**Extensions**

* 1a. The supplied date is invalid.

    * 1a1. PropTrack reports the problem with the date.

      Use case resumes at step 1.

* 2a. No appointments match the specified date.

    * 2a1. PropTrack informs the agent that no appointments were found.

      Use case ends.

* 3a. The appointment reference does not identify an appointment in the displayed list.

    * 3a1. PropTrack reports the invalid reference. No appointment is changed.

      Use case resumes at step 3.

* 3b. The supplied location is blank.

    * 3b1. PropTrack informs the agent that the location cannot be blank. No appointment is changed.

      Use case resumes at step 3.

**Use case: UC04 - Remove a cancelled appointment**

**MSS**

1. Agent requests to view appointments for a specified date.
2. PropTrack displays the matching appointments in chronological order.
3. Agent identifies the cancelled appointment and requests its deletion, explicitly confirming the deletion.
4. PropTrack deletes the appointment, confirms its removal, and displays all remaining appointments in chronological order. The associated client remains in the client directory.

   Use case ends.

**Extensions**

* 1a. The supplied date is invalid.

    * 1a1. PropTrack reports the problem with the date.

      Use case resumes at step 1.

* 2a. No appointments match the specified date.

    * 2a1. PropTrack informs the agent that no appointments were found.

      Use case ends.

* 3a. The appointment reference does not identify an appointment in the displayed list.

    * 3a1. PropTrack reports the invalid reference. No appointment is deleted.

      Use case resumes at step 3.

* 3b. Agent omits the required deletion confirmation.

    * 3b1. PropTrack reports that confirmation is required. No appointment is deleted.

      Use case resumes at step 3.

* 4a. No appointments remain after the deletion.

    * 4a1. PropTrack confirms the deletion and informs the agent that no appointments were found.

      Use case ends.

### Non-Functional Requirements

Environment NFRs
1. PropTrack must be able to perform consistently on any _mainstream OS_ as long as it has Java `25` or above installed. 
2. PropTrack must be able to operate completely offline and will not require any internet connection or connection with external servers to operate normally.
3. It must support, at minimum, the english language.
4. PropTrack must be packaged in a Jar executable file that can be run by either double-clicking it or through the terminal.

Performance NFRs
1. Should be able to hold up to 1000 Clients without noticeable sluggishness in performance for typical usage.
2. PropTrack should return the search results within a reasonable time of 1 second(s) for a data size of 100 clients.
3. PropTrack commands should remain responsive (Do not feel clunky or hang) when executing commands on a very large but supported dataset size.
4. During the use of PropTrack, it should not cause any excessive or noticeable lag, freezing during normal use.
5. During auto-saving of data (when a command that modifies the data file is successfully executed), it will not cause any noticeable lag or freezing during the process.
6. PropTrack should only rely on the CPU and Integrated GPU to run and not need any extra/special hardware to run.
7. Normal operation of PropTrack should not cause the computer to use a tremendous amount of RAM, CPU, GPU (To the point the cooling fans start to run on a laptop).

Data NFRs
1. PropTrack is able to locate its data files without depending on the user's current directory to do so.
2. PropTrack's data must retain its data after user closes or restarts it.
3. Any command that makes changes in PropTrack's data files will be automatically saved without requiring a user input to manually save any change(s) made.
4. Only successful commands will be able to modify the contents of the data file, failed commands will not be able to modify the data file at all.
5. Once a client is assigned a client ID, it will persist throughout all commands and cannot be changed by any commands.
6. A client's ID remains uniquely to that specific client and will not repeat else where even after deletion.
7. PropTrack will not replace corrupted/unreadable data with an empty dataset and must inform the user that data content has issues being read.
8. Client data and appointment must persist and remain correct throughout the use of the application.
9. No unsuccessful command must cause the data file to become corrupted nor its data rendered unreadable.
10. User inputting the same operation rapidly/repeatedly will not cause the client ID to duplicate, client data to be lost or produce malformed data.
11. The background save function must protect and ensure that the data file does not become unusable/corrupted.

Usability NFRs
1. A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
2. When error messages appear, they must clearly identity what is the issue causing the error and provide a sample command/command format for the user to reference.
3. Client and appointment ordering and indexing will remain consistent and predictable.
4. Empty filter results must be distinguishable from application or data-loading failures when displaying search results.
5. Commands must maintain consistent behaviour regardless of the user's previous filtering or viewing actions.
6. User input must be standardized according to the command format, any deviation should be considered an error and flagged to the user
7. PropTrack must be able to process other user's data file so long as they were generated by a PropTrack application.
8. PropTrack should not crash as a result of malformed user input/data content.

Security NFRs
1. PropTrack must not transmit any data fields(Client data, appointment data, data file) over any network during normal operations
2. No personal/identifying information must be displayed in any logs, temporary files or diagnostic output.
3. Malformed, excessively long or unexpected user inputs should not cause PropTrack to crash or consume a tremendous amount of resources.
4. Any malformed or unexpected data contents in the data file should not be processed and should be ignored.
5. No client data will be shared with any third parties.

Maintainability and Scalability NFRs
1. PropTrack's data file must be portable and supported in all mainstream OSes.
2. PropTrack must use a standard format for storing client data, appointments in its data file
3. Modifying/updating a command's validation, format or parsing should not affect an unrelated command in any other way.
4. JUnit tests should cover all normal, expected user operations, test if the proper error messages are returned for a given error and test for all edge cases that causes the program to crash/not respond properly.
5. Incompatible or unsupported data file types should not be processed by PropTrack.
6. The application should be designed such that it can easily handle and process a growing list of clients and their respective appointment data.
7. The client class and appointment class should be designed such that additional parameters can be added smoothly without causing major code refactoring.


### Glossary

* **Client**: A person whose contact details the property agent manages in PropTrack. A client may be a buyer, seller, landlord, or tenant.
* **Client type**: The client's role in a property transaction: buyer, seller, landlord, or tenant.
* **Client ID**: A unique identifier assigned to a client, written as `C` followed by a positive integer (for example, `C1`). It remains associated with that client when the displayed list changes.
* **Displayed index**: The one-based position of a client or appointment in the list currently shown on screen. It can change when records are added, removed, sorted, or filtered, so it is distinct from a stable ID.
* **Filtered list**: The subset of clients or appointments currently shown after a search or filter is applied.
* **Appointment**: A scheduled meeting associated with a client, with a name, location, and date and time.
* **Appointment ID**: A unique identifier assigned to an appointment, written as `A` followed by a positive integer (for example, `A2`).
* **Schedule clash**: An overlap between the time of a proposed appointment and an existing appointment.
* **Data file**: The local file in which PropTrack stores client and appointment records between runs.
* **Backup**: A separate copy of the saved data that can be used to recover records.
* **Restore**: Replace the current records with those in a valid backup file.
* **Undo and redo**: Reverse the latest data-changing command, or reapply a change that was undone, respectively.
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Private contact detail**: A contact detail that is not meant to be shared with others

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
