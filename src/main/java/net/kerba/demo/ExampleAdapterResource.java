package net.kerba.demo;

import com.ibm.mfp.adapter.api.OAuthSecurity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * @user: kerb
 * @created: 24/07/2017.
 */
@Path("/payment")
public class ExampleAdapterResource {

    private final static Logger logger = Logger.getLogger(ExampleAdapterApplication.class.getName());

    // configure host to proxy requests to
    private static final HttpHost PROXY_TO_HOST = new HttpHost("httpbin.org", 80, "http");

    @POST
    @OAuthSecurity(enabled = false)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mobile")
    public void process(@Context HttpServletResponse response,
                        String requestPostBody,
                        @QueryParam("cmd") String cmd,
                        @QueryParam("pid") String pid,
                        @QueryParam("name") String name) {
        try {
            URI uri = buildProxyURI(cmd, pid, name);

            // we'll do a POST request to backend
            HttpPost request = new HttpPost(uri);

            addSpecificHttpHeaders(request);

            StringEntity postParameters = new StringEntity(requestPostBody, ContentType.APPLICATION_JSON);
            request.setEntity(postParameters);

            sendRequestToBackendAndRespond(request, response);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendRequestToBackendAndRespond(HttpPost request,
                                                HttpServletResponse response) throws IOException {
        final String stringResponse;
        final long started = System.currentTimeMillis();
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            System.out.println("created httpClient in: " + (System.currentTimeMillis() - started) + "ms");
            final CloseableHttpResponse resp = httpClient.execute(PROXY_TO_HOST, request);
            final StatusLine statusLine = resp.getStatusLine();
            if (statusLine.getStatusCode() == 200) { // ok
                final HttpEntity responseEntity = resp.getEntity();
                stringResponse = EntityUtils.toString(responseEntity);
                try (OutputStream outputStream = response.getOutputStream()) {
                    outputStream.write(stringResponse.getBytes("UTF-8"));
                }
            } else {
                response.setStatus(500); // server error
            }
        }
    }

    private void addSpecificHttpHeaders(HttpPost request) {
        // add some headers
        request.setHeader("Content-Type", "application/json");
        request.setHeader("User-Agent", "Mobile Device");
        logger.info("Added specific headers");
    }

    private URI buildProxyURI(@QueryParam("cmd") String cmd,
                              @QueryParam("pid") String pid,
                              @QueryParam("name") String name) throws URISyntaxException {
        // check params and build URI
        URIBuilder builder = new URIBuilder("/mobile/main");
        if (cmd != null) {
            builder = builder.addParameter("cmd", cmd);
        }
        if (name != null) {
            builder = builder.addParameter("name", name);
        }
        if (pid != null) {
            builder = builder.addParameter("pid", pid);
        }

        URI uri = builder.build();
        logger.info("Built URI: " + uri);
        return uri;
    }
}
