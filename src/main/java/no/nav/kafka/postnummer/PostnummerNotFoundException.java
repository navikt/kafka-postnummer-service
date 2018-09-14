package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;

public class PostnummerNotFoundException extends RuntimeException {

    public PostnummerNotFoundException(Postnummer postnummer) {
        super("Unknown postnummer " + postnummer.getPostnummer());
    }
}
