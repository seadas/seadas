/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.obpg.seadas.sandbox.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author dshea
 */
public class OCSSWClient {

    public BufferedReader getHelpText(String progName) {

        try {

            Process p = Runtime.getRuntime().exec(progName + " -help");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            return stdInput;

        } catch (IOException e) {
            return null;
        }

    }

      public BufferedReader getDefaultValues(String progName) {

        try {

            Process p = Runtime.getRuntime().exec(progName + " -dump_options");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            return stdInput;

        } catch (IOException e) {
            return null;
        }

    }


}
