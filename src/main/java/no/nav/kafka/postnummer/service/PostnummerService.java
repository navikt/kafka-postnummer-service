package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;

public class PostnummerService {

    private final PostnummerRepository repository;

    public PostnummerService(PostnummerRepository repository) {
        this.repository = repository;
    }

    private Poststed lookup(Postnummer postnummer) {
        Poststed poststed = repository.get(postnummer);
        if (poststed == null) {
            throw new PostnummerNotFoundException(postnummer);
        }
        return poststed;
    }

    public Poststed findPoststed(Postnummer postnummer) {
        return lookup(postnummer);
    }
}

