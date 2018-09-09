package no.nav.kafka.postnummer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CONTEXT_PATH = "/";

    private final Server jettyServer;

    public WebServer(ResourceConfig resourceConfig) {
        this(DEFAULT_PORT, DEFAULT_CONTEXT_PATH, resourceConfig);
    }

    public WebServer(int port, String contextPath, ResourceConfig resourceConfig) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath(contextPath);

        jettyServer = new Server(port);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "no.nav.kafka.postnummer.web");

        context.addServlet(jerseyServlet, "/*");
    }

    public void start() throws Exception {
        jettyServer.start();
    }

    public void stop() throws Exception {
        jettyServer.stop();
        jettyServer.destroy();
    }
}
