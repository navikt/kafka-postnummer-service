package no.nav.kafka.postnummer.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Response;
import java.util.function.BooleanSupplier;

@Path("/")
public class NaisEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger(NaisEndpoints.class);
    private final BooleanSupplier isAliveSupplier;
    private final BooleanSupplier isReadySupplier;

    public NaisEndpoints(BooleanSupplier isAliveSupplier, BooleanSupplier isReadySupplier) {
        this.isAliveSupplier = isAliveSupplier;
        this.isReadySupplier = isReadySupplier;
    }

    @GET
    @Path("isAlive")
    public Response isAlive() {
        if (!isAliveSupplier.getAsBoolean()) {
            LOG.trace("isAlive called, returning not ok");
            throw new ServiceUnavailableException();
        }

        LOG.trace("isAlive called, returning ok");
        return Response.noContent()
                    .build();
    }

    @GET
    @Path("isReady")
    public Response isReady() {
        if (!isReadySupplier.getAsBoolean()) {
            LOG.trace("isReady called, returning not ok");
            throw new ServiceUnavailableException();
        }

        LOG.trace("isReady called, returning ok");
        return Response.noContent()
                    .build();
    }
}
