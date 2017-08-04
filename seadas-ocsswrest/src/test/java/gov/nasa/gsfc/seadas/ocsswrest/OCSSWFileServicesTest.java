package gov.nasa.gsfc.seadas.ocsswrest;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by aabduraz on 6/12/17.
 */
public class OCSSWFileServicesTest {
    @Test
    public void uploadClientFile() throws Exception {
        OCSSWFileServices ocsswFileServices = new OCSSWFileServices();
        File file = new File("/accounts/aabduraz/Downloads/A2011199230500.L1A_LAC");
        InputStream inputStream = new FileInputStream(file);
        final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file", file);
        final MultiPart multiPart = new FormDataMultiPart()
                //.field("ifileName", ifileName)
                .bodyPart(fileDataBodyPart);
        ocsswFileServices.uploadClientFile("2", inputStream, fileDataBodyPart.getFormDataContentDisposition());
    }

    @Before
    public void setUp() throws Exception {
//        OCSSWConfig ocsswConfig = new OCSSWConfig();
//        ocsswConfig.readProperties();
//        OCSSWServerModel.initiliaze();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void downloadFile() throws Exception {

        OCSSWFileServices ocsswFileServices = new OCSSWFileServices();
        ocsswFileServices.downloadFile("e3111428287d67a772eeb58946ae1bee");
    }

}