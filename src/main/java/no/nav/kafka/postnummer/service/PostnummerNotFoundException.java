package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;

public class PostnummerNotFoundException extends RuntimeException {

    PostnummerNotFoundException(Postnummer postnummer) {
        super("Unknown postnummer " + postnummer.getPostnummer());
    }
}
