package application;

import grakn.client.Grakn;
import grakn.client.Grakn.Client;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.concept.answer.ConceptMap;
import graql.lang.Graql;
import org.junit.Test;

import java.util.List;

import static graql.lang.Graql.var;
import static org.junit.Assert.assertEquals;

public class MavenApplicationTest {
    @Test
    public void testImport() {
        Client client = Grakn.client("localhost:48555");
        Session session = client.session("grakn");
        Transaction tx = session.transaction(Transaction.Type.WRITE);
        List<ConceptMap> answers = tx.execute(Graql.match(var("t").sub("thing")).get()).get();
        tx.close();
        session.close();
        assertEquals(4, answers.size());
    }
}
