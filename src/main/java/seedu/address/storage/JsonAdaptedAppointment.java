package seedu.address.storage;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Person;

/** Jackson-friendly appointment linked by position in the saved, unfiltered client list. */
class JsonAdaptedAppointment {
    private final String name;
    private final Integer clientIndex;
    private final String location;
    private final String start;
    private final String end;
    private final String remarks;

    public JsonAdaptedAppointment(String name, Integer clientIndex, String location,
            String start, String end, String remarks) {
        this.name = name;
        this.clientIndex = clientIndex;
        this.location = location;
        this.start = start;
        this.end = end;
        this.remarks = remarks;
    }

    public JsonAdaptedAppointment(Appointment appointment, List<Person> clients) {
        this(appointment.getName(), clients.indexOf(appointment.getClient()) + 1,
                appointment.getLocation(), appointment.getStart().toString(), appointment.getEnd().toString(),
                appointment.getRemarks());
    }

    /** Rejects fractional, overflowing and non-numeric references rather than coercing them to a client. */
    @JsonCreator
    public static JsonAdaptedAppointment fromJson(@JsonProperty("name") String name,
            @JsonProperty("clientIndex") JsonNode clientIndex, @JsonProperty("location") String location,
            @JsonProperty("start") String start, @JsonProperty("end") String end,
            @JsonProperty("remarks") String remarks) {
        Integer index = clientIndex != null && clientIndex.isIntegralNumber() && clientIndex.canConvertToInt()
                ? clientIndex.intValue() : null;
        return new JsonAdaptedAppointment(name, index, location, start, end, remarks);
    }

    /** Validates all fields and resolves the client only against the complete saved client list. */
    public Appointment toModelType(List<Person> clients) throws IllegalValueException {
        if (name == null || clientIndex == null || location == null || start == null || end == null
                || clientIndex < 1 || clientIndex > clients.size()) {
            throw new IllegalValueException("Appointment has missing fields or an invalid client reference.");
        }
        try {
            return new Appointment(name, clients.get(clientIndex - 1), location,
                    LocalDateTime.parse(start), LocalDateTime.parse(end), remarks == null ? "" : remarks);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new IllegalValueException("Invalid appointment: " + e.getMessage());
        }
    }
}
