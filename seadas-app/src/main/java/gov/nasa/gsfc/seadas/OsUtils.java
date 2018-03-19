package gov.nasa.gsfc.seadas;

import java.util.Locale;

/**
 * Created by aabduraz on 3/27/17.
 */
public final class OsUtils {
    /**
     * types of Operating Systems
     */
    public enum OSType {
        Windows, MacOS, Linux, Other
    }

    ;

    // cached result of OS detection
    protected static OSType detectedOS;

    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @returns - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                detectedOS = OSType.MacOS;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = OSType.Windows;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }
    public static String[] getCopyCommandSyntax(){
        String[] copyCommandSyntaxArray = {"cp"};
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (OS.indexOf("win") >= 0) {
            copyCommandSyntaxArray = new String[3];
            copyCommandSyntaxArray[0] = "cmd.exe";
            copyCommandSyntaxArray[1] = "/C";
            copyCommandSyntaxArray[2] = "copy";
        }
        return  copyCommandSyntaxArray;
    }
}
