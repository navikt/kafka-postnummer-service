package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.web.NaisEndpoints;
import no.nav.kafka.postnummer.web.PostnummerEndpoint;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.glassfish.jersey.jaxb.internal.XmlJaxbElementProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(XmlJaxbElementProvider.App.class);

    public static void main(String[] args) throws Exception {
        Properties configs = new Properties();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-postnummer-1-1");

        PostnummerStream postnummerStream = new PostnummerStream();

        KafkaStreams streams = postnummerStream.run(configs);

        Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>> postnummerStoreSupplier = new Supplier<ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune>>() {
            @Override
            public ReadOnlyKeyValueStore<Postnummer, PostnummerWithPoststedAndKommune> get() {
                LOG.info("Resolving postnummer store");
                return streams.store(PostnummerStream.POSTNUMMER_STATE_STORE, QueryableStoreTypes.keyValueStore());
            }
        };

        ResourceConfig resourceConfig = new ResourceConfig()
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
                .register(new PostnummerEndpoint(new PostnummerService(postnummerStoreSupplier)));

            runWebserver(resourceConfig);
    }

    private static void runWebserver(ResourceConfig resourceConfig) throws Exception {
        WebServer webServer = new WebServer(resourceConfig);

        webServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOG.info("Shutting down web server");
                webServer.stop();
            } catch (Exception e) {
                LOG.error("Error while shutting down web server", e);
            }
        }));
    }
}
