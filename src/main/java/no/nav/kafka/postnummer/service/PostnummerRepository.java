package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;

interface PostnummerRepository {

    Poststed get(Postnummer postnummer)
            throws PostnummerNotFoundException;
}
