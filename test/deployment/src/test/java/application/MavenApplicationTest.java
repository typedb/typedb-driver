package application;

import grakn.client.Grakn;
import grakn.client.Grakn.Client;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.concept.type.ThingType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// TODO: implement more advanced tests using Graql queries once Grakn 2.0 supports them
public class MavenApplicationTest {

    @Test
    public void test() {
        try (Client client = Grakn.client("localhost:48555")) {
            client.databases().create("grakn");
            try (Session session = client.session("grakn")) {
                try (Transaction tx = session.transaction(Transaction.Type.WRITE)) {
                    ThingType root = tx.concepts().getRootThingType();
                    assertNotNull(root);
                    assertEquals(4, root.asRemote(tx).getSubtypes().count());
                }
            }
        }
    }
}
