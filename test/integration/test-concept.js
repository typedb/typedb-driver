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

const { TypeDB, SessionType, TransactionType, AttributeType } = require("../../dist");
const assert = require("assert");

async function run() {
    const client = TypeDB.coreClient();

    try {
        const dbs = await client.databases.all();
        console.log(`get databases - SUCCESS - the databases are [${dbs}]`);
        const typedb = dbs.find(x => x.name === "typedb");
        if (typedb) {
            await typedb.delete();
            console.log(`delete database - SUCCESS - 'typedb' has been deleted`);
        }
        await client.databases.create("typedb");
        console.log("create database - SUCCESS - 'typedb' has been created");
    } catch (err) {
        console.error(`database operations - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    ///////////////////////
    // SCHEMA OPERATIONS //
    ///////////////////////

    let session, tx;
    let lion, lionFamily, lionCub, maneSize;
    try {
        session = await client.session("typedb", SessionType.SCHEMA);
        tx = await session.transaction(TransactionType.WRITE);
        lion = await tx.concepts.putEntityType("lion");
        await tx.commit();
        await tx.close();
        console.log("putEntityType - SUCCESS");
    } catch (err) {
        console.error(`putEntityType - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        lionFamily = await tx.concepts.putRelationType("lion-family");
        await lionFamily.asRemote(tx).setRelates("lion-cub");
        lionCub = await lionFamily.asRemote(tx).getRelates().collect().then(roles => roles[0]);
        await lion.asRemote(tx).setPlays(lionCub);
        await tx.commit();
        await tx.close();
        console.log("putRelationType / setRelates / setPlays - SUCCESS");
    } catch (err) {
        console.error(`putRelationType / setRelates / setPlays - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        maneSize = await tx.concepts.putAttributeType("mane-size", AttributeType.ValueType.LONG);
        await lion.asRemote(tx).setOwns(maneSize);
        await tx.commit();
        await tx.close();
        console.log("commit attribute type + owns - SUCCESS");
    } catch (err) {
        console.error(`commit attribute type + owns - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    let stoneLion;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        stoneLion = await tx.concepts.putEntityType("stone-lion");
        await stoneLion.asRemote(tx).setSupertype(lion);
        await tx.commit();
        await tx.close();
        console.log("set supertype - SUCCESS");
    } catch (err) {
        console.error(`set supertype - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.READ);
        const supertypeOfLion = await lion.asRemote(tx).getSupertype();
        await tx.close();
        console.log(`get supertype - SUCCESS - the supertype of 'lion' is '${supertypeOfLion.label}'.`);
    } catch (err) {
        console.error(`get supertype - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.READ);
        const supertypesOfStoneLion = await stoneLion.asRemote(tx).getSupertypes().collect();
        await tx.close();
        console.log(`get supertypes - SUCCESS - the supertypes of 'stone-lion' are [${supertypesOfStoneLion.map(x => x.label)}].`);
    } catch (err) {
        console.error(`get supertypes - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.READ);
        const subtypesOfLion = await lion.asRemote(tx).getSubtypes().collect();
        await tx.close();
        console.log(`get subtypes - SUCCESS - the subtypes of 'lion' are [${subtypesOfLion.map(x => x.label)}].`);
    } catch (err) {
        console.error(`get subtypes - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const monkey = await tx.concepts.putEntityType("monkey");
        await monkey.asRemote(tx).setLabel("orangutan");
        const newLabel = await tx.concepts.getEntityType("orangutan").then(entityType => entityType.label.scopedName);
        await tx.rollback();
        await tx.close();
        assert(newLabel === "orangutan");
        console.log(`set label - SUCCESS - 'monkey' has been renamed to '${newLabel}'.`);
    } catch (err) {
        console.error(`set label - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const whale = await tx.concepts.putEntityType("whale");
        await whale.asRemote(tx).setAbstract();
        const isAbstractAfterSet = await whale.asRemote(tx).isAbstract();
        assert(isAbstractAfterSet);
        console.log(`set abstract - SUCCESS - 'whale' ${isAbstractAfterSet ? "is" : "is not"} abstract.`);
        await whale.asRemote(tx).unsetAbstract();
        const isAbstractAfterUnset = await whale.asRemote(tx).isAbstract();
        assert(!isAbstractAfterUnset);
        await tx.rollback();
        await tx.close();
        console.log(`unset abstract - SUCCESS - 'whale' ${isAbstractAfterUnset ? "is still" : "is no longer"} abstract.`);
    } catch (err) {
        console.error(`set label - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    let parentship, fathership, person, man, parent, father;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        parentship = await tx.concepts.putRelationType("parentship");
        await parentship.asRemote(tx).setRelates("parent");
        fathership = await tx.concepts.putRelationType("fathership");
        await fathership.asRemote(tx).setSupertype(parentship);
        await fathership.asRemote(tx).setRelates("father", "parent");
        person = await tx.concepts.putEntityType("person");
        parent = await parentship.asRemote(tx).getRelates("parent");
        await person.asRemote(tx).setPlays(parent);
        man = await tx.concepts.putEntityType("man");
        await man.asRemote(tx).setSupertype(person);
        father = await fathership.asRemote(tx).getRelates("father");
        await man.asRemote(tx).setPlays(father, parent);
        const playingRoles = (await man.asRemote(tx).getPlays().collect()).map(role => role.label.scopedName);
        const roleplayers = (await father.asRemote(tx).getPlayers().collect()).map(player => player.label.scopedName);
        await tx.commit();
        await tx.close();
        assert(playingRoles.includes("fathership:father"));
        assert(roleplayers.includes("man"));
        console.log(`get/set relates/plays/players, overriding a super-role - SUCCESS - 'man' plays [${playingRoles}]; 'fathership:father' is played by [${roleplayers}].`);
    } catch (err) {
        console.error(`get/set relates/plays/players, overriding a super-role - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    let email, workEmail, customer, age;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        email = await tx.concepts.putAttributeType("email", AttributeType.ValueType.STRING);
        await email.asRemote(tx).setAbstract();
        workEmail = await tx.concepts.putAttributeType("work-email", AttributeType.ValueType.STRING);
        await workEmail.asRemote(tx).setSupertype(email);
        age = await tx.concepts.putAttributeType("age", AttributeType.ValueType.LONG);
        await person.asRemote(tx).setAbstract();
        await person.asRemote(tx).setOwns(email, true);
        man = await tx.concepts.getEntityType("man");
        await man.asRemote(tx).setSupertype(await tx.concepts.getRootEntityType());
        await person.asRemote(tx).setOwns(age, false);
        await lion.asRemote(tx).setOwns(age);
        customer = await tx.concepts.putEntityType("customer");
        await customer.asRemote(tx).setSupertype(person);
        await customer.asRemote(tx).setOwns(workEmail, email, true);
        const ownedAttributes = await customer.asRemote(tx).getOwns().collect();
        const ownedKeys = await customer.asRemote(tx).getOwns(true).collect();
        const ownedDateTimes = await customer.asRemote(tx).getOwns(AttributeType.ValueType.DATETIME, false).collect();
        await tx.commit();
        await tx.close();
        assert(ownedAttributes.length === 2);
        assert(ownedKeys.length === 1);
        assert(ownedDateTimes.length === 0);
        console.log(`get/set owns, overriding a super-attribute - SUCCESS - 'customer' owns [${ownedAttributes.map(x => x.label.scopedName)}], ` +
            `of which [${ownedKeys.map(x => x.label.scopedName)}] are keys, and [${ownedDateTimes.map((x => x.label))}] are datetimes`);
    } catch (err) {
        console.error(`get/set owns, overriding a super-attribute - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        await person.asRemote(tx).unsetOwns(age);
        await person.asRemote(tx).unsetPlays(parent);
        await fathership.asRemote(tx).unsetRelates("father");
        const personOwns = (await person.asRemote(tx).getOwns().collect()).map(x => x.label.scopedName);
        const personPlays = (await person.asRemote(tx).getPlays().collect()).map(x => x.label.scopedName);
        const fathershipRelates = (await fathership.asRemote(tx).getRelates().collect()).map(x => x.label.scopedName);
        await tx.rollback();
        await tx.close();
        assert(!personOwns.includes("age"));
        assert(!personPlays.includes("parent"));
        assert(!fathershipRelates.includes("father"));
        console.log(`unset owns/plays/relates - SUCCESS - 'person' owns [${personOwns}], `
            + `'person' plays [${personPlays}], 'fathership' relates [${fathershipRelates}]`);
    } catch (err) {
        console.error(`unset owns/plays/relates - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    let password, shoeSize, volume, isAlive, startDate;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        password = await tx.concepts.putAttributeType("password", AttributeType.ValueType.STRING);
        shoeSize = await tx.concepts.putAttributeType("shoe-size", AttributeType.ValueType.LONG);
        volume = await tx.concepts.putAttributeType("volume", AttributeType.ValueType.DOUBLE);
        isAlive = await tx.concepts.putAttributeType("is-alive", AttributeType.ValueType.BOOLEAN);
        startDate = await tx.concepts.putAttributeType("start-date", AttributeType.ValueType.DATETIME);
        await tx.commit();
        await tx.close();
        console.log(`put all 5 attribute value types - SUCCESS - password is a ${password.valueType}, shoe-size is a ${shoeSize.valueType}, `
            + `volume is a ${volume.valueType}, is-alive is a ${isAlive.valueType} and start-date is a ${startDate.valueType}`);
    } catch (err) {
        console.error(`put all 5 attribute value types - ERROR: ${err.stack || err}`);
        await client.close();
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
        await client.close();
        process.exit(1);
    }

    try {
        await session.close();
        console.log("close schema session - SUCCESS");
    } catch (err) {
        console.error(`close schema session - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    /////////////////////
    // DATA OPERATIONS //
    /////////////////////

    try {
        session = await client.session("typedb", SessionType.DATA);
        tx = await session.transaction(TransactionType.WRITE);
        for (let i = 0; i < 10; i++)  stoneLion.asRemote(tx).create();
        const lions = await lion.asRemote(tx).getInstances().collect();
        const firstLion = lions[0];
        const inferred = firstLion.inferred;
        const lionType = await firstLion.asRemote(tx).type;
        const age42 = await age.asRemote(tx).put(42);
        await firstLion.asRemote(tx).setHas(age42);
        const firstLionAttrs = (await firstLion.asRemote(tx).getHas().collect()).map(x => x.value);
        assert(firstLionAttrs.length === 1);
        assert(firstLionAttrs[0] === 42);
        const firstLionAges = (await firstLion.asRemote(tx).getHas(age).collect()).map(x => x.value);
        assert(firstLionAges.length === 1)
        assert(firstLionAges[0] === 42);
        const firstLionWorkEmails = (await firstLion.asRemote(tx).getHas(workEmail).collect()).map(x => x.value);
        assert(!firstLionWorkEmails.length);
        const firstFamily = await lionFamily.asRemote(tx).create();
        await firstFamily.asRemote(tx).addPlayer(lionCub, firstLion);
        const firstLionPlaying = (await firstLion.asRemote(tx).getPlaying().collect()).map(x => x.label.scopedName);
        assert(firstLionPlaying.length === 1);
        assert(firstLionPlaying[0] === "lion-family:lion-cub");
        const firstLionRelations = await firstLion.asRemote(tx).getRelations().collect();
        assert(firstLionRelations.length === 1);
        const firstLionFatherRelations = await firstLion.asRemote(tx).getRelations([father]).collect();
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
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const firstLionFamily = (await lionFamily.asRemote(tx).getInstances().collect())[0];
        const firstLion = (await firstLionFamily.asRemote(tx).getPlayers().collect())[0];
        const firstLionFamily2 = (await firstLion.asRemote(tx).getRelations().collect())[0];
        assert(firstLionFamily2);
        let players = await firstLionFamily.asRemote(tx).getPlayers().collect();
        assert(players.length === 1);
        const lionCubPlayers = await firstLionFamily.asRemote(tx).getPlayers([lionCub]).collect();
        assert(lionCubPlayers.length === 1);
        const playersByRoleType = (await firstLionFamily.asRemote(tx).getPlayersByRoleType()).keys();
        const firstPlayer = playersByRoleType.next().value;
        assert(firstPlayer.label.scopedName === "lion-family:lion-cub");
        await firstLionFamily.asRemote(tx).removePlayer(lionCub, firstLion);
        const lionFamilyCleanedUp = await firstLionFamily.asRemote(tx).isDeleted();
        assert(lionFamilyCleanedUp);
        await tx.rollback();
        await tx.close();
        console.log(`Relation methods - SUCCESS`);
    } catch (err) {
        console.error(`Relation methods - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        const passwordAttr = await password.asRemote(tx).put("rosebud");
        const shoeSizeAttr = await shoeSize.asRemote(tx).put(9);
        const volumeAttr = await volume.asRemote(tx).put(1.618);
        const isAliveAttr = await isAlive.asRemote(tx).put(!!"hopefully");
        const startDateAttr = await startDate.asRemote(tx).put(new Date());
        await tx.commit();
        await tx.close();
        console.log(`put 5 different types of attributes - SUCCESS - password is ${passwordAttr.value}, shoe-size is ${shoeSizeAttr.value}, `
            + `volume is ${volumeAttr.value}, is-alive is ${isAliveAttr.value} and start-date is ${startDateAttr.value}`);
    } catch (err) {
        console.error(`put 5 different types of attributes - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }

    try {
        await session.close();
        await client.close();
        console.log("close session and client - SUCCESS");
    } catch (err) {
        console.error(`close session and client - ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }
}

run();
