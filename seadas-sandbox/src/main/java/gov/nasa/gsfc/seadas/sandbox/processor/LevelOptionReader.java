/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.gsfc.seadas.sandbox.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author dshea
 */
public class LevelOptionReader {

    private ArrayList<LevelOption> optionList = new ArrayList<LevelOption>();
    private String progName;
    private String progVersion;

    LevelOptionReader(String progName) {
        this.progName = progName;
    }

    public void clear() {
        getOptionList().clear();
    }

    public boolean loadOptions() {

        OCSSWClient client = new OCSSWClient();
        String s = null;
        int currentLine = 1;

        try {
            BufferedReader helpText = client.getHelpText(getProgName());

            if(helpText == null) {
                return false;
            }
            // The first line is the program version
            progVersion = helpText.readLine();
            if(progVersion == null) {
                return false;
            }

            LevelOption option = new LevelOption();

            // throw away all lines until we get to an option description
            while ((s = helpText.readLine()) != null) {
                if (option.checkLine(s)) {
                    break;
                }
            }

            if(s == null) {
                return false;
            }

            // add the line we just checked
            option.addLine(s);

            // add all of the options
            while ((s = helpText.readLine()) != null) {
                if (option.checkLine(s)) {
                    // we found a new option
                    if (option.parse()) {
                        if (option.getKey().equals("-help")
                                || option.getKey().equals("-version")
                                || option.getKey().equals("-dump_options")
                                || option.getKey().equals("ifile")
                                || option.getKey().equals("ilist")
                                || option.getKey().equals("geofile")
                                || option.getKey().equals("ofile")) {
                            // skip these options
                            option.clear();
                            option.addLine(s);
                        } else {
                            option.setStartLine(currentLine);
                            currentLine += option.getNumLines();
                            getOptionList().add(option);
                            option = new LevelOption();
                            option.addLine(s);
                        }
                    } else {
                        // parse failed
                        option.clear();
                        option.addLine(s);
                    }
                } else {
                    // more option description lines
                    option.addLine(s);
                }

            } // while lines

            // add the last option if valid
            if(option.getNumLines() > 0) {
                if (option.parse()) {
                    option.setStartLine(currentLine);
                    getOptionList().add(option);
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * @return the optionList
     */
    public ArrayList<LevelOption> getOptionList() {
        return optionList;
    }

    /**
     * @return the progName
     */
    public String getProgName() {
        return progName;
    }

    /**
     * @param progName the progName to set
     */
    public void setProgName(String progName) {
        this.progName = progName;
    }

    /**
     * @return the progVersion
     */
    public String getProgVersion() {
        return progVersion;
    }
}
