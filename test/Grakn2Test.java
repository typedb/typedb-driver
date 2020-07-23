package grakn.client.test;

import grakn.client.GraknClient;
import grakn.client.concept.type.EntityType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;

public class Grakn2Test {

    private static GraknClient client;

    @BeforeClass
    public static void setupClass() {
        client = new GraknClient(GraknClient.DEFAULT_URI);
    }

    @Test
    public void test() {
//        client.databases().create("grakn");

//        try (GraknClient.Session session = client.schemaSession("grakn")) {
//            try (GraknClient.Transaction tx = session.transaction().write()) {
//                tx.putEntityType("person");
//                tx.commit();
//            }
//        }

//        try (GraknClient.Session session = client.session("grakn")) {
//            try (GraknClient.Transaction tx = session.transaction().write()) {
//                EntityType.Remote personType = tx.getEntityType("person");
//                assertNotNull(personType);
//
//                for (int i = 0; i < 100; i++) {
//                    Entity.Remote person = personType.create();
//                    assertNotNull(person);
//                }
//                tx.commit();
//            }
//        }
//        try (GraknClient.Session session = client.session("grakn")) {
//            System.out.println("warmup");
//
//            long count = IntStream.range(0, 100).parallel().mapToLong(i -> {
//                try (GraknClient.Transaction tx = session.transaction().read()) {
//                    EntityType.Remote personType = tx.getEntityType("person");
//                    assertNotNull(personType);
//
//                    System.out.println("Started " + i);
//                    long instances = personType.instances().count();
//                    System.out.println("Ended " + i);
//                    return instances;
//                }
//            }).sum();
//
//            System.out.println(count);
//        }
//
//        try (GraknClient.Session session = client.session("grakn")) {
//            System.out.println("go");
//            long time1 = System.currentTimeMillis();
//
//            long count = IntStream.range(0, 1000).parallel().mapToLong(i -> {
//                try (GraknClient.Transaction tx = session.transaction().read()) {
//                    EntityType.Remote personType = tx.getEntityType("person");
//                    assertNotNull(personType);
//
////                    System.out.println("Started " + i);
//                    long instances = personType.instances().count();
////                    System.out.println("Ended " + i);
//                    return instances;
//                }
//            }).sum();
//
//            long time2 = System.currentTimeMillis();
//            long duration = time2 - time1;
//            System.out.println("Duration: " + duration + "ms");
//            System.out.println(String.format("%d entities/s", (int)((double)count / ((double)duration / 1000.0))));
//        }

        try {
            client.databases().delete("grakn");
        } catch (Exception ex) {
            System.out.println("There was no DB");
        }

        client.databases().create("grakn");

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        final int factor = 100;

        List<String> types = IntStream.range(0, factor)
                .mapToObj(i -> random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()).collect(Collectors.toList());

        try (GraknClient.Session session = client.schemaSession("grakn")) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                types.forEach(tx::putEntityType);
                tx.commit();
            }
        }

        try (GraknClient.Session session = client.session("grakn")) {
            System.out.println("Writing");
            long start = System.currentTimeMillis();
            long count = types.parallelStream().mapToLong(type -> {
                try (GraknClient.Transaction tx = session.transaction().write()) {
                    EntityType.Remote et = tx.getEntityType(type);
                    assert et != null;

                    for (int i = 0; i < factor; i++) {
                        et.create();
                    }
                    tx.commit();
                    return factor;
                }
            }).sum();
            long duration = System.currentTimeMillis() - start;
            System.out.println("Duration: " + duration + "ms");
            System.out.println(String.format("%d entities/s", (int)((double)count / ((double)duration / 1000.0))));
        }

        try (GraknClient.Session session = client.session("grakn")) {
            System.out.println("Reading");
            long start = System.currentTimeMillis();
            long count = types.parallelStream().mapToLong(type -> {
                try (GraknClient.Transaction tx = session.transaction().write()) {
                    EntityType.Remote et = tx.getEntityType(type);
                    assert et != null;

                    return et.instances().count();
                }
            }).sum();
            long duration = System.currentTimeMillis() - start;
            System.out.println("Duration: " + duration + "ms");
            System.out.println(String.format("%d entities/s", (int)((double)count / ((double)duration / 1000.0))));
        }
    }

    @AfterClass
    public static void teardownClass() {
        client.close();
    }
}
