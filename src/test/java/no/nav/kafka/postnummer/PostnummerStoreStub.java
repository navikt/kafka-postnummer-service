package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Map;
import java.util.Optional;

public class PostnummerStoreStub implements ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune> {
    private final Map<Postnummer, PostnummerWithPoststedAndKommune> postnummer;

    public PostnummerStoreStub(Map<Postnummer, PostnummerWithPoststedAndKommune> postnummer) {
        this.postnummer = postnummer;
    }

    @Override
    public PostnummerWithPoststedAndKommune get(Postnummer key) {
        return Optional.ofNullable(postnummer.get(key)).orElseThrow(() -> new PostnummerNotFoundException(key));
    }

    @Override
    public KeyValueIterator<Postnummer, PostnummerWithPoststedAndKommune> range(Postnummer from, Postnummer to) {
        return null;
    }

    @Override
    public KeyValueIterator<Postnummer, PostnummerWithPoststedAndKommune> all() {
        return null;
    }

    @Override
    public long approximateNumEntries() {
        return 0;
    }
}
