package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path testFolder;

    @Test
    public void quarantineCorruptFile_existingFile_movesFileAsideAndKeepsItsContent() throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "{ this is not valid json");

        Path quarantinedFile = FileUtil.quarantineCorruptFile(dataFile);

        assertFalse(Files.exists(dataFile));
        assertTrue(Files.exists(quarantinedFile));
        assertEquals("{ this is not valid json", Files.readString(quarantinedFile));
        assertEquals(testFolder, quarantinedFile.getParent());
        assertTrue(quarantinedFile.getFileName().toString()
                .matches("addressbook\\.json\\.corrupt-\\d{4}-\\d{2}-\\d{2}-\\d{4}"));
    }

    @Test
    public void quarantineCorruptFile_repeatedRecovery_preservesEveryCopy() throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "first unreadable file");
        Path firstCopy = FileUtil.quarantineCorruptFile(dataFile);
        Files.writeString(dataFile, "second unreadable file");

        Path secondCopy = FileUtil.quarantineCorruptFile(dataFile);

        assertFalse(firstCopy.equals(secondCopy));
        assertEquals("first unreadable file", Files.readString(firstCopy));
        assertEquals("second unreadable file", Files.readString(secondCopy));
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void quarantineCorruptFile_fileDoesNotExist_throwsException() {
        Path missingFile = testFolder.resolve("missing.json");
        assertThrows(IOException.class, () -> FileUtil.quarantineCorruptFile(missingFile));
    }
}
