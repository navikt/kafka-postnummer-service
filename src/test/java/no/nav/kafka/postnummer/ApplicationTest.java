package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;

public class ApplicationTest {

    @Test
    public void testStream() {
        Application application = new Application();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        TopologyTestDriver testDriver = new TopologyTestDriver(application.getTopology(), props);

        ConsumerRecordFactory<String, String> factory = new ConsumerRecordFactory<>("postnummer", new StringSerializer(), new StringSerializer());
        testDriver.pipeInput(factory.create("0001\tOSLO\t0301\tOSLO\tP"));

        KeyValueStore<Postnummer, PostnummerWithPoststedAndKommune> store = testDriver.getKeyValueStore("postnummer-store");

        Assert.assertThat(store.get(new Postnummer("0001")).getPoststed().getPoststed(), equalTo("OSLO"));

        testDriver.close();
    }

}
