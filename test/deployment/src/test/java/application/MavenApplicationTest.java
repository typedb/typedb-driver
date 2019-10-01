package application;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;
import org.junit.Test;

import java.util.List;

import static graql.lang.Graql.var;
import static org.junit.Assert.assertEquals;

public class MavenApplicationTest {
    @Test
    public void testImport() {
        GraknClient client = new GraknClient("localhost:48555");
        GraknClient.Session session = client.session("grakn");
        GraknClient.Transaction tx = session.transaction().write();
        List<ConceptMap> answers = tx.execute(Graql.match(var("t").sub("thing")).get());
        tx.close();
        session.close();
        assertEquals(4, answers.size());
    }
}
