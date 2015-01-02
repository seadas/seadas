package gov.nasa.gsfc.seadas.ocsswws.resource;

import gov.nasa.gsfc.seadas.ocsswws.Server;
import gov.nasa.gsfc.seadas.ocsswws.utilities.Job;
import gov.nasa.gsfc.seadas.ocsswws.utilities.ServerSideFileUtilities;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/18/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/jobs")
public class JobResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response createNewJob(
            @DefaultValue("0") @QueryParam("clienId") String clientId) {

        //create new jobID
        Job newJob = new Job();
        String latestJobID = newJob.getJobID();
        String newJobID = ServerSideFileUtilities.generateNewJobID();
        if (ServerSideFileUtilities.makeNewJobDirectory(clientId, newJobID)) {
            ArrayList<String> jobList;
            if (Server.jobMap.containsKey(clientId)) {
                jobList = Server.jobMap.get(clientId);
            } else {
                jobList = new ArrayList<String>();
                Server.jobMap.put(clientId, jobList);
            }

            // add the new job to the list of jobs of the client
            jobList.add(newJobID);

            // make new directory for the new job

        }
        return Response.ok(newJobID).build();
    }

//    @GET
//    @Path("/{clientId}/{jobId}/{processorId}")
//    public Response getCurrentJobID(@QueryParam("clienId") String clientId,
//                                    @QueryParam("clienId") String jobId,
//                                    @QueryParam("clienId") String processorId) {
//
//        ArrayList<String> jobList = new ArrayList<String>();
//        if (Server.jobMap.containsKey(clientId)) {
//            jobList = Server.jobMap.get(clientId);
//        }
//
//        for (String job : jobList) {
//        }
//
//        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
//    }

    @GET
    @Path("/job{jobId}")
    public Response getJobStatus(@QueryParam("clienId") String clientId) {

        ArrayList<String> jobList = new ArrayList<String>();
        if (Server.jobMap.containsKey(clientId)) {
            jobList = Server.jobMap.get(clientId);
        }

        for (String job : jobList) {

        }

        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }


}
