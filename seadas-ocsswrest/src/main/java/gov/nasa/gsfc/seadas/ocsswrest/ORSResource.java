package gov.nasa.gsfc.seadas.ocsswrest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

/**
 * Created by aabduraz on 8/25/17.
 */
public class ORSResource {

    @Context
    private Application application;

    @GET
    @Path("")
    public String getOcsswRoot() {
        String ocsswRoot = String.valueOf(application.getProperties().get("ocsswroot"));
        return ocsswRoot;
    }
}
