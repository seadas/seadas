package gov.nasa.gsfc.seadas.processing.general;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 11/28/12
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnvironmentVariablesExpander {


    public static String getExpandedFilename(String filename) {

        String envVarNamePattern = "([A-Za-z0-9_]+)";

        filename = replaceStringPattern("\\$" + envVarNamePattern, filename);
        filename = replaceStringPattern("\\$\\{" + envVarNamePattern + "\\}", filename);

        return filename;
    }


    private static String replaceStringPattern(String patternString, String filename) {

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(filename);
        Map<String, String> envMap = null;

        while (matcher.find()) {
            // Retrieve environment variables
            if (envMap == null) {
                envMap = System.getenv();
            }

            String envNameOnly = matcher.group(1).toUpperCase();

            if (envMap.containsKey(envNameOnly)) {
                String envValue = envMap.get(envNameOnly);

                if (envValue != null) {
                    envValue = envValue.replace("\\", "\\\\");
                    String envNameClause = matcher.group(0);
                    Pattern envNameClausePattern = Pattern.compile(Pattern.quote(envNameClause));
                    filename = envNameClausePattern.matcher(filename).replaceAll(envValue);
                }
            }
        }

        return filename;
    }
}