package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Objects;
import java.util.function.Supplier;

public class KafkaPostnummerRepository implements PostnummerRepository {

    private final Supplier<ReadOnlyKeyValueStore<Postnummer, Poststed>> storeSupplier;

    public KafkaPostnummerRepository(Supplier<ReadOnlyKeyValueStore<Postnummer, Poststed>> storeSupplier) {
        Objects.requireNonNull(storeSupplier);
        this.storeSupplier = storeSupplier;
    }

    @Override
    public Poststed get(Postnummer postnummer) {
        Objects.requireNonNull(postnummer);
        return storeSupplier.get().get(postnummer);
    }
}
