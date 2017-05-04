package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/18/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Job {

    private String jobID;
    private boolean active;
    private String jobDir;

    public Job(){
        jobID = generateJobID();
        active = true;
        jobDir = null;
    }

    public String generateJobID(){
      return hashJobID(new Long(new Date().getTime()).toString());
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getJobDir() {
        return jobDir;
    }

    public void setJobDir(String jobDir) {
        this.jobDir = jobDir;
    }

    private String hashJobID(String jobID) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        md.update(jobID.getBytes());

        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
