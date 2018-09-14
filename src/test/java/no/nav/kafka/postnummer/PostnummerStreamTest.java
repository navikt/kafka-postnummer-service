package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;

public class PostnummerStreamTest {

    private TopologyTestDriver testDriver;
    private KeyValueStore<Postnummer, PostnummerWithPoststedAndKommune> store;

    @Before
    public void setUp() {
        PostnummerStream stream = new PostnummerStream();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        testDriver = new TopologyTestDriver(stream.getTopology(), props);
        store = testDriver.getKeyValueStore("postnummer-store");
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test(expected = NoSuchElementException.class)
    public void thatInvalidRecordThrows() {
        createRecords("this is not a postnummer line");
    }

    @Test
    public void thatRecordIsParsed() {
        createRecords("0001\tOSLO\t0301\tOSLO\tP");

        PostnummerWithPoststedAndKommune postnummerWithPoststedAndKommune = store.get(new Postnummer("0001"));
        Assert.assertThat(postnummerWithPoststedAndKommune.getPoststed().getPoststed(), equalTo("OSLO"));
        Assert.assertThat(postnummerWithPoststedAndKommune.getKommune().getKommune(), equalTo("OSLO"));
        Assert.assertThat(postnummerWithPoststedAndKommune.getKommune().getKommuneNr(), equalTo("0301"));
    }

    @Test
    public void thatPostnummerCanChangePoststed() {
        createRecords("0001\tOSLO\t0301\tOSLO\tP", "0001\tTØNSBERG\t0410\tTØNSBERG\tP");

        PostnummerWithPoststedAndKommune postnummerWithPoststedAndKommune = store.get(new Postnummer("0001"));
        Assert.assertThat(postnummerWithPoststedAndKommune.getPoststed().getPoststed(), equalTo("TØNSBERG"));
    }

    private void createRecords(String ...records) {
        ConsumerRecordFactory<String, String> factory = new ConsumerRecordFactory<>("postnummer", new StringSerializer(), new StringSerializer());
        for (String record : records) {
            testDriver.pipeInput(factory.create(record));
        }
    }
}
