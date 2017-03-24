package gov.nasa.gsfc.seadas.processing.core;

/**
 * Created by aabduraz on 3/24/17.
 */
public abstract class OCSSWEnv {
    public abstract boolean isOCSSWExist();
    public abstract String getOcsswDataRoot();
    public abstract String getOcsswScriptPath();
    public abstract String getOcsswRunnerScriptPath();
    public abstract String getOcsswInstallerScriptPath();
}

