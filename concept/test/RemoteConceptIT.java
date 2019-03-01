/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.concept.test;

import grakn.client.GraknClient;
import grakn.core.concept.Label;
import grakn.core.concept.thing.Attribute;
import grakn.core.concept.thing.Entity;
import grakn.core.concept.thing.Relation;
import grakn.core.concept.thing.Thing;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.AttributeType.DataType;
import grakn.core.concept.type.EntityType;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Role;
import grakn.core.concept.type.Rule;
import grakn.core.rule.GraknTestServer;
import graql.lang.pattern.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static graql.lang.Graql.var;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration Tests for testing methods of all subclasses of grakn.client.concept.RemoteConcept.
 */
public class RemoteConceptIT {

    @ClassRule
    public static final GraknTestServer server = new GraknTestServer(
            Paths.get("external/graknlabs_grakn_core/server/conf/grakn.properties"),
            Paths.get("external/graknlabs_grakn_core/test-integration/resources/cassandra-embedded.yaml")
    );
    private static GraknClient.Session session;
    private static int EMAIL_COUNTER = 0;
    private GraknClient.Transaction tx;
    // Attribute Type Labels
    private Label EMAIL = Label.of("email");
    private Label NAME = Label.of("name");
    private Label AGE = Label.of("age");
    private String EMAIL_REGEX = "\\S+@\\S+";
    // Entity Type Labels
    private Label LIVING_THING = Label.of("living-thing");
    private Label PERSON = Label.of("person");
    private Label MAN = Label.of("man");
    private Label BOY = Label.of("boy");

    // Relation Type Labels
    private Label HUSBAND = Label.of("husband");
    private Label WIFE = Label.of("wife");
    private Label MARRIAGE = Label.of("marriage");

    private Label FRIEND = Label.of("friend");
    private Label FRIENDSHIP = Label.of("friendship");

    private Label EMPLOYMENT = Label.of("employment");
    private Label EMPLOYER = Label.of("employer");
    private Label EMPLOYEE = Label.of("employee");

    //Rules
    private Label TEST_RULE = Label.of("genderisedParentship");
    private Pattern testRuleWhen = var("x").isa("person");
    private Pattern testRuleThen = var("y").isa("person");

    // Attribute values
    private String ALICE = "Alice";
    private String ALICE_EMAIL = "alice@email.com";
    private String BOB = "Bob";
    private String BOB_EMAIL = "bob@email.com";
    private Integer TWENTY = 20;

    private AttributeType<Integer> age;
    private AttributeType<String> name;
    private AttributeType<String> email;
    private EntityType livingThing;
    private EntityType person;
    private EntityType man;
    private EntityType boy;
    private Role husband;
    private Role wife;
    private RelationType marriage;
    private Role friend;
    private RelationType friendship;
    private Role employer;
    private Role employee;
    private RelationType employment;
    private Rule metaRule;
    private Rule testRule;

    private Attribute<String> emailAlice;
    private Attribute<String> emailBob;
    private Attribute<Integer> age20;
    private Attribute<String> nameAlice;
    private Attribute<String> nameBob;
    private Entity alice;
    private Entity bob;
    private Relation aliceAndBob;
    private Relation selfEmployment;

    @BeforeClass
    public static void setUpClass() {
        String randomKeyspace = "a" + UUID.randomUUID().toString().replaceAll("-", "");
        session = new GraknClient(server.grpcUri().toString()).session(randomKeyspace);
    }

    @AfterClass
    public static void closeSession() {
        session.close();
    }

