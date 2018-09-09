package no.nav.kafka.postnummer.schema.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class JacksonDeserializer<T> implements Deserializer<T> {

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
