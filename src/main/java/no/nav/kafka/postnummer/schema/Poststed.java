package no.nav.kafka.postnummer.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Poststed {
    private final String postnummer;
    private final String poststed;
    private final String kommuneNr;
    private final String kommune;
    private final String type;

    @JsonCreator
    public Poststed(@JsonProperty("postnummer") String postnummer, @JsonProperty("poststed") String poststed,
                    @JsonProperty("kommuneNr") String kommuneNr, @JsonProperty("kommune") String kommune,
                    @JsonProperty("type") String type) {
        this.postnummer = postnummer;
        this.poststed = poststed;
        this.kommuneNr = kommuneNr;
        this.kommune = kommune;
        this.type = type;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getKommuneNr() {
        return kommuneNr;
    }

    public String getKommune() {
        return kommune;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poststed poststed1 = (Poststed) o;
        return Objects.equals(postnummer, poststed1.postnummer) &&
                Objects.equals(poststed, poststed1.poststed) &&
                Objects.equals(kommuneNr, poststed1.kommuneNr) &&
                Objects.equals(kommune, poststed1.kommune) &&
                Objects.equals(type, poststed1.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postnummer, poststed, kommuneNr, kommune, type);
    }

    @Override
    public String toString() {
        return "Poststed{" +
                "postnummer='" + postnummer + '\'' +
                ", poststed='" + poststed + '\'' +
                ", kommuneNr='" + kommuneNr + '\'' +
                ", kommune='" + kommune + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
