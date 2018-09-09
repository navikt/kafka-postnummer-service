package no.nav.kafka.postnummer.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Postnummer {

    private final String postnummer;

    @JsonCreator
    public Postnummer(@JsonProperty("postnummer") String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostnummer() {
        return postnummer;
    }

    @Override
    public String toString() {
        return "Postnummer{" +
                "postnummer='" + postnummer + '\'' +
                '}';
    }
}