    @Before
    public void setUp() {
        tx = session.transaction().write();

        // Attribute Types
        email = tx.putAttributeType(EMAIL, DataType.STRING).regex(EMAIL_REGEX);
        name = tx.putAttributeType(NAME, DataType.STRING);
        age = tx.putAttributeType(AGE, DataType.INTEGER);

        // Entity Types
        livingThing = tx.putEntityType(LIVING_THING).isAbstract(true);
        person = tx.putEntityType(PERSON);
        person.sup(livingThing);
        person.key(email);
        person.has(name);
        person.has(age);

        man = tx.putEntityType(MAN);
        boy = tx.putEntityType(BOY);

        // Relation Types
        husband = tx.putRole(HUSBAND);
        wife = tx.putRole(WIFE);
        marriage = tx.putRelationType(MARRIAGE).relates(wife).relates(husband);

        employer = tx.putRole(EMPLOYER);
        employee = tx.putRole(EMPLOYEE);
        employment = tx.putRelationType(EMPLOYMENT).relates(employee).relates(employer);

        friend = tx.putRole(FRIEND);
        friendship = tx.putRelationType(FRIENDSHIP);

        person.plays(wife).plays(husband);

        //Rules
        metaRule = tx.getSchemaConcept(Label.of("rule"));
        testRule = tx.putRule(TEST_RULE, testRuleWhen, testRuleThen);

        // Attributes
        EMAIL_COUNTER++;
        emailAlice = email.create(ALICE_EMAIL + EMAIL_COUNTER);
        emailBob = email.create(BOB_EMAIL + EMAIL_COUNTER);

        nameAlice = name.create(ALICE);
        nameBob = name.create(BOB);
        age20 = age.create(TWENTY);

        // Entities
        alice = person.create().has(emailAlice).has(nameAlice).has(age20);
        bob = person.create().has(emailBob).has(nameBob).has(age20);

        // Relations
        aliceAndBob = marriage.create().assign(wife, alice).assign(husband, bob);
        selfEmployment = employment.create().assign(employer, alice).assign(employee, alice);

    }

    @After
    public void closeTx() {
        tx.close();
    }

    @Test
    public void whenGettingLabel_ReturnTheExpectedLabel() {
        assertEquals(EMAIL, email.label());
        assertEquals(NAME, name.label());
        assertEquals(AGE, age.label());
        assertEquals(PERSON, person.label());
        assertEquals(HUSBAND, husband.label());
        assertEquals(WIFE, wife.label());
        assertEquals(MARRIAGE, marriage.label());
    }

    @Test
    public void whenCallingIsImplicit_GetTheExpectedResult() {
        email.playing().forEach(role -> assertTrue(role.isImplicit()));
        name.playing().forEach(role -> assertTrue(role.isImplicit()));
        age.playing().forEach(role -> assertTrue(role.isImplicit()));
    }

    @Test
    public void whenCallingIsAbstract_GetTheExpectedResult() {
        assertTrue(livingThing.isAbstract());
    }

    @Test
    public void whenCallingGetValue_GetTheExpectedResult() {
        assertEquals(ALICE_EMAIL + EMAIL_COUNTER, emailAlice.value());
        assertEquals(BOB_EMAIL + EMAIL_COUNTER, emailBob.value());
        assertEquals(ALICE, nameAlice.value());
        assertEquals(BOB, nameBob.value());
        assertEquals(TWENTY, age20.value());
    }

    @Test
    public void whenCallingGetDataTypeOnAttributeType_GetTheExpectedResult() {
        assertEquals(DataType.STRING, email.dataType());
        assertEquals(DataType.STRING, name.dataType());
        assertEquals(DataType.INTEGER, age.dataType());
    }

    @Test
    public void whenCallingGetDataTypeOnAttribute_GetTheExpectedResult() {
        assertEquals(DataType.STRING, emailAlice.dataType());
        assertEquals(DataType.STRING, emailBob.dataType());
        assertEquals(DataType.STRING, nameAlice.dataType());
        assertEquals(DataType.STRING, nameBob.dataType());
        assertEquals(DataType.INTEGER, age20.dataType());
    }

    @Test
    public void whenCallingGetRegex_GetTheExpectedResult() {
        assertEquals(EMAIL_REGEX, email.regex());
    }

    @Test
    public void whenCallingGetAttribute_GetTheExpectedResult() {
        assertEquals(emailAlice, email.attribute(ALICE_EMAIL + EMAIL_COUNTER));
        assertEquals(emailBob, email.attribute(BOB_EMAIL + EMAIL_COUNTER));
        assertEquals(nameAlice, name.attribute(ALICE));
        assertEquals(nameBob, name.attribute(BOB));
        assertEquals(age20, age.attribute(TWENTY));
    }

    @Test
    public void whenCallingGetAttributeWhenThereIsNoResult_ReturnNull() {
        assertNull(email.attribute("x@x.com"));
        assertNull(name.attribute("random"));
        assertNull(age.attribute(-1));
    }

