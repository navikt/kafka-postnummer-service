package no.nav.kafka.postnummer.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class NaisEndpointsTest {

    private Server server;
    private WebTarget target;

    private boolean isAlive;
    private boolean isReady;

    @Before
    public void setUp() throws Exception {
        startWebserver();

        Client client = ClientBuilder.newClient();
        target = client.target("http://localhost:8080");
    }

    private void startWebserver() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");

        server = new Server(8080);
        server.setHandler(context);

        context.addServlet(new ServletHolder(new ServletContainer(new ResourceConfig()
                .register(new NaisEndpoints(() -> isAlive, () -> isReady)))), "/*");

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
    }

    @Test
    public void thatServiceUnavailableIsReturnedWhenIsAliveIsFalse() {
        isAlive = false;
        Response response = target.path("internal").path("isAlive").request().get(Response.class);
        Assert.assertEquals(503, response.getStatus());
    }

    @Test
    public void isAlive() {
        isAlive = true;
        Response response = target.path("internal").path("isAlive").request().get(Response.class);
        Assert.assertEquals(204, response.getStatus());
    }

    @Test
    public void thatServiceUnavailableIsReturnedWhenIsReadyIsFalse() {
        isReady = false;
        Response response = target.path("internal").path("isReady").request().get(Response.class);
        Assert.assertEquals(503, response.getStatus());
    }

    @Test
    public void isReady() {
        isReady = true;
        Response response = target.path("internal").path("isReady").request().get(Response.class);
        Assert.assertEquals(204, response.getStatus());
    }
}
