/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.obpg.seadas.sandbox.processor;

import java.util.ArrayList;

/**
 *
 * @author dshea
 */
public class LevelOption {

    private ArrayList<String> description = new ArrayList<String>();     // first line is 0
    private String key;
    private String value;
    private String type;
    private int startLine = -1;

    /**
     * constructor
     */
    public void LevelOption() {
    }

    /**
     * reset all values to default values
     */
    public void clear() {
        description.clear();
        key = null;
        value = null;
        type = null;
        startLine = -1;
    }

    /**
     * get the number of lines in the description
     * @return number of lines
     */
    public int getNumLines() {
        return getDescription().size();
    }

    /**
     * Add a line to the description
     * @param line string to add to the description
     */
    public void addLine(String line) {
        getDescription().add(line);
    }

    /**
     *
     * @param num
     * @return
     */
    public boolean containsLine(int num) {
        if (getStartLine() == -1) {
            return false;
        }
        if (getStartLine() > num) {
            return false;
        }
        if ((getStartLine() + getDescription().size()) > num) {
            return true;
        }
        return false;
    }

    /**
     * Check to see if the line given is the first line of an option
     * @param line string to analyze
     * @return true if this is the first line of the option
     */
    public boolean checkLine(String line) {
        String[] words = line.trim().split("\\s+", 3);
        if (words.length > 1) {
            return isDataType(words[1]);
        }
        return false;
    }

    /**
     * Check to see if the string given is a valid data type specifier
     * @param s string to analyze
     * @return true if this is a valid data type
     */
    public static boolean isDataType(String s) {
        String s1 = s.trim();
        if (s1.equals("(bool)")
                || s1.equals("(int)")
                || s1.equals("(float)")
                || s1.equals("(double)")
                || s1.equals("(string)")) {
            return true;
        }
        return false;
    }

    /**
     * parse the first line in the description setting the internal values
     * @return true if all went well
     */
    public boolean parse() {

        try {
            String[] words = getDescription().get(0).trim().split("\\s+");

            // set the key
            key = words[0];

            // set type
            if (words.length > 1) {
                type = words[1].substring(1, words[1].length() - 1);

                // set the value
                int i;
                for (i = 2; i < words.length; i++) {
                    if (words[i].startsWith("(default=")) {
                        value = words[i].substring(9, words[i].length() - 1);
                        break;
                    }
                }
            } // words.length>1

            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

    }

    /**
     * parse the line returned by "-dump_options" to set the value to the
     * real value used.
     * @param line string to parse
     */
    public void parseCurrentValue(String line) {
        String[] words = line.trim().split("\\s+");
        int i;
        for (i = 2; i < words.length; i++) {
            if (words[i].startsWith("(current=")) {
                value = words[i].substring(9, words[i].length() - 1);
                break;
            }
        }
    }

    /**
     * @return the description
     */
    public ArrayList<String> getDescription() {
        return description;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the startLine
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * @param startLine the startLine to set
     */
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }
}
