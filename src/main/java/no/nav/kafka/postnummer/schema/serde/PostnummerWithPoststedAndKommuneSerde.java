package no.nav.kafka.postnummer.schema.serde;

import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;

public class PostnummerWithPoststedAndKommuneSerde extends AbstractSerde<PostnummerWithPoststedAndKommune> {

    protected Class<PostnummerWithPoststedAndKommune> getClassType() {
        return PostnummerWithPoststedAndKommune.class;
    }
}
