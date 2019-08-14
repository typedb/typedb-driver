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

const messages = require("../../../grpc/nodejs/protocol/keyspace/Keyspace_pb");

function KeyspaceService(grpcClient, credentials) {
    this.credentials = credentials;
    this.client = grpcClient;
}

KeyspaceService.prototype.retrieve = function () {
    const retrieveRequest = new messages.Keyspace.Retrieve.Req();
    if (this.credentials) {
        retrieveRequest.setUsername(this.credentials.username);
        retrieveRequest.setPassword(this.credentials.password);
    }
    return new Promise((resolve, reject) => {
        this.client.retrieve(retrieveRequest, (err, resp) => {
            if (err) reject(err);
            else resolve(resp.getNamesList());
        });
    })
}

KeyspaceService.prototype.delete = function (keyspace) {
    const deleteRequest = new messages.Keyspace.Delete.Req();
    deleteRequest.setName(keyspace);
    if (this.credentials) {
        deleteRequest.setUsername(this.credentials.username);
        deleteRequest.setPassword(this.credentials.password);
    }
    return new Promise((resolve, reject) => {
        this.client.delete(deleteRequest, (err) => {
            if (err) reject(err);
            else resolve();
        });
    });
}

module.exports = KeyspaceService;