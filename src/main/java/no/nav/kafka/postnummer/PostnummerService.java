package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.function.Supplier;

public class PostnummerService {

    private final Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> postnummerStore;

    public PostnummerService(Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> postnummerStore) {
        this.postnummerStore = postnummerStore;
    }

    private PostnummerWithPoststedAndKommune lookup(Postnummer postnummer) {
        PostnummerWithPoststedAndKommune postnummerWithPoststedAndKommune = postnummerStore.get().get(postnummer);
        if (postnummerWithPoststedAndKommune == null) {
            throw new PostnummerNotFoundException(postnummer);
        }
        return postnummerWithPoststedAndKommune;
    }

    public Poststed findPoststed(Postnummer postnummer) {
        return lookup(postnummer).getPoststed();
    }

    public Kommune findKommune(Postnummer postnummer) {
        return lookup(postnummer).getKommune();
    }
}

