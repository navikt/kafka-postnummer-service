package no.nav.kafka.postnummer.web;

import no.nav.kafka.postnummer.PostnummerService;
import no.nav.kafka.postnummer.PostnummerStoreStub;
import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;
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

public class PostnummerEndpointTest {

    private Server server;

    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        PostnummerStoreStub store = new PostnummerStoreStub(Collections.singletonMap(new Postnummer("2010"),
                new PostnummerWithPoststedAndKommune(new Postnummer("2010"), new Poststed("STRØMMEN"),
                        new Kommune("0231", "SKEDSMO"))));

        startWebserver(store);

        Client client = ClientBuilder.newClient();
        target = client.target("http://localhost:8080");
    }

    private void startWebserver(PostnummerStoreStub stub) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");

        server = new Server(8080);
        server.setHandler(context);

        context.addServlet(new ServletHolder(new ServletContainer(new ResourceConfig()
                .register(new PostnummerEndpoint(new PostnummerService(() -> stub))))), "/*");

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.destroy();
    }

    @Test(expected = NotFoundException.class)
    public void thatNotFoundIsThrownWhenPostnummerIsAbsent() {
        target.path("poststed").request().get(Poststed.class);
    }

    @Test(expected = NotFoundException.class)
    public void thatNotFoundIsThrownWhenPostnummerDoesNotExist() {
        target.path("poststed").path("9999").request().get(Poststed.class);
    }

    @Test
    public void poststed() {
        Poststed response = target.path("postnummer").path("2010").request().get(Poststed.class);
        Assert.assertEquals("STRØMMEN", response.getPoststed());
    }

    @Test
    public void kommune() {
        Kommune kommune = target.path("postnummer").path("2010").path("kommune").request().get(Kommune.class);
        Assert.assertEquals("0231", kommune.getKommuneNr());
        Assert.assertEquals("SKEDSMO", kommune.getKommune());
    }
}
