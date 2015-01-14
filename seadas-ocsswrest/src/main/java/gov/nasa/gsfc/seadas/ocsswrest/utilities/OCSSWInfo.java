package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/9/15
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class OCSSWInfo {
    private boolean installed;
    private String ocsswDir;

    public boolean isInstalled(){
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getOcsswDir(){
        return ocsswDir;
    }

    public void setOcsswDir(String ocsswDir) {
        this.ocsswDir = ocsswDir;
    }

}