    @Test
    public void whenCallingGetWhenOnMetaRule_ReturnNull() {
        assertNull(metaRule.when());
    }

    @Test
    public void whenCallingGetThenOnMetaRule_ReturnNull() {
        assertNull(metaRule.then());
    }

    @Ignore
    @Test //TODO: build a more expressive dataset to test this
    public void whenCallingIsInferred_GetTheExpectedResult() {
        //assertTrue(thing.isInferred());
        //assertFalse(thing.isInferred());
    }

    @Test
    public void whenCallingGetWhen_GetTheExpectedResult() {
        assertEquals(testRuleWhen, testRule.when());
    }

    @Test
    public void whenCallingGetThen_GetTheExpectedResult() {
        assertEquals(testRuleThen, testRule.then());
    }

    @Test
    public void whenDeletingAConcept_ConceptIsDeleted() {
        Entity randomPerson = person.create();
        assertFalse(randomPerson.isDeleted());

        randomPerson.delete();
        assertTrue(randomPerson.isDeleted());
    }

    @Test
    public void whenCallingSups_GetTheExpectedResult() {
        assertTrue(person.sups().anyMatch(c -> c.equals(livingThing)));
    }

    @Test
    public void whenCallingSubs_GetTheExpectedResult() {
        assertTrue(livingThing.subs().anyMatch(c -> c.equals(person)));
    }

    @Test
    public void whenCallingSup_GetTheExpectedResult() {
        assertEquals(livingThing, person.sup());
    }

    @Test
    public void whenCallingSupOnMetaType_GetNull() {
        assertNull(tx.getEntityType("entity").sup());
    }

    @Test
    public void whenCallingType_GetTheExpectedResult() {
        assertEquals(email, emailAlice.type());
        assertEquals(email, emailBob.type());
        assertEquals(name, nameAlice.type());
        assertEquals(name, nameBob.type());
        assertEquals(age, age20.type());
        assertEquals(person, alice.type());
        assertEquals(person, bob.type());
        assertEquals(marriage, aliceAndBob.type());
    }

    @Test
    public void whenCallingAttributesWithNoArguments_GetTheExpectedResult() {
        assertThat(alice.attributes().collect(toSet()), containsInAnyOrder(emailAlice, nameAlice, age20));
        assertThat(bob.attributes().collect(toSet()), containsInAnyOrder(emailBob, nameBob, age20));
    }

    @Test
    public void whenCallingAttributesWithArguments_GetTheExpectedResult() {
        assertThat(alice.attributes(email, age).collect(toSet()), containsInAnyOrder(emailAlice, age20));
        assertThat(bob.attributes(email, age).collect(toSet()), containsInAnyOrder(emailBob, age20));
    }

    @Test
    public void whenCallingKeysWithNoArguments_GetTheExpectedResult() {
        assertThat(alice.keys().collect(toSet()), contains(emailAlice));
        assertThat(bob.keys().collect(toSet()), contains(emailBob));
    }

    @Test
    public void whenCallingKeysWithArguments_GetTheExpectedResult() {
        assertThat(alice.keys(email).collect(toSet()), contains(emailAlice));
        assertThat(bob.keys(email).collect(toSet()), contains(emailBob));
    }

    @Test
    public void whenCallingPlays_GetTheExpectedResult() {
        assertThat(person.playing().filter(r -> !r.isImplicit()).collect(toSet()), containsInAnyOrder(wife, husband));
    }

    @Test
    public void whenCallingInstances_GetTheExpectedResult() {
        assertThat(email.instances().collect(toSet()), containsInAnyOrder(emailAlice, emailBob));
        assertThat(name.instances().collect(toSet()), containsInAnyOrder(nameAlice, nameBob));
        assertThat(age.instances().collect(toSet()), containsInAnyOrder(age20));
        assertThat(person.instances().collect(toSet()), containsInAnyOrder(alice, bob));
        assertThat(marriage.instances().collect(toSet()), containsInAnyOrder(aliceAndBob));
    }

