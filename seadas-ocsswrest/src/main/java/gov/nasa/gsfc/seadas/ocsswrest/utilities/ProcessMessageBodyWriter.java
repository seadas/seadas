package gov.nasa.gsfc.seadas.ocsswrest.utilities;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by aabduraz on 5/28/15.
 */
@Produces("application/octet-stream")
public class ProcessMessageBodyWriter implements MessageBodyWriter<Process> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return type == Process.class;
    }

    @Override
    public long getSize(Process process, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return 0;
    }

    @Override
    public void writeTo(Process process,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream)
            throws WebApplicationException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Process.class);

            // serialize the entity myBean to the entity output stream
            jaxbContext.createMarshaller().marshal(process, entityStream);
        } catch (JAXBException jaxbException) {
            throw new ProcessingException(
                    "Error serializing a Process to the output stream", jaxbException);
        }
    }
}
