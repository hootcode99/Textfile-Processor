package textprocessor;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;

@SuppressWarnings("DuplicatedCode")
public class TextProcessor implements TextProcessorInterface {
    private static ArrayList<String> fileLines = new ArrayList<>();
    private static ArrayList<String> updatedLines = new ArrayList<>();
    private static HashMap<String, String> actionsMap = new HashMap<String, String>();

    public TextProcessor() {
        actionsMap.put("o", "");
        actionsMap.put("i", "");
        actionsMap.put("k", "");
        actionsMap.put("r", "");
        actionsMap.put("n", "");
        actionsMap.put("w", "");
        actionsMap.put("s", "");
        actionsMap.put("inFilePath", "");
        actionsMap.put("outFilePath", "");
        actionsMap.put("substring", "");
        actionsMap.put("oldString", "");
        actionsMap.put("newString", "");
        actionsMap.put("suffix", "");
        actionsMap.put("padding", "");
    }

    public void reset() {
        fileLines.clear();
        updatedLines.clear();
        actionsMap.clear();
    }

    public void setFilepath(String filepath) {
        actionsMap.put("inFilePath", filepath);
    }

    public void setOutputFile(String outputFile) {
        actionsMap.put("o", "present");
        actionsMap.put("outFilePath", outputFile);
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        actionsMap.put("i", "present");
    }

    public void setKeepLines(String keepLines) {
        actionsMap.put("k", "present");
        actionsMap.put("substring", keepLines);
    }

    public void setReplaceText(String oldString, String newString) {
        actionsMap.put("r", "present");
        actionsMap.put("oldString", oldString);
        actionsMap.put("newString", newString);
    }

    public void setAddPaddedLineNumber(int padding) {
        actionsMap.put("n", "present");
        actionsMap.put("padding", String.valueOf(padding));
    }

    public void setRemoveWhitespace(boolean removeWhitespace) {
        if (removeWhitespace) {
            actionsMap.put("w", "present");
        }
    }

    public void setSuffixLines(String suffixLines) {
        actionsMap.put("s", "present");
        actionsMap.put("suffix", suffixLines);
    }

    public void textprocessor() throws TextProcessorException {
        // read in the lines of the file
        readFile(fileLines);

        // check arguments
        if (!validateParameters(actionsMap)) {
            throw new TextProcessorException("1 or more parameters is missing or invalid");
        }

        // For each line in the file, check if the flags are provided and perform the appropriate operations
        for (String line: fileLines) {
            String newLine = line;
            updatedLines.add(line);

            // -k Flag
            if (actionsMap.get("k").equals("present")) {
                String substring = actionsMap.get("substring");
                String lowerSubstring = substring.toLowerCase();
                String lowerLine = line.toLowerCase();
                // case insensitive
                if (actionsMap.get("i").equals("present")) {
                    if (!(lowerLine.contains(lowerSubstring))) {
                        updatedLines.remove(line);
                        continue;
                    }
                // case sensitive
                } else {
                    if (!(line.contains(substring))) {
                        updatedLines.remove(line);
                        continue;
                    }
                }
            }

            // -r Flag
            if (actionsMap.get("r").equals("present")) {
                // get the old substring and the new string to replace it with
                String oldString = actionsMap.get("oldString");
                String newString = actionsMap.get("newString");
                // get the lowercase version of the old substring and the line
                String lowerOld = oldString.toLowerCase();
                String lowerLine = line.toLowerCase();
                // filter special characters in the string
                String filteredOld = filter(oldString);

                // case-insensitive
                if (actionsMap.get("i").equals("present")) {
                    // check if the line contains the substring (case-insensitive)
                    if (lowerLine.contains(lowerOld)) {
                        // set the output line to a newline with the first instance the old substring replaced
                        newLine = newLine.replaceFirst("(?i)" + filteredOld, newString);
                    }
                // case sensitive
                } else {
                    // check if the line contains the substring
                    if (line.contains(oldString)) {
                        // set the output line to a newline with the first instance the old substring replaced
                        newLine = newLine.replaceFirst(filteredOld, newString);
                    }
                }
            }

            // -n Flag
            if (actionsMap.get("n").equals("present")) {
                String paddingValue = actionsMap.get("padding");

                // check if the padding is within the appropriate values
                int testerValue = Integer.parseInt(paddingValue);
                if (testerValue > 9 || testerValue < 1) {
                    throw new TextProcessorException("Padding Value Out of Range");
                }

                String paddingFormatter = "%0" + paddingValue + "d";
                newLine = String.format(paddingFormatter, fileLines.indexOf(line) + 1) + " " + newLine;
            }

            // -w Flag
            if (actionsMap.get("w").equals("present")) {
                newLine = newLine.replaceAll("\\s", "");
            }

            // -s Flag
            if (actionsMap.get("s").equals("present")) {
                String suffix = actionsMap.get("suffix");
                newLine = newLine + suffix;
            }

            // add the changes to the output list
            updatedLines.set(updatedLines.indexOf(line), newLine);
        }

        // If output file is specified
        if (actionsMap.get("o").equals("present")) {
            try {
                // create the output file
                File outFile = new File(actionsMap.get("outFilePath"));
                if (!outFile.createNewFile()) {
                    // if the file already exists
                    throw new TextProcessorException("File Already Exists");
                }

                // create a filewriter object and write to the file
                FileWriter fw = new FileWriter(outFile);
                for (String updatedLine : updatedLines) {
                    fw.write(updatedLine + System.lineSeparator());
                }
                fw.close();
                reset();

            } catch (IOException e) {
                throw new TextProcessorException("Error Writing to Output File");
            }

        // otherwise write to the standard output
        } else {
            for (String updatedLine: updatedLines) {
                System.out.println(updatedLine);
            }
            reset();
        }
    }

