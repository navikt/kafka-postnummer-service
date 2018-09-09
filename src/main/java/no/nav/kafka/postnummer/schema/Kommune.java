package no.nav.kafka.postnummer.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Kommune {
    private final String kommuneNr;
    private final String kommune;

    @JsonCreator
    public Kommune(@JsonProperty("kommuneNr") String kommuneNr, @JsonProperty("kommune") String kommune) {
        this.kommuneNr = kommuneNr;
        this.kommune = kommune;
    }

    public String getKommuneNr() {
        return kommuneNr;
    }

    public String getKommune() {
        return kommune;
    }

    @Override
    public String toString() {
        return "Kommune{" +
                "kommuneNr='" + kommuneNr + '\'' +
                ", kommune='" + kommune + '\'' +
                '}';
    }
}
