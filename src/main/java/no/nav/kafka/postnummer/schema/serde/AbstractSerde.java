package no.nav.kafka.postnummer.schema.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public abstract class AbstractSerde<T> implements Serde<T> {

    abstract protected Class<T> getClassType();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<T> serializer() {
        return new JacksonSerializer<>();
    }

    @Override
    public Deserializer<T> deserializer() {
        return new JacksonDeserializer<>(getClassType());
    }
}
