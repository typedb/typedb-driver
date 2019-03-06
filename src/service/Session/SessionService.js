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

const TxService = require("./TransactionService");
const RequestBuilder = require("./util/RequestBuilder");

/**
 * This creates a new connection to the server over HTTP2,
 * the connection will contain all the Transaction streams
 */
function SessionService(grpcClient, credentials) {
    this.credentials = credentials;
    this.client = grpcClient;
}

/**
 * This sends an open Session request and retrieves the sessionId that will be needed
 * to open a Transaction.
 */
SessionService.prototype.open = async function open(keyspace){
    const openResponse = await wrapInPromise(this.client, this.client.open, RequestBuilder.openSession(keyspace));
    this.sessionId = openResponse.getSessionid();
}

/**
 * This method creates a new Duplex Stream (this.client.transaction()) over which gRPC will communicate when
 * exchanging messages related to the Transaction service.
 * It also sends an Open request before returning the TransactionService
 * @param {Grakn.txType} txType type of transaction to be open
 */
SessionService.prototype.transaction = async function create(txType) {
    const txService = new TxService(this.client.transaction());
    await txService.openTx(this.sessionId, txType, this.credentials);
    return txService;
}

/**
 * Closes connection to the server
 */
SessionService.prototype.close = function close() {
    return wrapInPromise(this.client, this.client.close, RequestBuilder.closeSession(this.sessionId));
}


function wrapInPromise(self, fn, requestMessage){
    return new Promise((resolve, reject) => {
        fn.call(self, requestMessage, (error, response) => {
            if (error) { reject(error); }
            resolve(response);
        });
    });
}

module.exports = SessionService;