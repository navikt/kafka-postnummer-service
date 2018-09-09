package no.nav.kafka.postnummer.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostnummerWithPoststedAndKommune {

    private final Postnummer postnummer;
    private final Poststed poststed;
    private final Kommune kommune;

    @JsonCreator
    public PostnummerWithPoststedAndKommune(@JsonProperty("postnummer") Postnummer postnummer,
                              @JsonProperty("poststed") Poststed poststed,
                              @JsonProperty("kommune") Kommune kommune) {
        this.postnummer = postnummer;
        this.poststed = poststed;
        this.kommune = kommune;
    }

    public Postnummer getPostnummer() {
        return postnummer;
    }

    public Poststed getPoststed() {
        return poststed;
    }

    public Kommune getKommune() {
        return kommune;
    }

    @Override
    public String toString() {
        return "PostnummerWithPoststedAndKommune{" +
                "postnummer=" + postnummer +
                ", poststed=" + poststed +
                ", kommune=" + kommune +
                '}';
    }
}
