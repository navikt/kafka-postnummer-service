package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;

public interface PostnummerRepository {

    PostnummerWithPoststedAndKommune get(Postnummer postnummer)
            throws PostnummerNotFoundException;
}
