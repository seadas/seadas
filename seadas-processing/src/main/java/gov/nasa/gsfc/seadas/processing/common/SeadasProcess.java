package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSWClient;

import javax.ws.rs.client.WebTarget;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by aabduraz on 6/11/15.
 */

public class SeadasProcess extends Process {
    int exitValue = 1;
    private InputStream inputStream;
    private InputStream errorStream;
    private OutputStream outputStream;
    int waitFor;
    OCSSWInfo ocsswInfo;
    WebTarget target;
    String jobId;

    public SeadasProcess(OCSSWInfo ocsswInfo, String jobId){
        super();
        this.ocsswInfo = ocsswInfo;
        this.jobId = jobId;
    }
    public void destroy(){

    }
    public int exitValue(){
        return exitValue;
    }

    public void setExitValue(int exitValue){
        this.exitValue = exitValue;
    }

    @Override
    public InputStream getErrorStream(){
      return null;
    }


    @Override
    public InputStream getInputStream() {
        return null;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setErrorStream(InputStream errorStream) {
        this.errorStream = errorStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public int waitFor(){
        return waitFor;
    }
}
