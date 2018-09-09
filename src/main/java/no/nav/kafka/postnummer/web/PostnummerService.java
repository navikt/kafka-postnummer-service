package no.nav.kafka.postnummer.web;

import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

@Path("/postnummer/{postnummer}")
public class PostnummerService {
    private static final Logger LOG = LoggerFactory.getLogger(PostnummerService.class);

    private final Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> postnummerStore;

    public PostnummerService(Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> postnummerStore) {
        this.postnummerStore = postnummerStore;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Poststed poststed(@PathParam("postnummer") Postnummer postnummer) {
        LOG.trace("Lookup for postnummer={}", postnummer);

        PostnummerWithPoststedAndKommune postnummerWithPoststedAndKommune = postnummerStore.get().get(postnummer);
        if (postnummerWithPoststedAndKommune == null) {
            throw new NotFoundException();
        }

        return postnummerWithPoststedAndKommune.getPoststed();
    }

    @GET
    @Path("kommune")
    @Produces(MediaType.APPLICATION_JSON)
    public Kommune kommune(@PathParam("postnummer") Postnummer postnummer) {
        LOG.trace("Lookup for postnummer={}", postnummer);

        PostnummerWithPoststedAndKommune postnummerWithPoststedAndKommune = postnummerStore.get().get(postnummer);
        if (postnummerWithPoststedAndKommune == null) {
            throw new NotFoundException();
        }

        return postnummerWithPoststedAndKommune.getKommune();
    }
}
