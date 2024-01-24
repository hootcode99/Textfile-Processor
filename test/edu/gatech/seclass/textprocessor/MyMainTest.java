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
public class MyMainTest {
    private final String usageStr =
            "Usage: textprocessor [ -o filename | -i | -k substring | -r old new | -n padding | -w | -s suffix ] FILE"
                    + System.lineSeparator();
    @TempDir
    Path tempDirectory;

    @RegisterExtension
    OutputCapture capture = new OutputCapture();

    /*
     * Test Utilities
     */

    private Path createFile(String contents) throws IOException {
        return createFile(contents, "input.txt");
    }

    private Path createFile(String contents, String fileName) throws IOException {
        Path file = tempDirectory.resolve(fileName);
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));

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
     * Test Cases -----------------------------------------------
     */
    @Test // Test Case 1: output result to file
    public void textprocessorTest1() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();
        String expected = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        Path outputFile = tempDirectory.resolve("output.txt");
        String[] args = {"-o", outputFile.toString(), inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output file contents match expected output
        Assertions.assertEquals(expected, getFileContent(outputFile));
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 2: empty input file produces empty output file
    public void textprocessorTest2() throws IOException {
        String input = "";
        String expected = "";

        Path inputFile = createFile(input);
        Path outputFile = tempDirectory.resolve("output.txt");
        String[] args = {"-o", outputFile.toString(), inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output file contents match expected output
        Assertions.assertEquals(expected, getFileContent(outputFile));
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 3: specify -i without -r or -k
    public void textprocessorTest3() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-i", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 4: specify -k with empty substring argument
    public void textprocessorTest4() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();
        String expected = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-k", "", inputFile.toString()};
        Main.main(args);

        // input has matches expected output
        Assertions.assertEquals(input, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
    }

    @Test // Test Case 5: specify -r with blank <old> parameter
    public void textprocessorTest5() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "", "new", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 6: specify -s with blank <suffix> parameter
    public void textprocessorTest6() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-s", "", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 7: specify -r and -k throws error
    public void textprocessorTest7() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "old", "new", "-k", "substring", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 8: specify -n <padding> as non-integer
    public void textprocessorTest8() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-n", "2.5", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 9: specify -n <padding> as out of range
    public void textprocessorTest9() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-n", "11", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 10: specify -n and -w throws error
    public void textprocessorTest10() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-n", "6", "-w", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 11: parameters omitted
    public void textprocessorTest11() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "-w", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 12: last line of non-empty input file is not terminated with newline character
    public void textprocessorTest12() throws IOException {
        String input = "This is the first line of the input file.";

        Path inputFile = createFile(input);
        String[] args = {"-w", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 13: -w properly removes whitespaces
    public void textprocessorTest13() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();
        String expected = "Thisisthefirstlineoftheinputfile." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-w", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 14: -w properly removes tabs
    public void textprocessorTest14() throws IOException {
        String input = "This\tis\tthe\tfirst\tline\tof\tthe\tinput\tfile." + System.lineSeparator();
        String expected = "Thisisthefirstlineoftheinputfile." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-w", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 15: -n properly adds padding "2"
    public void textprocessorTest15() throws IOException {
        String input = "I wish this line had a line number.." + System.lineSeparator()
                + "I also wish that.." + System.lineSeparator();
        String expected = "01 I wish this line had a line number.." + System.lineSeparator()
                + "02 I also wish that.." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-n", "2", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 16: only the last occurrence of an argument is considered
    public void textprocessorTest16() throws IOException {
        String input = "I wish this line had a line number.." + System.lineSeparator()
                + "I also wish that.." + System.lineSeparator();
        String expected = "01 I wish this line had a line number.." + System.lineSeparator()
                + "02 I also wish that.." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-n", "8", "-n", "2", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 17: -s argument adds suffix
    public void exampleTest17() throws IOException {
        String input = "This is cool" + System.lineSeparator() + "This is fast" + System.lineSeparator();
        String expected = "This is cooler" + System.lineSeparator() + "This is faster" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-s", "er", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 18: -r argument replaces first <old> string with <new>
    public void exampleTest18() throws Exception {
        String input = "This Sentence Is Old" + System.lineSeparator();
        String expected = "This Sentence Is New" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "Old", "New", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 19: -r argument replaces first <old> string with <new> -i option (case insensitive)
    public void exampleTest19() throws Exception {
        String input = "This Sentence Is Old" + System.lineSeparator();
        String expected = "This Sentence Is New" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-i", "-r", "old", "New", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 20: -k argument replaces first <old> string with <new>
    public void exampleTest20() throws Exception {
        String input = "This line has the substring" + System.lineSeparator()
                + "This line does not" + System.lineSeparator();
        String expected = "This line has the substring" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-k", "substring", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 21: -k argument replaces first <old> string with <new> -i option (case insensitive)
    public void exampleTest21() throws Exception {
        String input = "This line has the substring" + System.lineSeparator()
                + "This line does not" + System.lineSeparator();
        String expected = "This line has the substring" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-i", "-k", "Substring", inputFile.toString()};
        Main.main(args);

        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // output matches expected output
        Assertions.assertEquals(expected, capture.stdout());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 22: no arguments specified
    public void exampleTest22() throws Exception {
        String input = "Today is January 65, 2298." + System.lineSeparator()
                + "Yesterday was December 0, 3000." + System.lineSeparator()
                + "Tomorrow we will time travel again.";

        Path inputFile = createFile(input);
        String[] args = {};
        Main.main(args);

        // no output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 23: no output file parameter given
    public void textprocessorTest23() throws IOException {
        String input = "This is the the content of the file" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-o", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 24: verify processing order -k, -n, -s
    public void textprocessorTest24() throws IOException {
        String input = "This course's title is CS6300. #keep" + System.lineSeparator()
                + "CS stands for Counter Strike." + System.lineSeparator()
                + "It is part of the OMSCS program. #KEEP" + System.lineSeparator();
        String expected = "1 This course's title is CS6300. #keep#" + System.lineSeparator()
                        + "3 It is part of the OMSCS program. #KEEP#" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-i", "-k", "#keep", "-n", "1", "-s", "#", inputFile.toString()};
        Main.main(args);

        // output matched expected output
        Assertions.assertEquals(expected, capture.stdout());
        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 25: verify processing order -r, -w, -s
    public void textprocessorTest25() throws IOException {
        String input = "This is the old sentence" + System.lineSeparator();
        String expected = "Thisisthenewsentence." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "old", "new", "-w", "-s", ".", inputFile.toString()};
        Main.main(args);

        // output matched expected output
        Assertions.assertEquals(expected, capture.stdout());
        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 26: additional arguments mistaken for parameters to other arguments
    public void exampleTest26() throws Exception {
        String input = "This list contains words that start with -k:" + System.lineSeparator()
                + "-kale" + System.lineSeparator()
                + "-kilo" + System.lineSeparator()
                + "-kite" + System.lineSeparator()
                + "- knot" + System.lineSeparator();
        String expected = "This list contains words that start with -s:" + System.lineSeparator()
                + "-sale" + System.lineSeparator()
                + "-silo" + System.lineSeparator()
                + "-site" + System.lineSeparator()
                + "- knot" + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "-k", "-s", inputFile.toString()};
        Main.main(args);

        // output matched expected output
        Assertions.assertEquals(expected, capture.stdout());
        // no errors
        Assertions.assertTrue(capture.stderr().isEmpty());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }

    @Test // Test Case 27: specify -r with blank <new> parameter
    public void textprocessorTest27() throws IOException {
        String input = "This is the first line of the input file." + System.lineSeparator();

        Path inputFile = createFile(input);
        String[] args = {"-r", "old", "", inputFile.toString()};
        Main.main(args);

        // no standard output
        Assertions.assertTrue(capture.stdout().isEmpty());
        // Error Message
        Assertions.assertEquals(usageStr, capture.stderr());
        // input has not been modified
        Assertions.assertEquals(input, getFileContent(inputFile));
    }
}