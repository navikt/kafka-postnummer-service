package no.nav.kafka.postnummer;

import no.nav.common.KafkaEnvironment;
import no.nav.kafka.postnummer.schema.Poststed;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PostnummerComponentTest {

    private static final int NUMBER_OF_BROKERS = 1;
    private static final List<String> TOPICS = Collections.singletonList("postnummer");

    private KafkaEnvironment kafkaEnvironment;

    private final Properties properties = new Properties();

    private int port;
    private Application app;

    @Before
    public void setUp() {
        kafkaEnvironment = new KafkaEnvironment(NUMBER_OF_BROKERS, TOPICS, false, false, Collections.emptyList(), false);
        kafkaEnvironment.start();

        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaEnvironment.getBrokersURL());
        properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");

        Properties props = (Properties)properties.clone();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-postnummer");

        port = 1000 + new Random().nextInt(9999);
        app = new Application(props, port);
    }

    @After
    public void tearDown() throws Exception {
        app.stop();

        kafkaEnvironment.tearDown();
        properties.clear();
    }

    private void createRecords() {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties, new StringSerializer(), new StringSerializer())) {
            producer.send(new ProducerRecord<>("postnummer", "0001\tOSLO\t0301\tOSLO\tP"));
            producer.send(new ProducerRecord<>("postnummer", "1300\tSANDVIKA\t0219\tBÆRUM\tP"));
            producer.send(new ProducerRecord<>("postnummer", "3180\tNYKIRKE\t0701\tHORTEN\tG"));
            producer.flush();
        }
    }

    private void waitForKafka(long timeout, TimeUnit unit) {
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        while (end > System.currentTimeMillis()) {
            if (KafkaEnvironment.ServerParkStatus.Started.INSTANCE == kafkaEnvironment.getServerPark().getStatus()) {
                return;
            }
        }
        throw new RuntimeException("timeout while waiting for kafka to become ready");
    }

    private void waitForLiveness(WebTarget target, long timeout, TimeUnit unit) throws InterruptedException {
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        Response response;
        while (end > System.currentTimeMillis()) {
            response = target.path("isAlive").request().get();

            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return;
            }

            Thread.sleep(1000L);
        }

        throw new RuntimeException("timeout while waiting for app to become alive");
    }

    private void waitForReadiness(WebTarget target, long timeout, TimeUnit unit) throws InterruptedException {
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        Response response;
        while (end > System.currentTimeMillis()) {
            response = target.path("isReady").request().get();

            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return;
            }

            Thread.sleep(1000L);
        }

        throw new RuntimeException("timeout while waiting for app to become ready");
    }

    private Response waitForSuccessfulResponse(WebTarget target, long timeout, TimeUnit unit) throws InterruptedException {
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        while (end > System.currentTimeMillis()) {
            Response response = target.path("postnummer").path("3180").request().get();

            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return response;
            }

            Thread.sleep(1000L);
        }

        throw new RuntimeException("timeout while waiting for non-404 response");
    }

    @Test
    public void testApplication() throws Exception {
        waitForKafka(30, TimeUnit.SECONDS);
        createRecords();

        app.run();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:" + port);

        waitForLiveness(target, 60, TimeUnit.SECONDS);
        waitForReadiness(target, 60, TimeUnit.SECONDS);

        Response response = waitForSuccessfulResponse(target, 30, TimeUnit.SECONDS);

        Assert.assertEquals(200, response.getStatus());

        Poststed poststed = response.readEntity(Poststed.class);
        Assert.assertEquals("NYKIRKE", poststed.getPoststed());
        Assert.assertEquals("HORTEN", poststed.getKommune());
        Assert.assertEquals("0701", poststed.getKommuneNr());
        Assert.assertEquals("G", poststed.getType());
    }
}
