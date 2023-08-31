#
# Copyright (C) 2022 Vaticle
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import unittest
import uuid

from tests.integration.base import test_base, TypeDBServer

import typedb
from typedb import TypeDBError
from typedb.api.concept.concept import ValueType
from typedb.connection import TypeDBClient, Transaction


# TODO: we should ensure that all these tests are migrated to BDD
class test_client_PreDbSetup(test_base):
    """ Tests Database interactions *before* anything needs to be inserted/created """

    # --- Test typedb client instantiation for one URI ---
    def test_client_init_valid(self):
        """ Test valid URI """
        a_inst = TypeDBClient('localhost:48555')
        self.assertIsInstance(a_inst, TypeDBClient)
        a_inst.close()

    def test_client_with_statement(self):
        """ Test that client is compatible with using `with` """
        with TypeDBClient("localhost:48555") as client:
            with client.session("testing") as session:
                with session.transaction().read() as tx:
                    tx.query("match $x sub thing; get;")



    def test_client_init_invalid_uri(self):
        """ Test invalid URI """
        with self.assertRaises(TypeDBError):
            a_inst = TypeDBClient('localhost:1000')
            a_session = a_inst.session('testkeyspace')
            a_session.transaction().read()

        with self.assertRaises(TypeDBError):
            a_inst = TypeDBClient('localhost:1000')
            with a_inst.session("test") as s:
                with s.transaction().read() as tx:
                    pass
            a_inst.close()


    # --- Test client session for different keyspaces ---
    def test_client_session_valid_keyspace(self):
        """ Test OK uri and keyspace """
        a_inst = TypeDBClient('localhost:48555')
        a_session = a_inst.session('test')
        self.assertIsInstance(a_session, typedb.rpc.Session)
        tx = a_session.transaction().read()
        tx.close()
        a_session.close()

        # test the `with` statement
        with a_inst.session('test') as session:
            self.assertIsInstance(session, typedb.rpc.Session)
            tx = session.transaction().read()
            tx.close()

        a_inst.close()

    def test_client_session_invalid_keyspace(self):
        client = TypeDBClient('localhost:48555')
        with self.assertRaises(TypeError):
            a_session = client.session(123)
            tx = a_session.transaction().read() # won't fail until opening a transaction
        inst2 = TypeDBClient('localhost:48555')
        with self.assertRaises(TypeDBError):
            a_session = inst2.session('')
            tx = a_session.transaction().read() # won't fail until opening a transaction
        client.close()

    def test_client_session_close(self):
        client = TypeDBClient('localhost:48555')
        a_session = client.session('test')
        a_session.close()
        with self.assertRaises(TypeDBError):
            a_session.transaction().read()
        client.close()

    # --- Test typedb session transactions that are pre-DB setup ---
    def test_client_tx_valid_enum(self):
        client = TypeDBClient('localhost:48555')
        a_session = client.session('test')
        tx = a_session.transaction().read()
        self.assertIsInstance(tx, typedb.rpc.Transaction)
        client.close()

    def test_client_tx_invalid_enum(self):
        client = TypeDBClient('localhost:48555')
        a_session = client.session('test')
        with self.assertRaises(Exception):
            a_session.transaction('foo')
        client.close()


client = None
session = None

class test_client_base(test_base):
    """ Sets up DB for use in tests """

    @classmethod
    def setUpClass(cls):
        """ Make sure we have some sort of schema and data in DB, only done once """
        super(test_client_base, cls).setUpClass()

        global client, session
        client = TypeDBClient("localhost:48555")
        keyspace = "test_" + str(uuid.uuid4()).replace("-", "_")[:8]
        session = client.session(keyspace)

        # temp tx to set up DB, don't save it
        with session.transaction().write() as tx:
            try:
                # define parentship roles to test agains
                tx.query("define "
                         "parent sub role; "
                         "child sub role; "
                         "mother sub role; "
                         "son sub role; "
                         "person sub entity, has age, has gender, plays parent, plays child, plays mother, plays son; "
                         "age sub attribute, value long; "
                         "gender sub attribute, value string; "
                         "parentship sub relation, relates parent, relates child, relates mother, relates son;")
            except TypeDBError as ce:
                print(ce)
    
            answers = list(tx.query("match $x isa person, has age 20; get;"))
            if len(answers) == 0:
                tx.query("insert $x isa person, has age 20;")
            tx.commit()

    @classmethod
    def tearDownClass(cls):
        super(test_client_base, cls).tearDownClass()

        global client, session
        session.close()
        client.keyspaces().delete(session.keyspace)
        client.close()

    def setUp(self):
        global session
        self.tx = session.transaction().write()
        # functions called by `addCleanup` are reliably called independent of test pass or failure
        self.addCleanup(self.cleanupTransaction, self.tx)

    def cleanupTransaction(self, tx):
        tx.close()



