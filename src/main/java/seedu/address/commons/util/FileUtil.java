package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    private static final DateTimeFormatter QUARANTINE_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm", Locale.ENGLISH);

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

    /**
     * Moves the given file aside so that the app cannot overwrite it, and returns where it went.
     * The file keeps its name and gains a {@code .corrupt-<timestamp>} suffix, for example
     * {@code addressbook.json} becomes {@code addressbook.json.corrupt-2026-10-08-1930}.
     *
     * @param file the file to move aside, which must exist.
     * @return the path the file was moved to.
     * @throws IOException if the file cannot be moved.
     */
    public static Path quarantineCorruptFile(Path file) throws IOException {
        String quarantinedFileName = file.getFileName() + ".corrupt-"
                + LocalDateTime.now().format(QUARANTINE_TIMESTAMP_FORMAT);
        Path quarantinedFile = file.resolveSibling(quarantinedFileName);
        Files.move(file, quarantinedFile, StandardCopyOption.REPLACE_EXISTING);
        return quarantinedFile;
    }

}
