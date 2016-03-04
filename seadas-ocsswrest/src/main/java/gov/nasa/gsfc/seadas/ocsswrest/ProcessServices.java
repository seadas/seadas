package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.utilities.OCSSWServerModel;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * Created by aabduraz on 3/4/16.
 */

@Path("/process")
public class ProcessServices {

    @GET
    @Path("/inputStream")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessInputStream() {
        //TODO: get process input stream from the running process!
        return "process input stream";
    }

    @GET
    @Path("/errorStream")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProcessErrorStream() {
        //TODO: get process error stream from the running process!
        return "error stream";
    }
}