    private void readFile(ArrayList<String> fileLines) throws TextProcessorException {
        try {
            File inFile = new File(actionsMap.get("inFilePath"));
            if (inFile.length() > 0) {
                // Check for newline at the end of file
                byte[] fileBytes = Files.readAllBytes(inFile.toPath());
                byte[] lineSeps = System.lineSeparator().getBytes();
                String lastElement = String.valueOf(fileBytes[fileBytes.length - 1]);
                String newline = String.valueOf(lineSeps[lineSeps.length - 1]);
                if (!lastElement.equals(newline)){
                    throw new TextProcessorException("Last file element is not new line");
                }
            }
            // read the file lines
            Scanner fileReader = new Scanner(inFile);
            while (fileReader.hasNextLine()) {
                fileLines.add(fileReader.nextLine());
            }
            fileReader.close();

        } catch (IOException e) {
            throw new TextProcessorException("Error reading the file");
        }
    }

    private boolean validateParameters(HashMap<String, String> actionsMap){
        // i without k or r
        if (actionsMap.get("i").equals("present")){
            if (actionsMap.get("k").equals("") && actionsMap.get("r").equals("")) {
                return false;
            }
            // w and n
        } else if (actionsMap.get("w").equals("present") && actionsMap.get("n").equals("present")) {
            return false;
            // k and r
        } else if (actionsMap.get("k").equals("present") && actionsMap.get("r").equals("present")) {
            return false;
            // r flag is present but the parameters are empty
        } else if (actionsMap.get("r").equals("present")) {
            if (actionsMap.get("oldString").equals("") || actionsMap.get("oldString").equals("")) {
                return false;
            }
            // s flag is present but the parameters are empty
        } else if (actionsMap.get("s").equals("present")) {
            if (actionsMap.get("suffix").equals("")) {
                return false;
            }
        }

        // no errors
        return true;
    }

    private String filter(String oldString){
        String specials = "[\\[+\\]+:{}_^*.~?\\\\/()><=\"!]";
        StringBuilder builder = new StringBuilder();

        for (char character: oldString.toCharArray()) {
            if (specials.contains(String.valueOf(character))) {
                builder.append("\\").append(character);
            } else {
                builder.append(character);
            }
        }

        return builder.toString();
    }
}
