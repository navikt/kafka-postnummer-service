package no.nav.kafka.postnummer;

import no.nav.kafka.postnummer.schema.Kommune;
import no.nav.kafka.postnummer.schema.Postnummer;
import no.nav.kafka.postnummer.schema.PostnummerWithPoststedAndKommune;
import no.nav.kafka.postnummer.schema.Poststed;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class PostnummerServiceTest {

    private PostnummerService service;

    @Before
    public void setUp() {
        Postnummer postnummer = new Postnummer("2010");
        PostnummerRepositoryStub repository = new PostnummerRepositoryStub(Collections.singletonMap(postnummer,
                new PostnummerWithPoststedAndKommune(postnummer, new Poststed("STRØMMEN"), new Kommune("0231", "SKEDSMO"))));

        service = new PostnummerService(repository);
    }

    @Test
    public void findPoststed() {
        Postnummer postnummer = new Postnummer("2010");

        Poststed poststed = service.findPoststed(postnummer);
        Assert.assertEquals("STRØMMEN", poststed.getPoststed());
    }

    @Test(expected = PostnummerNotFoundException.class)
    public void findNonExistingPoststedThrows() {
        Postnummer postnummer = new Postnummer("0101");
        service.findPoststed(postnummer);
    }

    @Test
    public void findKommune() {
        Postnummer postnummer = new Postnummer("2010");

        Kommune kommune = service.findKommune(postnummer);
        Assert.assertEquals("0231", kommune.getKommuneNr());
        Assert.assertEquals("SKEDSMO", kommune.getKommune());
    }

    @Test(expected = PostnummerNotFoundException.class)
    public void findNonExistingKommuneThrows() {
        Postnummer postnummer = new Postnummer("0101");
        service.findKommune(postnummer);
    }
}
