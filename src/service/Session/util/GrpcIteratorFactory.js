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

const RequestBuilder = require("./RequestBuilder");
const AnswerFactory = require("./AnswerFactory");

/**
 * Factory of Iterators, bound to a specific transaction
 */
class GrpcIteratorFactory {
  constructor(conceptFactory, respConverter, communicator, txService) {
    this.communicator = communicator;
    this.answerFactory = new AnswerFactory(conceptFactory, txService);
    this.respConverter = respConverter;
  }

  // Query Iterator
  async createQueryIterator(startIterRequest) {
    const mapResponse = (response) => {
      const answer = response.getQueryIterRes().getAnswer();
      return this.answerFactory.createAnswer(answer);
    };
    const iterator = new Iterator(this.communicator, startIterRequest, mapResponse);
    // Extend iterator with helper method collectConcepts()
    iterator.collectConcepts = async function () { return (await this.collect()).map(a => Array.from(a.map().values())).reduce((a, c) => a.concat(c), []); };
    await iterator._start();
    return iterator;
  }

  // Concept Iterator
  async createIterator(iterateReq, responseConverter) {
    const iterator = new Iterator(this.communicator, iterateReq, responseConverter);
    await iterator._start();
    return iterator;
  }
}


class Iterator {
  constructor(communicator, startIterRequest, mapResponse) {
    this._communicator = communicator;
    this._mapResponse = mapResponse;
    this._options = startIterRequest.getOptions();
    this._startIterRequest = startIterRequest;
  }

  async _start() {
    await this._nextBatch(this._startIterRequest);
    const response = await this._iterator.next(); // Fetch first response in anticipation of query errors
    if (!response) {
      throw new Error('Iterator did not end with a Done or Iteratorid response');
    }

    this._firstResponse = response
  }

  async _nextBatch(iterRequest) {
    this._iterator = await this._communicator.iterateUntil(RequestBuilder.txIter(iterRequest), (res) => {
      const iterRes = res.getIterRes();
      return (iterRes.getDone() == true) || (iterRes.getIteratorid() > 0);
    });
  }

  async next() {
    if (this._iterator === null) { // If next is called again after the last done
      return null;
    }

    let response;
    if (this._firstResponse) {
      response = this._firstResponse;
      this._firstResponse = null;
    } else {
      response = await this._iterator.next();
    }

    if (!response) {
      throw new Error('Iterator did not end with a Done or Iteratorid response');
    }

    const iterRes = response.getIterRes();
    if (iterRes.getDone()) {
      this._iterator = null; // Reference no longer needed
      return null;
    }

    const iterId = iterRes.getIteratorid();
    if (iterId) {
      await this._nextBatch(RequestBuilder.continueIter(iterId, this._options));
      return this.next(); // Next batch messages are never returned, mapRepsonse will only see Done
    }
    
    return this._mapResponse(iterRes);
  }

  async collect() {
    const results = [];
    let result = await this.next();
    while (result) {
      results.push(result);
      result = await this.next();
    }
    return results;
  }
}

module.exports = GrpcIteratorFactory;
