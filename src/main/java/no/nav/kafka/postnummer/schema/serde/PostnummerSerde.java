package no.nav.kafka.postnummer.schema.serde;

import no.nav.kafka.postnummer.schema.Postnummer;

public class PostnummerSerde extends AbstractSerde<Postnummer> {

    protected Class<Postnummer> getClassType() {
        return Postnummer.class;
    }
}
