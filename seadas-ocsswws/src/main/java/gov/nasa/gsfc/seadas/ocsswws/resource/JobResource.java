package gov.nasa.gsfc.seadas.ocsswws.resource;

import com.sun.jersey.core.util.Base64;
import gov.nasa.gsfc.seadas.ocsswws.Server;
import gov.nasa.gsfc.seadas.ocsswws.utilities.Job;
import gov.nasa.gsfc.seadas.ocsswws.utilities.ServerSideFileUtilities;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/18/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("/jobs")
public class JobResource {
    @Context
        SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

//    public RootResource(@Context SecurityContext securityContext) {
//            // this is ok too: the proxy of SecurityContext will be injected
//        }
    @GET
    public Response get1(@Context HttpHeaders headers) {
        // you can get username form HttpHeaders
        System.out.println("Service: GET / User: " + getUser(headers));
         headers.getRequestHeaders();
        Properties systemProperties = System.getProperties();
        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }

    private String getUser(HttpHeaders headers) {

        // this is a very minimalistic and "naive" code; if you plan to use it
        // add necessary checks (see com.sun.jersey.samples.https_grizzly.auth.SecurityFilter)
        String auth = headers.getRequestHeader("authorization").get(0);

        auth = auth.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(auth)).split(":");

        // String username = values[0];
        // String password = values[1];

        return values[0];
    }

    @POST
    public Response createNewJob(@QueryParam("clienId") String clientId) {

        //create new jobID
        Job newJob = new Job();
        String latestJobID = newJob.getJobID();
        ArrayList<Job> jobList;
        if (Server.jobMap.containsKey(clientId)) {
           jobList = Server.jobMap.get(clientId);
        } else {
            jobList = new ArrayList<Job>();
            Server.jobMap.put(clientId, jobList);
        }

        // add the new job to the list of jobs of the client
        jobList.add(newJob);

        // set the latest job status as active; set all others to false
        for (Job job : jobList) {
            if (job.getJobID().equals(latestJobID)) {
                job.setActive(true);
            } else {
                job.setActive(false);
            }
        }

        // make new directory for the new job

        ServerSideFileUtilities.makeNewJobDirectory(latestJobID);

        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }

    @GET @Path("/job{clientId}")
    public Response getCurrentJobID(@QueryParam("clienId") String clientId) {

        ArrayList<Job> jobList = new ArrayList<Job>();
        if (Server.jobMap.containsKey(clientId)) {
           jobList = Server.jobMap.get(clientId);
        }

        for (Job job : jobList) {
             if (job.isActive() ) {

             }
        }

        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }

    @GET @Path("/job{jobId}")
    public Response getJobStatus(@QueryParam("clienId") String clientId) {

        ArrayList<Job> jobList = new ArrayList<Job>();
        if (Server.jobMap.containsKey(clientId)) {
           jobList = Server.jobMap.get(clientId);
        }

        for (Job job : jobList) {
             if (job.isActive() ) {

             }
        }

        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }


}
