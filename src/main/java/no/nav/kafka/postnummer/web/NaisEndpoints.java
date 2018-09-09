package no.nav.kafka.postnummer.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.function.BooleanSupplier;

@Path("/internal")
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
        LOG.trace("isAlive called");

        if (isAliveSupplier.getAsBoolean()) {
            LOG.trace("Returning {}", Response.noContent()
                    .build());
            return Response.noContent()
                    .build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .build();
    }

    @GET
    @Path("isReady")
    public Response isReady() {
        LOG.trace("isReady called");

        if (isReadySupplier.getAsBoolean()) {
            return Response.noContent()
                    .build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .build();
    }
}