package no.nav.kafka.postnummer.schema.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class JsonSerde<T> implements Serde<T> {

    private final Class<T> classType;

    public JsonSerde(Class<T> classType) {
        this.classType = classType;
    }

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
        return new JacksonDeserializer<>(classType);
    }

    static class JacksonDeserializer<T> implements Deserializer<T> {

        private static final Logger LOG = LoggerFactory.getLogger(JacksonDeserializer.class);

        private final Class<T> type;

        public JacksonDeserializer(Class<T> classType) {
            this.type = classType;
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                if (data == null) {
                    return null;
                }
                return objectMapper.readValue(data, type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
        }
    }

    static class JacksonSerializer<T> implements Serializer<T> {

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        @Override
        public byte[] serialize(String topic, T data) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(data).getBytes();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
        }

    }
}
