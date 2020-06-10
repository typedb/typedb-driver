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

const DEFAULT_URI = "localhost:48555";
const INTEGRATION_TESTS_TIMEOUT = 70000;
const TEST_KEYSPACE = 'testkeyspace';

const childProcess = require('child_process');
const fs = require('fs-extra');
const path = require('path');
const tmp = require('tmp');
const unzipper = require('unzipper');
const propertiesReader = require('properties-reader');


const GraknClient = require("../../client-nodejs/src/GraknClient");

const graknClient = new GraknClient(DEFAULT_URI);

let session;
let tempRootDir;
let graknRootDir;
let graknExecutablePath;


jasmine.DEFAULT_TIMEOUT_INTERVAL = INTEGRATION_TESTS_TIMEOUT;

const unzipArchive = function(zipFile, extractPath) {
    return new Promise((resolve, reject) => {
        fs.createReadStream(zipFile)
            .pipe(unzipper.Extract({ path: extractPath }))
            .once('close', () => {
                resolve();
            });
    });
};

const execGraknCommand = (command) => {
    try {
        childProcess.execSync(command, { cwd: graknRootDir, stdio: 'inherit' });
    } catch (error) {
        throw new Error(`There was a problem when running ${command}`)
    }
}

const getServerCommand = (cmd) =>  `${graknExecutablePath} server ${cmd}`

const getLoadGraqlCommand = (filePath, keyspace) => `${graknExecutablePath} console -f ${filePath} -k ${keyspace}`;

const isGraknRunning = () => {
    const graknProperties = propertiesReader(tempRootDir + '/grakn-core-all-mac/server/conf/grakn.properties');
    const SERVER_HOST_NAME = 'server.host';
    const GRPC_PORT = 'grpc.port';
    const uri = `${graknProperties.get(SERVER_HOST_NAME)}:${graknProperties.get(GRPC_PORT)}`;
            
    try {
        childProcess.execSync(`curl ${uri}`);
        return true;      
    } catch (error) {
        return false;
    }
}

module.exports = {
    session: async () => {
        session = await graknClient.session(TEST_KEYSPACE);
        return session;
    },
    sessionForKeyspace: (keyspace) => graknClient.session(keyspace),
    tearDown: async () => {
        if(session) await session.close();
        await graknClient.close();
        execGraknCommand(getServerCommand('stop'));
        fs.removeSync(tempRootDir);
    },
    valueType: () => GraknClient.valueType,
    graknClient,
    buildParentship: async (localTx) => {
        const relationType = await localTx.putRelationType('parentship');
        const relation = await relationType.create();
        const parentRole = await localTx.putRole('parent');
        const childRole = await localTx.putRole('child');
        await relationType.relates(childRole);
        await relationType.relates(parentRole);
        const personType = await localTx.putEntityType('person');
        await personType.plays(parentRole);
        await personType.plays(childRole);
        const parent = await personType.create();
        const child = await personType.create();
        await relation.assign(childRole, child);
        await relation.assign(parentRole, parent);
        await localTx.commit();
        return {child: child.id, parent: parent.id, rel: relation.id};
    },
    startGraknServer: async () => {
        const tmpobj = tmp.dirSync();
        tempRootDir = tmpobj.name;
        tmpobj.removeCallback(); // disable automatic cleanup

        await unzipArchive('external/graknlabs_grakn_core/grakn-core-all-mac.zip', tempRootDir);

        graknRootDir = path.join(tempRootDir, 'grakn-core-all-mac');
        graknExecutablePath = path.join(graknRootDir, 'grakn');
        
        // fix permissions to not get EACCES
        fs.chmodSync(graknExecutablePath, 0o755);
        // make `/tmp` writable as running console commands creates a file in there
        fs.chmodSync('/tmp', 0o755);

        if (isGraknRunning()) {
            throw new Error('Grakn Server is already running. Stop it before running the integration tests');            
        } else {
            execGraknCommand(getServerCommand('start'));
            execGraknCommand(getLoadGraqlCommand(path.resolve('.', 'tests/support/basic-genealogy.gql'), 'gene'));
        }
    },
    beforeAllTimeout: 100000 // empirically, this should be enough to unpack, bootup Grakn and load data
}
