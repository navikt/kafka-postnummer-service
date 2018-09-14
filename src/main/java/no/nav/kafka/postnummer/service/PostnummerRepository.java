package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;

interface PostnummerRepository {

    PostnummerWithPoststedAndKommune get(Postnummer postnummer)
            throws PostnummerNotFoundException;
}
