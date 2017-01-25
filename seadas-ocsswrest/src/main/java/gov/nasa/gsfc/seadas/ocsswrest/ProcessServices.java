package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.database.SQLiteJDBC;
import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerModel;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aabduraz on 3/4/16.
 */

@Path("/process")
public class ProcessServices {

    private final String PROCESSOR_TABLE_NAME = "PROCESSOR_TABLE";
    private final String stdout = "stdout";
    private final String stderr = "stderr";

    @GET
    @Path("/stdout/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessInputStream(@PathParam("jobId") String jobId) {
        //TODO: get process input stream from the running process!
        System.out.println("reached to grep standard output.");
        String stdoutString = SQLiteJDBC.retrieveItem(PROCESSOR_TABLE_NAME, jobId, stdout);
        return stdoutString == null ? "done!"  :stdoutString;
    }

    @GET
    @Path("/stderr/{jobId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessErrorStream(@PathParam("jobId") String jobId) {
        //TODO: get process error stream from the running process!
        System.out.println("reached to grep standard error.");
        String stderrString = SQLiteJDBC.retrieveItem(PROCESSOR_TABLE_NAME, jobId, stderr);
        return stderrString == null ? "done!" : stderrString;
    }


}