    @Test
    public void whenCallingThingPlays_GetTheExpectedResult() {
        assertThat(alice.roles().filter(r -> !r.isImplicit()).collect(toSet()), containsInAnyOrder(wife, employee, employer));
        assertThat(bob.roles().filter(r -> !r.isImplicit()).collect(toSet()), containsInAnyOrder(husband));
    }

    @Test
    public void whenCallingRelationsWithNoArguments_GetTheExpectedResult() {
        assertThat(alice.relations().filter(rel -> !rel.type().isImplicit()).collect(toSet()), containsInAnyOrder(aliceAndBob, selfEmployment));
        assertThat(bob.relations().filter(rel -> !rel.type().isImplicit()).collect(toSet()), containsInAnyOrder(aliceAndBob));
    }

    @Test
    public void whenCallingRelationsWithRoles_GetTheExpectedResult() {
        assertThat(alice.relations(wife).collect(toSet()), containsInAnyOrder(aliceAndBob));
        assertThat(bob.relations(husband).collect(toSet()), containsInAnyOrder(aliceAndBob));
    }

    @Test
    public void whenCallingRelationTypes_GetTheExpectedResult() {
        assertThat(wife.relations().collect(toSet()), containsInAnyOrder(marriage));
        assertThat(husband.relations().collect(toSet()), containsInAnyOrder(marriage));
    }

    @Test
    public void whenCallingPlayedByTypes_GetTheExpectedResult() {
        assertThat(wife.players().collect(toSet()), containsInAnyOrder(person));
        assertThat(husband.players().collect(toSet()), containsInAnyOrder(person));
    }

    @Test
    public void whenCallingRelates_GetTheExpectedResult() {
        assertThat(marriage.roles().collect(toSet()), containsInAnyOrder(wife, husband));
    }

    @Test
    public void whenCallingAllRolePlayers_GetTheExpectedResult() {
        Map<Role, Set<Thing>> expected = new HashMap<>();
        expected.put(wife, Collections.singleton(alice));
        expected.put(husband, Collections.singleton(bob));

        assertEquals(expected, aliceAndBob.rolePlayersMap());
    }

    @Test
    public void whenCallingRolePlayersWithNoArguments_GetTheExpectedResult() {
        assertThat(aliceAndBob.rolePlayers().collect(toSet()), containsInAnyOrder(alice, bob));
    }

    @Test
    public void whenCallingRolePlayersWithNoArgumentsOnReflexiveRelation_GetDistinctExpectedResult() {
        List<Thing> list = selfEmployment.rolePlayers().collect(toList());
        assertEquals(1, list.size());
        assertThat(list, containsInAnyOrder(alice));
    }

    @Test
    public void whenCallingRolePlayersWithRoles_GetTheExpectedResult() {
        assertThat(aliceAndBob.rolePlayers(wife).collect(toSet()), containsInAnyOrder(alice));
        assertThat(aliceAndBob.rolePlayers(husband).collect(toSet()), containsInAnyOrder(bob));
    }

    @Test
    public void whenCallingOwnerInstances_GetTheExpectedResult() {
        assertThat(emailAlice.owners().collect(toSet()), containsInAnyOrder(alice));
        assertThat(emailBob.owners().collect(toSet()), containsInAnyOrder(bob));
        assertThat(nameAlice.owners().collect(toSet()), containsInAnyOrder(alice));
        assertThat(nameBob.owners().collect(toSet()), containsInAnyOrder(bob));
        assertThat(age20.owners().collect(toSet()), containsInAnyOrder(alice, bob));
    }

    @Test
    public void whenCallingAttributeTypes_GetTheExpectedResult() {
        assertThat(person.attributes().collect(toSet()), containsInAnyOrder(email, name, age));
    }

    @Test
    public void whenCallingKeyTypes_GetTheExpectedResult() {
        assertThat(person.keys().collect(toSet()), containsInAnyOrder(email));
    }

    @Test
    public void whenSettingSuperType_TypeBecomesSupertype() {
        man.sup(person);
        assertEquals(person, man.sup());
    }

    @Test
    public void whenSettingTypeLabel_LabelIsSetToType() {
        Label lady = Label.of("lady");
        EntityType type = tx.putEntityType(lady);
        assertEquals(lady, type.label());

        Label woman = Label.of("woman");
        type.label(woman);
        assertEquals(woman, type.label());
    }

