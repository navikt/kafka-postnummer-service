package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.serde.PostnummerSerde;
import no.nav.kafka.postnummer.schema.serde.PostnummerWithPoststedAndKommuneSerde;
import no.nav.kafka.postnummer.web.NaisEndpoints;
import no.nav.kafka.postnummer.web.PostnummerService;
import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private static final String POSTNUMMER_TOPIC = "postnummer";

    public static void main(String[] args) throws Exception {

        PostnummerSerde postnummerSerde = new PostnummerSerde();
        PostnummerWithPoststedAndKommuneSerde postnummerWithPoststedAndKommuneSerde = new PostnummerWithPoststedAndKommuneSerde();

        StreamsBuilder builder = new StreamsBuilder();

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
                }, Materialized.<Postnummer, PostnummerWithPoststedAndKommune, KeyValueStore<Bytes, byte[]>>as("postnummer-store")
                        .withKeySerde(postnummerSerde)
                        .withValueSerde(postnummerWithPoststedAndKommuneSerde));

        postnummerTable.toStream().print(Printed.toSysOut());

        Properties configs = new Properties();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        configs.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-postnummer-1-1");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaStreams streams = new KafkaStreams(builder.build(), configs);

        Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> postnummerStoreSupplier = new Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>>() {
            @Override
            public ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune> get() {
                LOG.info("Resolving postnummer store");
                return streams.store(postnummerTable.queryableStoreName(), QueryableStoreTypes.keyValueStore());
            }
        };

        WebServer webServer = new WebServer(new ResourceConfig()
                .register(new NaisEndpoints(new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() {
                        return streams.state().isRunning();
                    }
                }, new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() {
                        try {
                            postnummerStoreSupplier.get();
                            return true;
                        } catch (Exception e) {
                            LOG.warn(e.getMessage(), e);
                        }

                        return false;
                    }
                }))
                .register(new PostnummerService(postnummerStoreSupplier))
        );

        webServer.start();

        streams.setStateListener(new KafkaStreams.StateListener() {
            @Override
            public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
                LOG.info("From state={} to state={}", oldState, newState);
            }
        });
        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down");
            try {
                webServer.stop();
            } catch (Exception e) {
                LOG.error("Error while shutting down web server", e);
            }
            streams.close();
        }));
    }
}
