package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC.PROCESS_TABLE_NAME;

/**
 * Created by aabduraz on 3/4/16.
 */

@Path("/process")
public class ProcessServices {

    private final String stdout = "stdout";
    private final String stderr = "stderr";

    @GET
    @Path("/stdout/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessInputStream(@PathParam("jobId") String jobId) {
        //TODO: get process input stream from the running process!
        System.out.println("reached to grep standard output.");
        String stdoutString = SQLiteJDBC.retrieveItem(PROCESS_TABLE_NAME, jobId, stdout);
        return stdoutString == null ? "done!"  :stdoutString;
    }

    @GET
    @Path("/stderr/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessErrorStream(@PathParam("jobId") String jobId) {
        //TODO: get process error stream from the running process!
        System.out.println("reached to grep standard error.");
        String stderrString = SQLiteJDBC.retrieveItem(PROCESS_TABLE_NAME, jobId, stderr);
        return stderrString == null ? "done!" : stderrString;
    }


}
