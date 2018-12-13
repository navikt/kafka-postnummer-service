package no.nav.kafka.postnummer.web;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;
import no.nav.kafka.postnummer.service.PostnummerRepositoryStub;
import no.nav.kafka.postnummer.service.PostnummerService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Collections;
import java.util.Random;

public class PostnummerEndpointTest {

    private Server server;

    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        PostnummerRepositoryStub repository = new PostnummerRepositoryStub(Collections.singletonMap(new Postnummer("2010"),
                new Poststed("2010", "STRØMMEN", "0231", "SKEDSMO", "P")));

        int port = 1000 + new Random().nextInt(9999);
        startWebserver(port, repository);

        Client client = ClientBuilder.newClient();
        target = client.target("http://localhost:" + port);
    }

    private void startWebserver(int port, PostnummerRepositoryStub repository) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");

        server = new Server(port);
        server.setHandler(context);

        context.addServlet(new ServletHolder(new ServletContainer(new ResourceConfig()
                .register(new PostnummerEndpoint(new PostnummerService(repository))))), "/*");

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
    }

    @Test(expected = NotFoundException.class)
    public void thatNotFoundIsThrownWhenPostnummerIsAbsent() {
        target.path("postnummer").request().get(Poststed.class);
    }

    @Test(expected = NotFoundException.class)
    public void thatNotFoundIsThrownWhenPostnummerDoesNotExist() {
        target.path("postnummer").path("9999").request().get(Poststed.class);
    }

    @Test
    public void poststed() {
        Poststed response = target.path("postnummer").path("2010").request().get(Poststed.class);
        Assert.assertEquals("STRØMMEN", response.getPoststed());
        Assert.assertEquals("SKEDSMO", response.getKommune());
        Assert.assertEquals("0231", response.getKommuneNr());
        Assert.assertEquals("P", response.getType());
    }
}
