package edu.gatech.seclass.textprocessor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Timeout(value = 1, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class MyLibTest {
    @TempDir
    Path tempDirectory;

    @RegisterExtension
    OutputCapture capture = new OutputCapture();

    /*
     * Test Utilities
     */

    private Path createFile(String contents) {
        return createFile(contents, "input.txt");
    }

    private Path createFile(String contents, String fileName) {
        Path file = tempDirectory.resolve(fileName);

        try {
            Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }

        return file;
    }

    private String getFileContent(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Test Case
     */

    @Test
    public void exampleTest1() {
        String input = "This is the first line of the input file." + System.lineSeparator();
        String expected = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        Path outputFile = tempDirectory.resolve("output.txt");

        TextProcessorInterface utility = new TextProcessor();
        utility.setOutputFile(outputFile.toString());
        utility.setFilepath(inputFile.toString());

        Assertions.assertDoesNotThrow(utility::textprocessor);
        Assertions.assertTrue(capture.stdout().isEmpty());
        Assertions.assertTrue(capture.stderr().isEmpty());
        Assertions.assertEquals(expected, getFileContent(outputFile));
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test
    public void exampleTest2() {
        String input = "this is a lin." + System.lineSeparator();
        String expected = "this is a line." + System.lineSeparator();

        Path inputFile = createFile(input);

        TextProcessorInterface utility = new TextProcessor();
        utility.setFilepath(inputFile.toString());
        utility.setCaseInsensitive(true);
        utility.setReplaceText("lin.", "line.");

        Assertions.assertDoesNotThrow(utility::textprocessor);
        Assertions.assertTrue(capture.stderr().isEmpty());
        Assertions.assertEquals(expected, capture.stdout());
    }
}
