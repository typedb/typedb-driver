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

const Transaction = require("./Transaction");
const SessionService = require("./service/Session/SessionService");
const messages = require("../grpc/nodejs/protocol/session/Session_pb");

/**
 * List of available transaction types supported by Grakn
 */
const txType = {
    READ: messages.Transaction.Type.READ,
    WRITE: messages.Transaction.Type.WRITE
};

/**
 * Session object that can be used to:
 *  - create a new Transaction
 *
 * @param {Object} grpcClient grpc node client (session stub + channel)
 * @param {Object} credentials Optional object containing user credentials - only used when connecting to a KGMS instance
 */
class Session {
  constructor(grpcClient) {
    this.sessionService = new SessionService(grpcClient);
  }
  /**
   * Open a new Session on the server side
   * @param {String} keyspace Grakn keyspace to which this sessions should be bound to
   */
  open(keyspace, credentials) {
    return this.sessionService.open(keyspace, credentials);
  }
  /**
   * Create new Transaction, which is already open and ready to be used.
   * @param {Grakn.txType} txType Type of transaction to open READ, WRITE or BATCH
   * @returns {Transaction}
   */
  transaction() {
    return {
      read: async () => {
        const transactionService = await this.sessionService.transaction(txType.READ).catch(e => { throw e; });
        return new Transaction(transactionService);
      },
      write: async () => {
        const transactionService = await this.sessionService.transaction(txType.WRITE).catch(e => { throw e; });
        return new Transaction(transactionService);
      }
    };
  }
  /**
   * Close stream connected to gRPC server
   */
  close() {
    return this.sessionService.close();
  }
}

module.exports = Session;
