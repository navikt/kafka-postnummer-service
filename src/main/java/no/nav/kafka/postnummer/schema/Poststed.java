package no.nav.kafka.postnummer.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Poststed {
    private final String poststed;

    @JsonCreator
    public Poststed(@JsonProperty("poststed") String poststed) {
        this.poststed = poststed;
    }

    public String getPoststed() {
        return poststed;
    }

    @Override
    public String toString() {
        return "Poststed{" +
                "poststed='" + poststed + '\'' +
                '}';
    }
}
