package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.service.KafkaPostnummerRepository;
import no.nav.kafka.postnummer.service.PostnummerService;
import no.nav.kafka.postnummer.web.NaisEndpoints;
import no.nav.kafka.postnummer.web.PostnummerEndpoint;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.streams.StreamsConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final ResourceConfig resourceConfig = new ResourceConfig();
    private final WebServer webServer;
    private final PostnummerStream postnummerStream;

    public static void main(String[] args) throws Exception {
        Map<String, String> env = System.getenv();

        Properties configs = new Properties();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getRequiredProperty(env, "KAFKA_BOOTSTRAP_SERVERS"));
        configs.put(StreamsConfig.APPLICATION_ID_CONFIG, "postnummer-service");
        configs.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"" + getRequiredProperty(env, "KAFKA_USERNAME") + "\" " +
                "password=\"" + getRequiredProperty(env, "KAFKA_PASSWORD") + "\";");
        configs.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");

        Optional<String> truststoreLocation = Optional.ofNullable(env.get("NAV_TRUSTSTORE_PATH"));
        if (truststoreLocation.isPresent()) {
            configs.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation.get());

            Optional<String> truststorePassword = Optional.ofNullable(env.get("NAV_TRUSTSTORE_PASSWORD"));
            if (truststorePassword.isPresent()) {
                configs.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword.get());
            }
        }

        new Application(configs, Integer.valueOf(env.getOrDefault("HTTP_PORT", "8080"))).run();
    }

    private static <K, V> V getRequiredProperty(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key)).orElseThrow(() -> new IllegalStateException("Missing required property " + key));
    }

    public Application(Properties configs, int port) {
        this.webServer = new WebServer(port, "/", resourceConfig);
        this.postnummerStream = new PostnummerStream(configs);
    }

    public void run() throws Exception {
        resourceConfig.register(new NaisEndpoints(postnummerStream::isRunning, () -> {
                try {
                    postnummerStream.getStore().get();
                    return true;
                } catch (Exception e) {
                    LOG.warn("Exception while fetching state store; not ready for traffic", e);
                }

                return false;
            }))
                .register(new PostnummerEndpoint(new PostnummerService(new KafkaPostnummerRepository(postnummerStream.getStore()))));


        webServer.start();

        postnummerStream.run(() -> {
            System.exit(1);
        });
    }

    public void stop() throws Exception {
        postnummerStream.stop();
        webServer.stop();
    }
}
