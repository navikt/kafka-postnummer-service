package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.Poststed;
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
    private KeyValueStore<Postnummer, Poststed> store;

    @Before
    public void setUp() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        PostnummerStream stream = new PostnummerStream();
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

        Poststed poststed = store.get(new Postnummer("0001"));
        Assert.assertThat(poststed.getPoststed(), equalTo("OSLO"));
        Assert.assertThat(poststed.getKommune(), equalTo("OSLO"));
        Assert.assertThat(poststed.getKommuneNr(), equalTo("0301"));
        Assert.assertThat(poststed.getType(), equalTo("P"));
    }

    @Test
    public void thatPostnummerCanChangePoststed() {
        createRecords("0001\tOSLO\t0301\tOSLO\tP", "0001\tTØNSBERG\t0410\tTØNSBERG\tP");

        Poststed poststed = store.get(new Postnummer("0001"));
        Assert.assertThat(poststed.getPoststed(), equalTo("TØNSBERG"));
    }

    private void createRecords(String ...records) {
        ConsumerRecordFactory<String, String> factory = new ConsumerRecordFactory<>("postnummer", new StringSerializer(), new StringSerializer());
        for (String record : records) {
            testDriver.pipeInput(factory.create(record));
        }
    }
}
