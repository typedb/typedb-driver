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

use std::{fmt, iter, pin::Pin};

#[cfg(not(feature = "sync"))]
use futures::{stream, StreamExt};

use crate::{
    answer::{ConceptMap, ConceptMapGroup, readable_concept, ValueGroup},
    common::{
        Promise,
        Result, stream::{BoxStream, Stream},
    },
    concept::Value,
    connection::message::{
        QueryRequest, QueryResponse,
        TransactionRequest, TransactionResponse,
    },
    error::{ConnectionError, InternalError},
    Options, promisify, resolve, TransactionType,
};

use super::network::transmitter::TransactionTransmitter;

pub(crate) struct TransactionStream {
    type_: TransactionType,
    options: Options,
    transaction_transmitter: TransactionTransmitter,
}

impl TransactionStream {
    pub(super) fn new(
        type_: TransactionType,
        options: Options,
        transaction_transmitter: TransactionTransmitter,
    ) -> Self {
        Self { type_, options, transaction_transmitter }
    }

    pub(crate) fn is_open(&self) -> bool {
        self.transaction_transmitter.is_open()
    }

    pub(crate) fn force_close(&self) {
        self.transaction_transmitter.force_close();
    }

    pub(crate) fn type_(&self) -> TransactionType {
        self.type_
    }

    pub(crate) fn options(&self) -> Options {
        self.options
    }

    pub(crate) fn on_close(&self, callback: impl FnOnce(ConnectionError) + Send + Sync + 'static) {
        self.transaction_transmitter.on_close(callback)
    }

    pub(crate) fn commit(self: Pin<Box<Self>>) -> impl Promise<'static, Result> {
        let promise = self.transaction_transmitter.single(TransactionRequest::Commit);
        promisify! {
            let _this = self;  // move into the promise so the stream isn't dropped until the promise is resolved
            resolve!(promise).map(|_| ())
        }
    }

    pub(crate) fn rollback(&self) -> impl Promise<'_, Result> {
        let promise = self.single(TransactionRequest::Rollback);
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn define(&self, query: String, options: Options) -> impl Promise<'_, Result> {
        let promise = self.query_single(QueryRequest::Define { query, options });
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn undefine(&self, query: String, options: Options) -> impl Promise<'_, Result> {
        let promise = self.query_single(QueryRequest::Undefine { query, options });
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn delete(&self, query: String, options: Options) -> impl Promise<'_, Result> {
        let promise = self.query_single(QueryRequest::Delete { query, options });
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn get(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Get { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Get { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => {
                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
            }
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn insert(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Insert { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Insert { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => {
                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
            }
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn update(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Update { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Update { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => {
                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
            }
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn get_aggregate(&self, query: String, options: Options) -> impl Promise<'_, Result<Option<Value>>> {
        let promise = self.query_single(QueryRequest::GetAggregate { query, options });
        promisify! {
            match resolve!(promise)? {
                QueryResponse::GetAggregate { answer } => Ok(answer),
                other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
            }
        }
    }

    pub(crate) fn get_group(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        let stream = self.query_stream(QueryRequest::GetGroup { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::GetGroup { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => {
                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
            }
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn get_group_aggregate(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ValueGroup>>> {
        let stream = self.query_stream(QueryRequest::GetGroupAggregate { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::GetGroupAggregate { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => {
                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
            }
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn fetch(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<readable_concept::Tree>>> {
        let stream = self.query_stream(QueryRequest::Fetch { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Fetch { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => {
                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
            }
            Err(err) => stream_once(Err(err)),
        }))
    }

    fn single(&self, req: TransactionRequest) -> impl Promise<'static, Result<TransactionResponse>> {
        self.transaction_transmitter.single(req)
    }

    fn query_single(&self, req: QueryRequest) -> impl Promise<'_, Result<QueryResponse>> {
        let promise = self.single(TransactionRequest::Query(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::Query(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
            }
        }
    }

    fn stream(&self, req: TransactionRequest) -> Result<impl Stream<Item = Result<TransactionResponse>>> {
        self.transaction_transmitter.stream(req)
    }

    fn query_stream(&self, req: QueryRequest) -> Result<impl Stream<Item = Result<QueryResponse>>> {
        Ok(self.stream(TransactionRequest::Query(req))?.map(|response| match response {
            Ok(TransactionResponse::Query(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()),
            Err(err) => Err(err),
        }))
    }
}

impl fmt::Debug for TransactionStream {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("TransactionStream").field("type_", &self.type_).field("options", &self.options).finish()
    }
}

fn stream_once<'a, T: Send + 'a>(value: T) -> BoxStream<'a, T> {
    stream_iter(iter::once(value))
}

#[cfg(feature = "sync")]
fn stream_iter<'a, T: Send + 'a>(iter: impl Iterator<Item = T> + Send + 'a) -> BoxStream<'a, T> {
    Box::new(iter)
}

#[cfg(not(feature = "sync"))]
fn stream_iter<'a, T: Send + 'a>(iter: impl Iterator<Item = T> + Send + 'a) -> BoxStream<'a, T> {
    Box::pin(stream::iter(iter))
}
