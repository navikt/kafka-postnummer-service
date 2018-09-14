package no.nav.kafka.postnummer.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Postnummer that = (Postnummer) o;
        return Objects.equals(postnummer, that.postnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postnummer);
    }

    @Override
    public String toString() {
        return "Postnummer{" +
                "postnummer='" + postnummer + '\'' +
                '}';
    }
}
