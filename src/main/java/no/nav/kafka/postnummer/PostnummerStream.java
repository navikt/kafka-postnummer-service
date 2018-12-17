package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;
import no.nav.kafka.postnummer.schema.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.function.Supplier;

public class PostnummerStream {
    private static final Logger LOG = LoggerFactory.getLogger(PostnummerStream.class);

    private static final String POSTNUMMER_STATE_STORE = "postnummer-store";

    private final Supplier<ReadOnlyKeyValueStore<Postnummer, Poststed>> postnummerStoreSupplier;
    private final KafkaStreams streams;

    public PostnummerStream(Properties configs) {
        streams = new KafkaStreams(new PostnummerTopology().topology, configs);

        postnummerStoreSupplier = new Supplier<ReadOnlyKeyValueStore<Postnummer, Poststed>>() {
            @Override
            public ReadOnlyKeyValueStore<Postnummer, Poststed> get() {
                LOG.info("Resolving postnummer store");
                return streams.store(POSTNUMMER_STATE_STORE, QueryableStoreTypes.keyValueStore());
            }
        };
    }

    public Supplier<ReadOnlyKeyValueStore<Postnummer, Poststed>> getStore() {
        return postnummerStoreSupplier;
    }

    public void run(Runnable shutdownHook) {
        streams.setStateListener(new KafkaStreams.StateListener() {
            @Override
            public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
                LOG.info("From state={} to state={}", oldState, newState);

                if (newState == KafkaStreams.State.ERROR) {
                    // if the stream has died there is no reason to keep spinning
                    LOG.info("No reason to keep living, closing stream");
                    shutdownHook.run();
                }
            }
        });

        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down streams");
            streams.close();
        }));
    }

    public boolean isRunning() {
        return streams.state().isRunning();
    }

    public void stop() {
        streams.close();
    }

    public static class PostnummerTopology {
        private static final String POSTNUMMER_TOPIC = "postnummer";

        private final Topology topology;

        public PostnummerTopology() {
            StreamsBuilder builder = new StreamsBuilder();

            configureStreams(builder);

            topology = builder.build();
        }

        private void configureStreams(StreamsBuilder builder) {
            Serde<Postnummer> postnummerSerde = new JsonSerde<>(Postnummer.class);
            Serde<Poststed> poststedSerde = new JsonSerde<>(Poststed.class);

            KTable<Postnummer, Poststed> postnummerTable = builder.stream(POSTNUMMER_TOPIC,
                    Consumed.with(Serdes.String(), Serdes.String())
                            .withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                    .map(new KeyValueMapper<String, String, KeyValue<Postnummer, Poststed>>() {
                        @Override
                        public KeyValue<Postnummer, Poststed> apply(String key, String value) {
                            StringTokenizer tokenizer = new StringTokenizer(value, "\t");
                            Postnummer postnummer = new Postnummer(tokenizer.nextToken());
                            return new KeyValue<>(
                                    postnummer,
                                    new Poststed(postnummer.getPostnummer(), tokenizer.nextToken(),
                                            tokenizer.nextToken(), tokenizer.nextToken(),
                                            tokenizer.nextToken())
                            );
                        }
                    })
                    .groupByKey(Serialized.with(postnummerSerde, poststedSerde))
                    .aggregate(new Initializer<Poststed>() {
                        @Override
                        public Poststed apply() {
                            return null;
                        }
                    }, new Aggregator<Postnummer, Poststed, Poststed>() {
                        @Override
                        public Poststed apply(Postnummer key, Poststed value, Poststed aggregate) {
                            return value;
                        }
                    }, Materialized.<Postnummer, Poststed, KeyValueStore<Bytes, byte[]>>as(POSTNUMMER_STATE_STORE)
                            .withKeySerde(postnummerSerde)
                            .withValueSerde(poststedSerde));

            postnummerTable.toStream().print(Printed.toSysOut());
        }

        public Topology getTopology() {
            return topology;
        }
    }
}
