package no.nav.kafka.postnummer.web;

import no.nav.kafka.postnummer.service.PostnummerNotFoundException;
import no.nav.kafka.postnummer.service.PostnummerService;
import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.function.Function;

@Path("/postnummer/{postnummer}")
public class PostnummerEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PostnummerEndpoint.class);

    private final PostnummerService postnummerService;

    public PostnummerEndpoint(PostnummerService postnummerService) {
        this.postnummerService = postnummerService;
    }

    private <R> R wrapException(Function<Postnummer, R> function, Postnummer postnummer) {
        try {
            R result = function.apply(postnummer);
            LOG.debug("Returning {} for postnummer={}", result, postnummer);
            return result;
        } catch (PostnummerNotFoundException e) {
            LOG.debug("Postnummer {} not found", postnummer, e);
            throw new NotFoundException("Postnummer not found", e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Poststed poststed(@PathParam("postnummer") Postnummer postnummer) {
        LOG.trace("Lookup for postnummer={}", postnummer);
        return wrapException(postnummerService::findPoststed, postnummer);
    }

    @GET
    @Path("kommune")
    @Produces(MediaType.APPLICATION_JSON)
    public Kommune kommune(@PathParam("postnummer") Postnummer postnummer) {
        LOG.trace("Lookup for postnummer={}", postnummer);
        return wrapException(postnummerService::findKommune, postnummer);
    }
}
