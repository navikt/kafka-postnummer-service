package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.service.PostnummerNotFoundException;
import no.nav.kafka.postnummer.service.PostnummerRepository;

import java.util.Map;
import java.util.Optional;

public class PostnummerRepositoryStub implements PostnummerRepository {
    private final Map<Postnummer, PostnummerWithPoststedAndKommune> postnummerMap;

    public PostnummerRepositoryStub(Map<Postnummer, PostnummerWithPoststedAndKommune> postnummerMap) {
        this.postnummerMap = postnummerMap;
    }

    @Override
    public PostnummerWithPoststedAndKommune get(Postnummer postnummer) throws PostnummerNotFoundException {
        return Optional.ofNullable(postnummerMap.get(postnummer)).orElseThrow(() -> new PostnummerNotFoundException(postnummer));
    }
}