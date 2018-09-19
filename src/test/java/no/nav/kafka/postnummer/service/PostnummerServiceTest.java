package no.nav.kafka.postnummer.service;

import no.nav.kafka.postnummer.schema.Postnummer;
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
                new Poststed(postnummer.getPostnummer(),"STRØMMEN","0231", "SKEDSMO", "P")));

        service = new PostnummerService(repository);
    }

    @Test
    public void findPoststed() {
        Postnummer postnummer = new Postnummer("2010");

        Poststed poststed = service.findPoststed(postnummer);
        Assert.assertEquals("STRØMMEN", poststed.getPoststed());
        Assert.assertEquals("0231", poststed.getKommuneNr());
        Assert.assertEquals("SKEDSMO", poststed.getKommune());
        Assert.assertEquals("P", poststed.getType());
    }

    @Test(expected = PostnummerNotFoundException.class)
    public void findNonExistingPoststedThrows() {
        Postnummer postnummer = new Postnummer("0101");
        service.findPoststed(postnummer);
    }
}
