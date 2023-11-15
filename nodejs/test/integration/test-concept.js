/*
 * Copyright (C) 2022 Vaticle
 *
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

const {TypeDB, SessionType, TransactionType, Concept, ThingType} = require("../../dist");
const assert = require("assert");
const Annotation = ThingType.Annotation;

async function run() {
    const driver = await TypeDB.coreDriver();

    try {
        const dbs = await driver.databases.all();
        console.log(`get databases - SUCCESS - the databases are [${dbs}]`);
        const typedb = dbs.find(x => x.name === "typedb");
        if (typedb) {
            await typedb.delete();
            console.log(`delete database - SUCCESS - 'typedb' has been deleted`);
        }
        await driver.databases.create("typedb");
        console.log("create database - SUCCESS - 'typedb' has been created");
    } catch (err) {
        console.error(`database operations - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    ///////////////////////
    // SCHEMA OPERATIONS //
    ///////////////////////

    let session, tx;
    let lion, lionFamily, lionCub, maneSize;
    try {
        session = await driver.session("typedb", SessionType.SCHEMA);
        tx = await session.transaction(TransactionType.WRITE);
        lion = await tx.concepts.putEntityType("lion");
        await tx.commit();
        await tx.close();
        console.log("putEntityType - SUCCESS");
    } catch (err) {
        console.error(`putEntityType - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        lionFamily = await tx.concepts.putRelationType("lion-family");
        await lionFamily.setRelates(tx, "lion-cub");
        lionCub = await lionFamily.getRelates(tx).collect().then(roles => roles[0]);
        await lion.setPlays(tx, lionCub);
        await tx.commit();
        await tx.close();
        console.log("putRelationType / setRelates / setPlays - SUCCESS");
    } catch (err) {
        console.error(`putRelationType / setRelates / setPlays - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        maneSize = await tx.concepts.putAttributeType("mane-size", Concept.ValueType.LONG);
        await lion.setOwns(tx, maneSize);
        await tx.commit();
        await tx.close();
        console.log("commit attribute type + owns - SUCCESS");
    } catch (err) {
        console.error(`commit attribute type + owns - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    let stoneLion;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        stoneLion = await tx.concepts.putEntityType("stone-lion");
        await stoneLion.setSupertype(tx, lion);
        await tx.commit();
        await tx.close();
        console.log("set supertype - SUCCESS");
    } catch (err) {
        console.error(`set supertype - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.READ);
        const supertypeOfLion = await lion.getSupertype(tx);
        await tx.close();
        console.log(`get supertype - SUCCESS - the supertype of 'lion' is '${supertypeOfLion.label}'.`);
    } catch (err) {
        console.error(`get supertype - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.READ);
        const supertypesOfStoneLion = await stoneLion.getSupertypes(tx).collect();
        await tx.close();
        console.log(`get supertypes - SUCCESS - the supertypes of 'stone-lion' are [${supertypesOfStoneLion.map(x => x.label)}].`);
    } catch (err) {
        console.error(`get supertypes - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.READ);
        const subtypesOfLion = await lion.getSubtypes(tx).collect();
        await tx.close();
        console.log(`get subtypes - SUCCESS - the subtypes of 'lion' are [${subtypesOfLion.map(x => x.label)}].`);
    } catch (err) {
        console.error(`get subtypes - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const monkey = await tx.concepts.putEntityType("monkey");
        await monkey.setLabel(tx, "orangutan");
        const newLabel = await tx.concepts.getEntityType("orangutan").then(entityType => entityType.label.scopedName);
        await tx.rollback();
        await tx.close();
        assert(newLabel === "orangutan");
        console.log(`set label - SUCCESS - 'monkey' has been renamed to '${newLabel}'.`);
    } catch (err) {
        console.error(`set label - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        let whale = await tx.concepts.putEntityType("whale");
        await whale.setAbstract(tx);
        whale = await tx.concepts.getEntityType("whale");
        const isAbstractAfterSet = whale.abstract;
        assert(isAbstractAfterSet);
        console.log(`set abstract - SUCCESS - 'whale' ${isAbstractAfterSet ? "is" : "is not"} abstract.`);
        await whale.unsetAbstract(tx);
        whale = await tx.concepts.getEntityType("whale");
        const isAbstractAfterUnset = whale.abstract;
        assert(!isAbstractAfterUnset);
        await tx.rollback();
        await tx.close();
        console.log(`unset abstract - SUCCESS - 'whale' ${isAbstractAfterUnset ? "is still" : "is no longer"} abstract.`);
    } catch (err) {
        console.error(`set label - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    let parentship, fathership, person, man, parent, father;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        parentship = await tx.concepts.putRelationType("parentship");
        await parentship.setRelates(tx, "parent");
        fathership = await tx.concepts.putRelationType("fathership");
        await fathership.setSupertype(tx, parentship);
        await fathership.setRelates(tx, "father", "parent");
        person = await tx.concepts.putEntityType("person");
        parent = await parentship.getRelatesForRoleLabel(tx, "parent");
        await person.setPlays(tx, parent);
        man = await tx.concepts.putEntityType("man");
        await man.setSupertype(tx, person);
        father = await fathership.getRelatesForRoleLabel(tx, "father");
        await man.setPlays(tx, father, parent);
        const playingRoles = (await man.getPlays(tx).collect()).map(role => role.label.scopedName);
        const roleplayers = (await father.getPlayerTypes(tx).collect()).map(player => player.label.scopedName);
        await tx.commit();
        await tx.close();
        assert(playingRoles.includes("fathership:father"));
        assert(roleplayers.includes("man"));
        console.log(`get/set relates/plays/players, overriding a super-role - SUCCESS - 'man' plays [${playingRoles}]; 'fathership:father' is played by [${roleplayers}].`);
    } catch (err) {
        console.error(`get/set relates/plays/players, overriding a super-role - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    let email, workEmail, customer, age;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        email = await tx.concepts.putAttributeType("email", Concept.ValueType.STRING);
        await email.setAbstract(tx);
        workEmail = await tx.concepts.putAttributeType("work-email", Concept.ValueType.STRING);
        await workEmail.setSupertype(tx, email);
        age = await tx.concepts.putAttributeType("age", Concept.ValueType.LONG);
        await person.setAbstract(tx);
        await person.setOwns(tx, email, [Annotation.KEY]);
        man = await tx.concepts.getEntityType("man");
        await man.setSupertype(tx, await tx.concepts.getRootEntityType());
        await person.setOwns(tx, age);
        await lion.setOwns(tx, age);
        customer = await tx.concepts.putEntityType("customer");
        await customer.setSupertype(tx, person);
        await customer.setOwns(tx, workEmail, email);
        const ownedAttributes = await customer.getOwns(tx).collect();
        const ownedKeys = await customer.getOwns(tx, [Annotation.KEY]).collect();
        const ownedDateTimes = await customer.getOwns(tx, Concept.ValueType.DATETIME, []).collect();
        await tx.commit();
        await tx.close();
        assert(ownedAttributes.length === 2);
        assert(ownedKeys.length === 1);
        assert(ownedDateTimes.length === 0);
        console.log(`get/set owns, overriding a super-attribute - SUCCESS - 'customer' owns [${ownedAttributes.map(x => x.label.scopedName)}], ` +
            `of which [${ownedKeys.map(x => x.label.scopedName)}] are keys, and [${ownedDateTimes.map((x => x.label))}] are datetimes`);
    } catch (err) {
        console.error(`get/set owns, overriding a super-attribute - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        await person.unsetOwns(tx, age);
        await person.unsetPlays(tx, parent);
        await fathership.unsetRelates(tx, "father");
        const personOwns = (await person.getOwns(tx).collect()).map(x => x.label.scopedName);
        const personPlays = (await person.getPlays(tx).collect()).map(x => x.label.scopedName);
        const fathershipRelates = (await fathership.getRelates(tx).collect()).map(x => x.label.scopedName);
        await tx.rollback();
        await tx.close();
        assert(!personOwns.includes("age"));
        assert(!personPlays.includes("parent"));
        assert(!fathershipRelates.includes("father"));
        console.log(`unset owns/plays/relates - SUCCESS - 'person' owns [${personOwns}], `
            + `'person' plays [${personPlays}], 'fathership' relates [${fathershipRelates}]`);
    } catch (err) {
        console.error(`unset owns/plays/relates - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    let password, shoeSize, volume, isAlive, startDate;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        password = await tx.concepts.putAttributeType("password", Concept.ValueType.STRING);
        shoeSize = await tx.concepts.putAttributeType("shoe-size", Concept.ValueType.LONG);
        volume = await tx.concepts.putAttributeType("volume", Concept.ValueType.DOUBLE);
        isAlive = await tx.concepts.putAttributeType("is-alive", Concept.ValueType.BOOLEAN);
        startDate = await tx.concepts.putAttributeType("start-date", Concept.ValueType.DATETIME);
        await tx.commit();
        await tx.close();
        console.log(`put all 5 attribute value types - SUCCESS - password is a ${password.valueType}, shoe-size is a ${shoeSize.valueType}, `
            + `volume is a ${volume.valueType}, is-alive is a ${isAlive.valueType} and start-date is a ${startDate.valueType}`);
    } catch (err) {
        console.error(`put all 5 attribute value types - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        await tx.logic.putRule("septuagenarian-rule", "{$x isa person;}", "$x has age 70");
        await tx.commit();
        await tx.close();
        console.log(`put rule - SUCCESS`);
    } catch (err) {
        console.error(`put rule - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        await session.close();
        console.log("close schema session - SUCCESS");
    } catch (err) {
        console.error(`close schema session - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    /////////////////////
    // DATA OPERATIONS //
    /////////////////////

    try {
        session = await driver.session("typedb", SessionType.DATA);
        tx = await session.transaction(TransactionType.WRITE);
        for (let i = 0; i < 10; i++) stoneLion.create(tx);
        const lions = await lion.getInstances(tx).collect();
        const firstLion = lions[0];
        const inferred = firstLion.inferred;
        const lionType = await firstLion.type;
        const age42 = await age.putLong(tx, 42);
        await firstLion.setHas(tx, age42);
        const firstLionAttrs = (await firstLion.getHas(tx).collect()).map(x => x.value);
        assert(firstLionAttrs.length === 1);
        assert(firstLionAttrs[0] === 42);
        const firstLionAges = (await firstLion.getHas(tx, age).collect()).map(x => x.value);
        assert(firstLionAges.length === 1)
        assert(firstLionAges[0] === 42);
        const firstLionWorkEmails = (await firstLion.getHas(tx, workEmail).collect()).map(x => x.value);
        assert(!firstLionWorkEmails.length);
        const firstFamily = await lionFamily.create(tx);
        await firstFamily.addRolePlayer(tx, lionCub, firstLion);
        const firstLionPlaying = (await firstLion.getPlaying(tx).collect()).map(x => x.label.scopedName);
        assert(firstLionPlaying.length === 1);
        assert(firstLionPlaying[0] === "lion-family:lion-cub");
        const firstLionRelations = await firstLion.getRelations(tx).collect();
        assert(firstLionRelations.length === 1);
        const firstLionFatherRelations = await firstLion.getRelations(tx, [father]).collect();
        assert(!firstLionFatherRelations.length);
        await tx.commit();
        await tx.close();
        assert(lions.length === 10);
        assert(!inferred);
        console.log(`Thing methods - SUCCESS - There are ${lions.length} lions.`);
        assert(lionType.label.scopedName === "stone-lion");
        console.log(`getType - SUCCESS - After looking more closely, it turns out that there are ${lions.length} stone lions.`);
    } catch (err) {
        console.error(`Thing methods - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const firstLionFamily = (await lionFamily.getInstances(tx).collect())[0];
        const firstLion = (await firstLionFamily.getPlayersByRoleType(tx).collect())[0];
        const firstLionFamily2 = (await firstLion.getRelations(tx).collect())[0];
        assert(firstLionFamily2);
        let players = await firstLionFamily.getPlayersByRoleType(tx).collect();
        assert(players.length === 1);
        const lionCubPlayers = await firstLionFamily.getPlayersByRoleType(tx, [lionCub]).collect();
        assert(lionCubPlayers.length === 1);
        const playersByRoleType = (await firstLionFamily.getRolePlayers(tx)).keys();
        const firstPlayer = playersByRoleType.next().value;
        assert(firstPlayer.label.scopedName === "lion-family:lion-cub");
        await firstLionFamily.removeRolePlayer(tx, lionCub, firstLion);
        const emptyLionFamilyExists = !(await firstLionFamily.isDeleted(tx));
        assert(emptyLionFamilyExists);
        await tx.rollback();
        await tx.close();
        console.log(`Relation methods - SUCCESS`);
    } catch (err) {
        console.error(`Relation methods - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const passwordAttr = await password.putString(tx, "rosebud");
        const shoeSizeAttr = await shoeSize.putLong(tx, 9);
        const volumeAttr = await volume.putDouble(tx, 1.618);
        const isAliveAttr = await isAlive.putBoolean(tx, !!"hopefully");
        const startDateAttr = await startDate.putDateTime(tx, new Date());
        await tx.commit();
        await tx.close();
        console.log(`put 5 different types of attributes - SUCCESS - password is ${passwordAttr.value}, shoe-size is ${shoeSizeAttr.value}, `
            + `volume is ${volumeAttr.value}, is-alive is ${isAliveAttr.value} and start-date is ${startDateAttr.value}`);
    } catch (err) {
        console.error(`put 5 different types of attributes - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        await session.close();
        await driver.close();
        console.log("close session and driver - SUCCESS");
    } catch (err) {
        console.error(`close session and driver - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }
}

run();
