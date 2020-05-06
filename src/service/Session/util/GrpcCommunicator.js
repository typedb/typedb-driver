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

class SingleResponse {
  constructor(resolve, reject) {
    this._reject = reject
    this._resolve = resolve
  }

  _onResponse(resp) {
    this._resolve(resp)
    return false
  }

  _onError(err) {
    this._reject(err)
  }
}


class MultiResponse {
  constructor(endEvaluator) {
    this._responseQueue = []
    this._readerQueue = []
    this._endEvaluator = endEvaluator
  }

  next() {
    return new Promise((resolve, reject) => {
      const responseQueue = this._responseQueue
      if (responseQueue.length > 0) {
        resolve(responseQueue.shift())
      }
      const err = this._error
      if (err) {
        reject(err)
      }
      if (this._finished) {
        resolve() // Iterator-style empty result for finished stream
      }
      this._readerQueue.push({ resolve, reject })
    })
  }

  _onResponse(response) {
    const readerQueue = this._readerQueue
    if (readerQueue.length > 0) {
      readerQueue.shift().resolve(response)
    }
    else {
      this._responseQueue.push(response)
    }
    if (this._endEvaluator(response)) {
      this._finished = true
      return false
    }
    return true
  }

  _onError(err) {
    const readerQueue = this._readerQueue
    if (readerQueue === null) {
      return // Have already received an error
    }
    for (let reader of readerQueue) {
      reader.reject(err)
    }
    this._readerQueue = null // We can never have more readers added after an error
    this._finished = true
    this._error = err
  }
}


/**
 * Wrapper for Duplex Stream that exposes method to send new requests and returns
 * responses as results of Promises.
 * @param {*} stream
 */
class GrpcCommunicator {
  constructor(stream) {
    this.stream = stream
    this.pending = []
    this.stream.on('data', resp => {
      if (!this.pending[0]._onResponse(resp)) {
        this.pending.shift() // Only remove if resolver returns falsy
      }
    })

    this.stream.on('error', err => {
      this.end()
      if (this.pending.length) {
        for (let p of this.pending) {
          p._onError(err)
        }
      }
      else {
        throw err
      }
    })

    this.stream.on('status', (e) => {
      if (this.pending.length) {
        this.pending.shift()._onError(e)
      }
    })
  }

  send(request) {
    if (!this.stream.writable)
      throw 'Transaction is already closed.'
    return new Promise((resolve, reject) => {
      this.pending.push(new SingleResponse(resolve, reject))
      this.stream.write(request)
    })
  }

  iterateUntil(request, endEvaluator) {
    if (!this.stream.writable)
      throw 'Transaction is already closed.'
    return new Promise((resolve) => {
      const responseIterator = new MultiResponse(endEvaluator)
      this.pending.push(responseIterator)
      this.stream.write(request)
      resolve(responseIterator)
    })
  }

  end() {
    if (this.stream.writable) { // transaction is still open
      this.stream.end()
      return new Promise((resolve) => {
        this.stream.on('end', resolve)
      })
    }
  }
}


module.exports = GrpcCommunicator;