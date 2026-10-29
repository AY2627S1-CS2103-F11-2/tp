package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Client to select after the command has been saved successfully, if any. */
    private final Person selectedClient;

    /** Whether logic must persist the command's changes before returning the result. */
    private final boolean saveRequired;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, Person selectedClient) {
        this(feedbackToUser, showHelp, exit, selectedClient, true);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit,
            Person selectedClient, boolean saveRequired) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.selectedClient = selectedClient;
        this.saveRequired = saveRequired;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /** Constructs a successful result that asks the UI to select the specified stored client. */
    public CommandResult(String feedbackToUser, Person selectedClient) {
        this(feedbackToUser, false, false, requireNonNull(selectedClient));
    }

    /** Returns a successful result for a command that does not change persisted data. */
    public static CommandResult withoutSave(String feedbackToUser) {
        return withoutSave(feedbackToUser, false, false);
    }

    /** Returns a result that skips data saving while retaining help and exit actions for the UI. */
    public static CommandResult withoutSave(String feedbackToUser, boolean showHelp, boolean exit) {
        return new CommandResult(feedbackToUser, showHelp, exit, null, false);
    }

    public boolean isSaveRequired() {
        return saveRequired;
    }

    public Optional<Person> getSelectedClient() {
        return Optional.ofNullable(selectedClient);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && saveRequired == otherCommandResult.saveRequired
                && Objects.equals(selectedClient, otherCommandResult.selectedClient)
                && getSelectedClient().flatMap(Person::getClientId)
                        .equals(otherCommandResult.getSelectedClient().flatMap(Person::getClientId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, selectedClient, saveRequired,
                getSelectedClient().flatMap(Person::getClientId));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("selectedClient", selectedClient)
                .toString();
    }

}
