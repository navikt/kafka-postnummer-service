package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;

public class PostnummerService {

    private final PostnummerRepository repository;

    public PostnummerService(PostnummerRepository repository) {
        this.repository = repository;
    }

    private PostnummerWithPoststedAndKommune lookup(Postnummer postnummer) {
        PostnummerWithPoststedAndKommune postnummerWithPoststedAndKommune = repository.get(postnummer);
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

