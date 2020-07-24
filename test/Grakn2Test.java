package grakn.client.test;

import grakn.client.GraknClient;
import grakn.client.concept.ValueType;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
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
        int targetStringLength = 100;
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
            LongSummaryStatistics statistics = types.stream().mapToLong(type -> {
                try (GraknClient.Transaction tx = session.transaction().write()) {
                    EntityType.Remote et = tx.getEntityType(type);
                    assert et != null;

                    for (int i = 0; i < 1; i++) {
                        et.create();
                    }
                    tx.commit();
                    return 1;
                }
            }).summaryStatistics();
            long duration = System.currentTimeMillis() - start;

            System.out.println("Duration: " + duration + "ms");
            System.out.println(String.format("%d tx/s", perSecond(statistics.getCount(), duration)));
            System.out.println(String.format("%d ent/s", perSecond(statistics.getSum(), duration)));
        }

        try (GraknClient.Session session = client.session("grakn")) {
            System.out.println("Reading");
            long start = System.currentTimeMillis();
            LongSummaryStatistics statistics = IntStream.range(0, 100000).parallel().mapToLong(i -> {
                String type = types.get(ThreadLocalRandom.current().nextInt(types.size()));
                try (GraknClient.Transaction tx = session.transaction().read()) {
                    EntityType.Remote et = tx.getEntityType(type);
                    assert et != null;
//
//                    return et.instances().count();
                    return 0;
                }
            }).summaryStatistics();
            long duration = System.currentTimeMillis() - start;

            System.out.println("Duration: " + duration + "ms");
            System.out.println(String.format("%d tx/s", perSecond(statistics.getCount(), duration)));
            System.out.println(String.format("%d ent/s", perSecond(statistics.getSum(), duration)));
        }
    }

    @Test
    public void simple() {
        if (client.databases().contains("grakn")) {
            client.databases().delete("grakn");
        }
        client.databases().create("grakn");

        try (GraknClient.Session session = client.schemaSession("grakn")) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                EntityType.Remote person = tx.putEntityType("person");
                AttributeType.Remote<String> name = tx.putAttributeType("name", ValueType.STRING);
                person.has(name);
                tx.commit();
            }
        }

        try (GraknClient.Session session = client.session("grakn")) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                Objects.requireNonNull(tx.getEntityType("person")).create()
                        .has(Objects.requireNonNull(tx.getAttributeType("name")).create("bob"));
                tx.commit();
            }
        }

        try (GraknClient.Session session = client.session("grakn")) {
            try (GraknClient.Transaction tx = session.transaction().read()) {
                long sum = Objects.requireNonNull(tx.getEntityType("person"))
                        .instances()
                        .mapToLong(p -> p.attributes(tx.getAttributeType("name"))
                                .peek(System.out::println).count()).sum();

                assertEquals(1L, sum);
            }
        }
    }

    @AfterClass
    public static void teardownClass() {
        client.close();
    }

    private static long perSecond(long times, long millis) {
        return (long)((double)times / ((double)millis / 1000.0));
    }
}
