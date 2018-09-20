package no.nav.kafka.postnummer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class TestApplication {

    public static void main(String[] args) throws Exception {
        Properties configs = new Properties();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-postnummer-1-1");

        new Application(configs).run();
    }
}
