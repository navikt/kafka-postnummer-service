package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;
import no.nav.kafka.postnummer.schema.serde.PostnummerSerde;
import no.nav.kafka.postnummer.schema.serde.PostnummerWithPoststedAndKommuneSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.StringTokenizer;

public class PostnummerStream {
    private static final Logger LOG = LoggerFactory.getLogger(PostnummerStream.class);

    private static final String POSTNUMMER_TOPIC = "postnummer";
    static final String POSTNUMMER_STATE_STORE = "postnummer-store";

    private final Topology topology;

    public PostnummerStream() {
        StreamsBuilder builder = new StreamsBuilder();

        configureStreams(builder);

        topology = builder.build();
    }

    private void configureStreams(StreamsBuilder builder) {
        PostnummerSerde postnummerSerde = new PostnummerSerde();
        PostnummerWithPoststedAndKommuneSerde postnummerWithPoststedAndKommuneSerde = new PostnummerWithPoststedAndKommuneSerde();

        KTable<Postnummer, PostnummerWithPoststedAndKommune> postnummerTable = builder.stream(POSTNUMMER_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String())
                        .withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .map(new KeyValueMapper<String, String, KeyValue<Postnummer, PostnummerWithPoststedAndKommune>>() {
                    @Override
                    public KeyValue<Postnummer, PostnummerWithPoststedAndKommune> apply(String key, String value) {
                        StringTokenizer tokenizer = new StringTokenizer(value, "\t");
                        Postnummer postnummer = new Postnummer(tokenizer.nextToken());
                        return new KeyValue<>(
                                postnummer,
                                new PostnummerWithPoststedAndKommune(postnummer,
                                        new Poststed(tokenizer.nextToken()),
                                        new Kommune(tokenizer.nextToken(), tokenizer.nextToken()))
                        );
                    }
                })
                .groupByKey(Serialized.with(postnummerSerde, postnummerWithPoststedAndKommuneSerde))
                .aggregate(new Initializer<PostnummerWithPoststedAndKommune>() {
                    @Override
                    public PostnummerWithPoststedAndKommune apply() {
                        return null;
                    }
                }, new Aggregator<Postnummer, PostnummerWithPoststedAndKommune, PostnummerWithPoststedAndKommune>() {
                    @Override
                    public PostnummerWithPoststedAndKommune apply(Postnummer key, PostnummerWithPoststedAndKommune value, PostnummerWithPoststedAndKommune aggregate) {
                        return value;
                    }
                }, Materialized.<Postnummer, PostnummerWithPoststedAndKommune, KeyValueStore<Bytes, byte[]>>as(POSTNUMMER_STATE_STORE)
                        .withKeySerde(postnummerSerde)
                        .withValueSerde(postnummerWithPoststedAndKommuneSerde));

        postnummerTable.toStream().print(Printed.toSysOut());
    }

    public Topology getTopology() {
        return topology;
    }

    public KafkaStreams run(Properties configs) throws Exception {
        KafkaStreams streams = new KafkaStreams(topology, configs);

        streams.setStateListener(new KafkaStreams.StateListener() {
            @Override
            public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
                LOG.info("From state={} to state={}", oldState, newState);
            }
        });
        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down streams");
            streams.close();
        }));

        return streams;
    }
}