class test_Transaction(test_client_base):
    """ Class for testing transaction methods, eg query, put attribute type... """

    # --- query tests ---
    def test_query_valid_result(self):
        """ Test a valid query """
        answers = self.tx.query("match $x isa person; get;")
        self.assertIsNotNone(answers)

    def test_query_empty_result(self):
        """ Ensures that empty query results behave like empty iterators """
        answers = self.tx.query('match $x isa person, has age 9999; get;')
        with self.assertRaises(StopIteration):
            next(answers)

    def test_query_with_multiple_batches(self):
        """ Ensures that the query batching code works as intended and does not regress """
        self.tx.query('define giraffe sub entity;')
        for i in range(150):
            self.tx.query('insert $x isa giraffe;')
        answers = self.tx.query('match $x isa giraffe; get $x;')
        self.assertEquals(sum(1 for _ in answers), 150)

    def test_query_invalid_syntax(self):
        """ Invalid syntax -- expected behavior is an exception & closed transaction """
        with self.assertRaises(TypeDBError):
            next(self.tx.query("match $x bob marley; get"))
        with self.assertRaises(TypeDBError):
            # should be closed
            next(self.tx.query("match $x isa person; get;"))
        self.assertFalse(self.tx.is_open(), msg="Tx is not closed after invalid syntax")


    def test_query_infer_false(self):
        """ Test that when the infer flag is false, no inferred answers are returned """
        local_session = client.session("query_flag_infer_false")
        local_tx = local_session.transaction().write()
        local_tx.query("define person sub entity, has name; name sub attribute, value string; naming sub rule, when {$x isa person; $a isa name;}, then {$x has name $a;};").get()
        local_tx.query("insert $x isa person; $a \"John\" isa name;").get()
        local_tx.commit()
        local_tx = local_session.transaction().read()
        answers = list(local_tx.query("match $x isa person, has name $a; get;", infer=False).get())
        self.assertEquals(len(answers), 0)


    def test_query_infer_true(self):
        """ Test that when the infer flag is true, inferred answers are returned """
        local_session = client.session("query_flag_infer_true")
        local_tx = local_session.transaction().write()
        local_tx.query("define person sub entity, has name; name sub attribute, value string; naming sub rule, when {$x isa person; $a isa name;}, then {$x has name $a;};").get()
        local_tx.query("insert $x isa person; $a \"John\" isa name;").get()
        local_tx.commit()
        local_tx = local_session.transaction().read()
        answers = list(local_tx.query("match $x isa person, has name $a; get;", infer=True).get())
        self.assertEquals(len(answers), 1)


    def test_query_batch_all(self):
        """ Test that batch_size=ALL succeeds at retrieving all the answers """
        local_session = client.session("query_batch_size_all")
        local_tx = local_session.transaction().write()
        local_tx.query("define person sub entity, has name; name sub attribute, value string;")
        for i in range(100):
            local_tx.query("insert $x isa person, has name \"John-{0}\"; ".format(i))
        local_tx.commit()
        local_tx = local_session.transaction().read()
        answers = list(local_tx.query("match $x isa person, has name $a; get;", batch_size=Transaction.Options.BATCH_ALL).get())
        self.assertEquals(len(answers), 100)


    def test_query_batch_one(self):
        """ Test that batch_size=1 succeeds at retrieving all the answers """
        local_session = client.session("query_batch_size_one")
        local_tx = local_session.transaction().write()
        local_tx.query("define person sub entity, has name; name sub attribute, value string;")
        for i in range(100):
            local_tx.query("insert $x isa person, has name \"John-{0}\"; ".format(i))
        local_tx.commit()
        local_tx = local_session.transaction().read()
        answers = list(local_tx.query("match $x isa person, has name $a; get;", batch_size=1).get())
        self.assertEquals(len(answers), 100)


    def test_query_batch_nonpositive_throws(self):
        """ Test that batch_size="Nonsense" succeeds at retrieving all the answers """
        local_session = client.session("query_batch_size_nonpositive")
        local_tx = local_session.transaction().write()
        local_tx.query("define person sub entity, has name; name sub attribute, value string;")
        for i in range(100):
            local_tx.query("insert $x isa person, has name \"John-{0}\"; ".format(i))
        local_tx.commit()
        local_tx = local_session.transaction().read()
        with self.assertRaises(Exception):
            answers = list(local_tx.query("match $x isa person, has name $a; get;", batch_size=0).get())
        with self.assertRaises(Exception):
            answers = list(local_tx.query("match $x isa person, has name $a; get;", batch_size=-50).get())


    def test_query_batch_nonsense_throws(self):
        """ Test that batch_size="Nonsense" succeeds at retrieving all the answers """
        local_session = client.session("query_batch_size_nonsense")
        local_tx = local_session.transaction().write()
        local_tx.query("define person sub entity, has name; name sub attribute, value string;")
        for i in range(100):
            local_tx.query("insert $x isa person, has name \"John-{0}\"; ".format(i))
        local_tx.commit()
        local_tx = local_session.transaction().read()
        with self.assertRaises(Exception):
            answers = list(local_tx.query("match $x isa person, has name $a; get;", batch_size="nonsense").get())


    def test_query_tx_already_closed(self):
        self.tx.close()
        with self.assertRaises(TypeDBError):
            self.tx.query("match $x isa person; get;")
            
        self.assertFalse(self.tx.is_open(), msg="Tx is not closed after close()")

    def test_no_metatype_duplicates(self):
        concepts = [ans.get("x") for ans in self.tx.query("match $x sub entity; get;")]
        self.assertEqual(len(concepts), 2) # entity and person
        id_set = set(concepts)
        self.assertEqual(len(id_set), 2) # entity and person, not the same

    def test_query_errors_async(self):
        answer = self.tx.query("match $x isa unicorn; get;")

        with self.assertRaises(TypeDBError):
            answer.get()

    def test_query_errors_async_on_commit(self):
        self.tx.query("match $x isa unicorn; get;")

        with self.assertRaises(TypeDBError):
            self.tx.commit()


    # --- commit tests --- 
    def test_commit_no_error_thrown(self):
        """ TODO double check if we even need this test """
        self.tx.commit()

    def test_commit_normal(self):
        """ Insert, commit, read and check it worked """
        jills_before= len(list(self.tx.query('match $x isa person, has age 10; get;')))
        self.tx.query('insert $x isa person, has age 10;') 
        self.tx.commit()
        # need to open new tx after commit
        tx = session.transaction().write()
        answers = tx.query('match $x isa person, has age 10; get;')
        jills_after = len(list(answers))
        self.assertGreater(jills_after, jills_before, msg="Number of entities did not increase after insert and commit")
        

    def test_commit_check_tx_closed(self):
        self.tx.commit()
        self.assertFalse(self.tx.is_open(), msg="Tx not closed after commit")

    # --- close tests ---

    def test_close_check_closed(self):
        """ Close then confirm closed """
        # attempt to perform a query/put etc 
        self.tx.close()
        with self.assertRaises(Exception):
            self.tx.query("match $x isa person; get;")

        self.assertFalse(self.tx.is_open(), msg="Tx not closed after tx.close()")

    # --- test get concept ---
    def test_get_concept(self):
        """ Test retrieving concept by concept ID """
        people_ids = [answer.get('x').id for answer in self.tx.query('match $x isa person; get;')]
        person_id = people_ids[0]
        
        with self.subTest(i=0):
            # valid ID
            same_person = self.tx.get_concept(person_id)
            self.assertTrue(same_person.is_thing(), msg="Concept retrieved is not a thing")
            self.assertEqual(same_person.id, person_id, msg="Retrieved concept does not have matching ID")
        with self.subTest(i=1):
            # invalid ID (can still be parsed as integer by server)
            none_person = self.tx.get_concept('1111122222')
            self.assertIsNone(none_person, msg="Nonexistant concept ID does not return None")
        with self.subTest(i=2):
            # invalid ID (cannot be parsed as integer by server)
            none_person = self.tx.get_concept('not_an_id')
            self.assertIsNone(none_person, msg="Invalid concept ID does not return None")

    # --- test get schema concept ---
    def test_get_schema_concept(self):
        """ Retrieve schema concept (ie a type) by label test """
        with self.subTest(i=0):
            # valid label
            person_schema_type = self.tx.get_schema_concept('person')
            self.assertTrue(person_schema_type.is_schema_concept())
        with self.subTest(i=1):
            # nonexistant label
            not_person_type = self.tx.get_schema_concept('not_a_person')
            self.assertIsNone(not_person_type, msg="Nonexistant label type does not return None")


    # --- test get attributes by value ---
    def test_get_attributes_by_value(self):
        """ Retrieve attribute instances by value """
        with self.subTest(i=0):
            # test retrieving multiple concepts
            firstname_attr_type = self.tx.put_attribute_type("firstname", ValueType.STRING)
            middlename_attr_type = self.tx.put_attribute_type("middlename", ValueType.STRING)
            firstname = firstname_attr_type.create("Billie")
            middlename = middlename_attr_type.create("Billie")
    
            attr_concepts = self.tx.get_attributes_by_value("Billie", ValueType.STRING)
            attr_concepts = list(attr_concepts) 
            self.assertEqual(len(attr_concepts), 2, msg="Do not have 2 first name attrs")
    
            ids = [attr.id for attr in attr_concepts]
            self.assertTrue(firstname.id in ids and middlename.id in ids)

        with self.subTest(i=1):
            # test retrieving no concepts
            # because we have no "Jean" attributes
            jean_attrs = list(self.tx.get_attributes_by_value("Jean", ValueType.STRING))
            self.assertEqual(len(jean_attrs), 0)

    # --- test schema modification ---
    def test_put_entity_type(self):
        """ Test get schema entity type by label """
        dog_entity_type = self.tx.put_entity_type('dog')
        self.assertTrue(dog_entity_type.is_schema_concept())
        self.assertTrue(dog_entity_type.is_entity_type())

            
    def test_put_relation_type(self):
        """ Test putting a schema relation type """
        marriage_type = self.tx.put_relation_type('marriage')
        self.assertTrue(marriage_type.is_schema_concept())
        self.assertTrue(marriage_type.is_relation_type())

    def test_put_attribute_type(self):
        """ Test putting a new attribtue type in schema """
        birthdate = self.tx.put_attribute_type("surname", ValueType.DATETIME)
        self.assertTrue(birthdate.is_schema_concept())
        self.assertTrue(birthdate.is_attribute_type())

    def test_put_role(self):
        """ Test adding a role """
        role = self.tx.put_role("spouse")
        self.assertTrue(role.is_role(), msg="created role type is not Role")
        self.assertEqual(role.base_type, "ROLE", msg="Role base_type is not ROLE")

    def test_put_rule(self):
        """ Test adding a rule for genderized parentship"""

        # create a role which creates a trivial "ancestor" relation
        label = "genderizedparentship"
        when = "{ (parent: $p, child: $c) isa parentship; $p has gender 'female'; $c has gender 'male'; };"
        then = "{ (mother: $p, son: $c) isa parentship; };"

        rule = self.tx.put_rule(label, when, then)
        self.assertTrue(rule.is_rule())
        self.assertEqual(rule.label(), label)


if __name__ == "__main__":
    with TypeDBServer():
        unittest.main(verbosity=2)
