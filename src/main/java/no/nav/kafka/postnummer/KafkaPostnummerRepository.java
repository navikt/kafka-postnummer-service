package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.function.Supplier;

public class KafkaPostnummerRepository implements PostnummerRepository {

    private final Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> storeSupplier;

    public KafkaPostnummerRepository(Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> storeSupplier) {
        this.storeSupplier = storeSupplier;
    }

    @Override
    public PostnummerWithPoststedAndKommune get(Postnummer postnummer) throws PostnummerNotFoundException {
        return storeSupplier.get().get(postnummer);
    }
}
