package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;

import java.util.Map;
import java.util.Optional;

public class PostnummerRepositoryStub implements PostnummerRepository {
    private final Map<Postnummer, Poststed> postnummerMap;

    public PostnummerRepositoryStub(Map<Postnummer, Poststed> postnummerMap) {
        this.postnummerMap = postnummerMap;
    }

    @Override
    public Poststed get(Postnummer postnummer) throws PostnummerNotFoundException {
        return Optional.ofNullable(postnummerMap.get(postnummer)).orElseThrow(() -> new PostnummerNotFoundException(postnummer));
    }
}
