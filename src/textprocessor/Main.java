package textprocessor;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Iterator;

import static java.util.regex.Pattern.quote;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> fileLines = new ArrayList<>();
        ArrayList<String> updatedLines = new ArrayList<>();
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
        HashMap<String, String> actionsMap;
        String outFileName = "";
        boolean outToFile = false;
        boolean caseInsensitive = false;

        // if there are arguments, populate the hashmap
        if (arguments.size() > 0) {
            actionsMap = parseArguments(arguments);
            if (actionsMap.get("error").equals("true")){
                usage();
                return;
            }
        } else {
            usage();
            return;
        }

        // basic eval checks
        if (!validateArguments(actionsMap)) {
            usage();
            return;
        }

        // get the last argument as the input file and place the lines in an array
        try {
            File inFile = new File(actionsMap.get("inFilename"));
            if (inFile.length() > 0) {
                // Check for newline at the end of file
                byte[] fileBytes = Files.readAllBytes(inFile.toPath());
                byte[] lineSeps = System.lineSeparator().getBytes();
                String lastElement = String.valueOf(fileBytes[fileBytes.length - 1]);
                String newline = String.valueOf(lineSeps[lineSeps.length - 1]);
                if (!lastElement.equals(newline)){
                    usage();
                    return;
                }
            }
            // read the file lines
            Scanner fileReader = new Scanner(inFile);
            while (fileReader.hasNextLine()) {
                fileLines.add(fileReader.nextLine());
            }
            fileReader.close();

        } catch (IOException e) {
            usage();
        }

        // -o Flag
        if (actionsMap.get("o").equals("present")) {
            outToFile = true;
            // if the output file is blank or the empty string, throw an error
            if (actionsMap.get("filename").equals(" ") || actionsMap.get("filename").equals("")) {
                usage();
                return;
            }
            // if the output file is the input file, throw an error
            if (actionsMap.get("filename").equals(actionsMap.get("inFilename"))) {
                usage();
                return;
            }
            outFileName = actionsMap.get("filename");;
            // create the output file
            try {
                File outFile = new File(outFileName);
                if (!outFile.createNewFile()) {
                    usage(); // if the file already exists
                }
            } catch (IOException e) {
                usage();
                return;
            }
        }

        // -i Flag
        if (actionsMap.get("i").equals("present")) {
            caseInsensitive = true;
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

                if (caseInsensitive) {
                    if (!(lowerLine.contains(lowerSubstring))) {
                        updatedLines.remove(line);
                        continue;
                    }
                } else {
                    if (!(line.contains(substring))) {
                        updatedLines.remove(line);
                        continue;
                    }
                }
            }

            // -r Flag
            if (actionsMap.get("r").equals("present")) {
                String oldString = actionsMap.get("oldString");
                String newString = actionsMap.get("newString");
                String lowerOld = oldString.toLowerCase();
                String lowerLine = line.toLowerCase();

                //  catch error if parameters are missing entirely
                if (oldString.equals(actionsMap.get("inFilename")) || newString.equals(actionsMap.get("inFilename"))) {
                    usage();
                    return;
                }

                // catch error of blank old/new string
                if (oldString.equals("") || newString.equals("")) {
                    usage();
                    return;
                }

                if (caseInsensitive) {
                    if (lowerLine.contains(lowerOld)) {
                        newLine = newLine.replaceFirst("(?i)" + quote(oldString), newString);
                    }
                } else {
                    if (line.contains(oldString)) {
                        newLine = newLine.replaceFirst(oldString, newString);
                    }
                }
            }

            // -n Flag
            if (actionsMap.get("n").equals("present")) {
                String paddingValue = actionsMap.get("padding");
                int testerValue;

                //  catch error if parameters are missing entirely
                if (paddingValue.equals(actionsMap.get("inFilename"))) {
                    usage();
                    return;
                }

                // check if the padding value is not an integer
                try {
                    testerValue = Integer.parseInt(paddingValue);
                } catch (NumberFormatException e) {
                    usage();
                    return;
                }

                // check if the padding is within the appropriate values
                if (testerValue > 9 || testerValue < 1) {
                    usage();
                    return;
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

                //  catch error if parameters are missing entirely
                // if (suffix.equals(inputFileName)) {
                //    usage();
                //     return;
                //  }

                // check if suffix is an empty string
                if (suffix.equals("")) {
                    usage();
                    return;
                }
                newLine = newLine + suffix;
            }
            // add the changes to the output list
            updatedLines.set(updatedLines.indexOf(line), newLine);
        }

        // file out
        if (outToFile) {
            try {
                FileWriter fw = new FileWriter(outFileName);
                for (String updatedLine : updatedLines) {
                    fw.write(updatedLine + System.lineSeparator());
                }
                fw.close();
            } catch (IOException e) {
                usage();
            }
            // stdout
        } else {
            for (String updatedLine: updatedLines) {
                System.out.println(updatedLine);
            }
        }
    }

    private static boolean validateArguments(HashMap<String, String> actionsMap){
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
        }
        // no errors
        return true;
    }

    private static HashMap<String, String> parseArguments(ArrayList<String> arguments) {
        HashMap<String, String> actionsMap = new HashMap<>();
        String inFileName = arguments.get(arguments.size() - 1);
        Iterator<String> iter = arguments.listIterator();

        actionsMap.put("o", "");
        actionsMap.put("i", "");
        actionsMap.put("k", "");
        actionsMap.put("r", "");
        actionsMap.put("n", "");
        actionsMap.put("w", "");
        actionsMap.put("s", "");
        actionsMap.put("inFilename", inFileName);
        actionsMap.put("outFilename", "");
        actionsMap.put("substring", "");
        actionsMap.put("oldString", "");
        actionsMap.put("newString", "");
        actionsMap.put("suffix", "");
        actionsMap.put("padding", "");
        actionsMap.put("error", "");

        while(iter.hasNext()) {
            String arg = iter.next();
            switch (arg) {
                case "-o":
                    // if the flag has not been set, set the flag
                    if (!actionsMap.get("o").equals("present")) {

                        actionsMap.put("o", "present");
                    }
                    /* 
                    remove the item from the arguments list
                    capture the filename as the next element
                    add the filename to the hashmap
                    remove the filename from the arguments list
                    break the switch statement
                    */

                    iter.remove();
                    String filename = iter.next(); 
                    actionsMap.put("filename", filename);
                    iter.remove();
                    break;

                case "-i":
                    actionsMap.put("i", "present");
                    iter.remove();
                    break;

                case "-r":
                    // if the flag has not been set, set the flag
                    if (!actionsMap.get("r").equals("present")) {
                        actionsMap.put("r", "present");
                    }

                    /* 
                    remove the item from the arguments list
                    capture the new string as the next element
                    add the old string to the hashmap
                    remove the old string from the arguments list

                    capture the new string as the next element
                    add the new string to the hashmap
                    remove the new string from the arguments list
                    break the switch statement
                    */

                    iter.remove();
                    String oldString = iter.next();
                    actionsMap.put("oldString", oldString);
                    iter.remove();

                    String newString = iter.next();
                    actionsMap.put("newString", newString);
                    iter.remove();
                    break;

                case "-k":
                    // if the flag has not been set, set the flag
                    if (!actionsMap.get("k").equals("present")) {
                        actionsMap.put("k", "present");
                    }

                    /*
                    remove the item from the arguments list
                    capture the substring as the next element
                    add the substring to the hashmap
                    remove the substring from the arguments list
                    break the switch statement
                    */

                    iter.remove();
                    String substring = iter.next();
                    actionsMap.put("substring", substring);
                    iter.remove();

                    break;

                case "-s":
                    // if the flag has not been set, set the flag
                    if (!actionsMap.get("s").equals("present")) {
                        actionsMap.put("s", "present");
                    }

                    /* 
                    remove the item from the arguments list
                    capture the suffix as the next element
                    add the padding to the hashmap
                    remove the padding from the arguments list
                    break the switch statement
                    */

                    iter.remove();
                    String suffix = iter.next();
                    actionsMap.put("suffix", suffix);
                    iter.remove();
                    break;

                case "-n":
                    // if the flag has not been set, set the flag
                    if (!actionsMap.get("n").equals("present")) {
                        actionsMap.put("n", "present");
                    }

                    /*  
                    remove the item from the arguments list
                    capture the padding as the next element
                    add the padding to the hashmap
                    remove the padding from the arguments list
                    break the switch statement
                    */

                    iter.remove();
                    String padding = iter.next();
                    actionsMap.put("padding", padding);                
                    iter.remove();                    
                    break;

                case "-w":
                    // if the flag has not been set
                    if (!actionsMap.get("w").equals("present")) {
                        // set the flag
                        actionsMap.put("w", "present");
                    }
                    iter.remove();
                    break;

                default:
                    if (!arg.equals(inFileName)) {
                        actionsMap.put("error", "true");
                    }
                    iter.remove();
                    break;
            }
        }
        return actionsMap;
    }
    private static void usage() {
        System.err.println("Usage: textprocessor [ -o filename | -i | -k substring | -r old new | -n padding | -w | -s suffix ] FILE");
    }
}
