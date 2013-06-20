package gov.nasa.gsfc.seadas.ocsswws;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/11/13
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainTest {

    @Before
    public void setUp() throws Exception {
        Server.startServer();
    }

    @After
    public void tearDown() throws Exception {
        Server.stopServer();
    }

    /**
     * Test to see that the message "JERSEY HTTPS EXAMPLE" is sent in the response.
     */
    @Test
    public void testSSLWithAuth() {

        TrustManager mytm[] = null;
        KeyManager mykm[] = null;

        try {
            mytm = new TrustManager[]{new MyX509TrustManager(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/truststore_client", "seadas7".toCharArray())};
            mykm = new KeyManager[]{new MyX509KeyManager(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/keystore_client", "seadas7".toCharArray())};
        } catch (Exception ex) {

        }

        SSLContext context = null;

        try {
            context = SSLContext.getInstance("SSL");
            context.init(mykm, mytm, null);
        } catch (NoSuchAlgorithmException nae) {

        } catch (KeyManagementException kme) {

        }

        HTTPSProperties prop = new HTTPSProperties(null, context);

        DefaultClientConfig dcc = new DefaultClientConfig();
        dcc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, prop);

        Client c = Client.create(dcc);

        // client basic auth demonstration
        c.addFilter(new HTTPBasicAuthFilter("user", "password"));

        System.out.println("Client: GET " + Server.BASE_URI);

        WebResource r = c.resource(Server.BASE_URI);

        String page = (String) r.path("/").get(String.class);

        assertEquals(Server.CONTENT, page);
    }

    /**
     *
     * Test to see that HTTP 401 is returned when client tries to GET without
     * proper credentials.
     */
    @Test
    public void testHTTPBasicAuth1() {

        TrustManager mytm[] = null;
        KeyManager mykm[] = null;

        try {
            mytm = new TrustManager[]{new MyX509TrustManager(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/truststore_client", "seadas7".toCharArray())};
            mykm = new KeyManager[]{new MyX509KeyManager(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/keystore_client", "seadas7".toCharArray())};
        } catch (Exception ex) {
            System.out.println("Something bad happened " + ex.getMessage());
        }

        SSLContext context = null;

        try {
            context = SSLContext.getInstance("SSL");
            context.init(mykm, mytm, null);
        } catch (NoSuchAlgorithmException nae) {
            System.out.println("NoSuchAlgorithmException " + nae.getMessage());
        } catch (KeyManagementException kme) {
            System.out.println("KeyManagementException happened " + kme.getMessage());

        }


        HTTPSProperties prop = new HTTPSProperties(null, context);

        DefaultClientConfig dcc = new DefaultClientConfig();
        dcc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, prop);

        Client c = Client.create(dcc);

        WebResource r = c.resource(Server.BASE_URI);

        r.addFilter(new LoggingFilter());

        String msg = null;

        try {
            String page = (String) r.path("/").get(String.class);
        } catch (Exception e) {
            msg = e.getMessage();
        }

        assertTrue(msg.contains("401"));
    }

    /**
     *
     * Test to see that SSLHandshakeException is thrown when client don't have
     * trusted key.
     */
    @Test
    public void testSSLAuth1() {

        TrustManager mytm[] = null;

        try {
            mytm = new TrustManager[]{new MyX509TrustManager(System.getProperty("user.dir") + "/seadas/seadas-ocsswws/truststore_client", "seadas7".toCharArray())};
        } catch (Exception ex) {

            System.out.println("Something bad happened " + ex.getMessage());
            String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                    absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
            System.out.println(System.getProperty("user.dir"));
            System.out.println(absolutePath);
        }

        SSLContext context = null;

        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, mytm, null);
        } catch (NoSuchAlgorithmException nae) {
            System.out.println("NoSuchAlgorithmException " + nae.getMessage());
        } catch (KeyManagementException kme) {
            System.out.println("KeyManagementException happened " + kme.getMessage());
        }

        HTTPSProperties prop = new HTTPSProperties(null, context);

        DefaultClientConfig dcc = new DefaultClientConfig();
        dcc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, prop);

        Client c = Client.create(dcc);

        WebResource r = c.resource(Server.BASE_URI);

        r.addFilter(new LoggingFilter());

        String msg = null;

        boolean caught = false;

        try {
            String page = (String) r.path("/").get(String.class);
        } catch (Exception e) {
            caught = true;
            msg = e.getMessage();
        }

        assertTrue(caught); // solaris throws java.net.SocketException instead of SSLHandshakeException
        // assertTrue(msg.contains("SSLHandshakeException"));
    }

}

/**
 * Taken from http://java.sun.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
 *
 */
class MyX509TrustManager implements X509TrustManager {

     /*
      * The default PKIX X509TrustManager9.  We'll delegate
      * decisions to it, and fall back to the logic in this class if the
      * default X509TrustManager doesn't trust it.
      */
     X509TrustManager pkixTrustManager;

     MyX509TrustManager(String trustStore, char[] password) throws Exception {
         this(new File(trustStore), password);
     }

     MyX509TrustManager(File trustStore, char[] password) throws Exception {
         // create a "default" JSSE X509TrustManager.

         KeyStore ks = KeyStore.getInstance("JKS");
         System.out.println("absolute path: " + trustStore.getAbsolutePath());

         ks.load(new FileInputStream(trustStore), password);

         TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
         tmf.init(ks);

         TrustManager tms [] = tmf.getTrustManagers();

         /*
          * Iterate over the returned trustmanagers, look
          * for an instance of X509TrustManager.  If found,
          * use that as our "default" trust manager.
          */
         for (int i = 0; i < tms.length; i++) {
             if (tms[i] instanceof X509TrustManager) {
                 pkixTrustManager = (X509TrustManager) tms[i];
                 return;
             }
         }

         /*
          * Find some other way to initialize, or else we have to fail the
          * constructor.
          */
         throw new Exception("Couldn't initialize");
     }

     /*
      * Delegate to the default trust manager.
      */
     public void checkClientTrusted(X509Certificate[] chain, String authType)
                 throws CertificateException {
         try {
             pkixTrustManager.checkClientTrusted(chain, authType);
         } catch (CertificateException excep) {
             // do any special handling here, or rethrow exception.
         }
     }

     /*
      * Delegate to the default trust manager.
      */
     public void checkServerTrusted(X509Certificate[] chain, String authType)
                 throws CertificateException {
         try {
             pkixTrustManager.checkServerTrusted(chain, authType);
         } catch (CertificateException excep) {
             /*
              * Possibly pop up a dialog box asking whether to trust the
              * cert chain.
              */
         }
     }

     /*
      * Merely pass this through.
      */
     public X509Certificate[] getAcceptedIssuers() {
         return pkixTrustManager.getAcceptedIssuers();
     }
}

/**
 * Inspired from http://java.sun.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
 *
 */
class MyX509KeyManager implements X509KeyManager {

     /*
      * The default PKIX X509KeyManager.  We'll delegate
      * decisions to it, and fall back to the logic in this class if the
      * default X509KeyManager doesn't trust it.
      */
     X509KeyManager pkixKeyManager;

     MyX509KeyManager(String keyStore, char[] password) throws Exception {
         this(new File(keyStore), password);
     }

     MyX509KeyManager(File keyStore, char[] password) throws Exception {
         // create a "default" JSSE X509KeyManager.

         KeyStore ks = KeyStore.getInstance("JKS");
         ks.load(new FileInputStream(keyStore), password);

         KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
         kmf.init(ks, "seadas7".toCharArray());

         KeyManager kms[] = kmf.getKeyManagers();

         /*
          * Iterate over the returned keymanagers, look
          * for an instance of X509KeyManager.  If found,
          * use that as our "default" key manager.
          */
         for (int i = 0; i < kms.length; i++) {
             if (kms[i] instanceof X509KeyManager) {
                 pkixKeyManager = (X509KeyManager) kms[i];
                 return;
             }
         }

         /*
          * Find some other way to initialize, or else we have to fail the
          * constructor.
          */
         throw new Exception("Couldn't initialize");
     }

    public PrivateKey getPrivateKey(String arg0) {
        return pkixKeyManager.getPrivateKey(arg0);
    }

    public X509Certificate[] getCertificateChain(String arg0) {
        return pkixKeyManager.getCertificateChain(arg0);
    }

    public String[] getClientAliases(String arg0, Principal[] arg1) {
        return pkixKeyManager.getClientAliases(arg0, arg1);
    }

    public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
        return pkixKeyManager.chooseClientAlias(arg0, arg1, arg2);
    }

    public String[] getServerAliases(String arg0, Principal[] arg1) {
        return pkixKeyManager.getServerAliases(arg0, arg1);
    }

    public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
        return pkixKeyManager.chooseServerAlias(arg0, arg1, arg2);
    }
}