    @Test
    public void whenSettingAndDeletingRelationRelatesRole_RoleInRelationIsSetAndDeleted() {
        friendship.relates(friend);
        assertTrue(friendship.roles().anyMatch(c -> c.equals(friend)));

        friendship.unrelate(friend);
        assertFalse(friendship.roles().anyMatch(c -> c.equals(friend)));
    }

    @Test
    public void whenSettingAndDeletingEntityPlaysRole_RolePlaysEntityIsSetAndDeleted() {
        person.plays(friend);
        assertTrue(person.playing().anyMatch(c -> c.equals(friend)));

        person.unplay(friend);
        assertFalse(person.playing().anyMatch(c -> c.equals(friend)));
    }

    @Test
    public void whenSettingAndUnsettingAbstractType_TypeAbstractIsSetAndUnset() {
        livingThing.isAbstract(false);
        assertFalse(livingThing.isAbstract());

        livingThing.isAbstract(true);
        assertTrue(livingThing.isAbstract());
    }

    @Test
    public void whenSettingAndDeletingAttributeToType_AttributeIsSetAndDeleted() {
        EntityType cat = tx.putEntityType(Label.of("cat"));
        cat.has(name);
        assertTrue(cat.attributes().anyMatch(c -> c.equals(name)));

        cat.unhas(name);
        assertFalse(cat.attributes().anyMatch(c -> c.equals(name)));
    }

    @Test
    public void whenSettingAndDeletingKeyToType_KeyIsSetAndDeleted() {
        AttributeType<String> username = tx.putAttributeType(Label.of("username"), DataType.STRING);
        person.key(username);
        assertTrue(person.keys().anyMatch(c -> c.equals(username)));

        person.unkey(username);
        assertFalse(person.keys().anyMatch(c -> c.equals(username)));
    }

    @Test
    public void whenCallingAddEntity_TypeIsCorrect() {
        Entity newPerson = person.create();
        assertEquals(person, newPerson.type());
    }

    @Test
    public void whenCallingAddRelation_TypeIsCorrect() {
        Relation newMarriage = marriage.create();
        assertEquals(marriage, newMarriage.type());
    }

    @Test
    public void whenCallingPutAttribute_TypeIsCorrect() {
        Attribute<String> nameCharlie = name.create("Charlie");
        assertEquals(name, nameCharlie.type());
    }

    @Test
    public void whenSettingAndUnsettingRegex_RegexIsSetAndUnset() {
        email.regex(null);
        assertNull((email.regex()));

        email.regex(EMAIL_REGEX);
        assertEquals(EMAIL_REGEX, email.regex());
    }

    @Test
    public void whenCallingAddAttributeRelationOnThing_RelationIsImplicit() {
        assertTrue(alice.relhas(emailAlice).type().isImplicit());
        assertTrue(alice.relhas(nameAlice).type().isImplicit());
        assertTrue(alice.relhas(age20).type().isImplicit());
        assertTrue(bob.relhas(emailBob).type().isImplicit());
        assertTrue(bob.relhas(nameBob).type().isImplicit());
        assertTrue(bob.relhas(age20).type().isImplicit());
    }

    @Test
    public void whenCallingDeleteAttribute_ExecuteAConceptMethod() {
        Entity charlie = person.create();
        Attribute<String> nameCharlie = name.create("Charlie");
        charlie.has(nameCharlie);
        assertTrue(charlie.attributes(name).anyMatch(x -> x.equals(nameCharlie)));

        charlie.unhas(nameCharlie);
        assertFalse(charlie.attributes(name).anyMatch(x -> x.equals(nameCharlie)));
    }

    @Test
    public void whenAddingAndRemovingRolePlayer_RolePlayerIsAddedAndRemoved() {
        Entity dylan = person.create();
        Entity emily = person.create();

        Relation dylanAndEmily = friendship.create()
                .assign(friend, dylan)
                .assign(friend, emily);

        assertThat(dylanAndEmily.rolePlayers().collect(toSet()), containsInAnyOrder(dylan, emily));

        dylanAndEmily.unassign(friend, dylan);
        dylanAndEmily.unassign(friend, emily);

        assertTrue(dylanAndEmily.rolePlayers().collect(toSet()).isEmpty());
    }
}