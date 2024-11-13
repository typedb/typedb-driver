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

use std::{fmt, iter, pin::Pin, sync::Arc};

#[cfg(not(feature = "sync"))]
use futures::{stream, StreamExt};

use super::network::transmitter::TransactionTransmitter;
use crate::{
    answer::{concept_document::ConceptDocument, ConceptRow, QueryAnswer},
    box_stream,
    common::{
        stream::{BoxStream, Stream},
        Promise, Result,
    },
    connection::message::{QueryRequest, QueryResponse, TransactionRequest, TransactionResponse},
    error::{ConnectionError, InternalError},
    promisify, resolve, Error, Options, TransactionType,
};

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

    pub(crate) fn on_close(&self, callback: impl FnOnce(Option<Error>) + Send + Sync + 'static) {
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

    pub(crate) fn query(&self, query: &str, options: Options) -> impl Promise<'static, Result<QueryAnswer>> {
        let stream = self.query_stream(QueryRequest::Query { query: query.to_owned(), options });
        promisify! {
            let mut stream = stream?;

            #[cfg(feature = "sync")]
            let header = stream.next();
            #[cfg(not(feature = "sync"))]
            let header: Option<Result<QueryResponse>> = stream.next().await;

            let header = match header {
                None => return Err(ConnectionError::QueryStreamNoResponse.into()),
                Some(Err(err)) => return Err(err),
                Some(Ok(header)) => header,
            };

            match header {
                QueryResponse::Ok(query_type) => Ok(QueryAnswer::Ok(query_type)),
                QueryResponse::ConceptDocumentsHeader(documents_header) => {
                    let header = Arc::new(documents_header);
                    let stream_header = header.clone();
                    let answers = box_stream(stream.flat_map(move |result| {
                        let header = header.clone();
                        match result {
                            Ok(QueryResponse::StreamConceptDocuments(documents)) => {
                                stream_iter(documents.into_iter().map({
                                        move |document| {
                                            Ok(ConceptDocument::new(header.clone(), document))
                                        }
                                    }))
                            }
                            Ok(QueryResponse::Error(error)) => stream_once(Err(error.into())),
                            Ok(other) => {
                                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
                            }
                            Err(err) => stream_once(Err(err)),
                        }
                    }));
                    Ok(QueryAnswer::ConceptDocumentStream(stream_header, answers))
                },
                QueryResponse::ConceptRowsHeader(rows_header) => {
                    let header = Arc::new(rows_header);
                    let stream_header = header.clone();
                    let answers = box_stream(stream.flat_map(move |result| {
                        let header = header.clone();
                        match result {
                            Ok(QueryResponse::StreamConceptRows(rows)) => {
                                stream_iter(rows.into_iter().map({
                                    move |row| {
                                        Ok(ConceptRow::new(header.clone(), row))
                                    }
                                }))
                            }
                            Ok(QueryResponse::Error(error)) => stream_once(Err(error.into())),
                            Ok(other) => {
                                stream_once(Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into()))
                            }
                            Err(err) => stream_once(Err(err)),
                        }
                     }));
                     Ok(QueryAnswer::ConceptRowStream(stream_header, answers))
                },
                QueryResponse::Error(error) => Err(error.into()),
                other => Err(InternalError::UnexpectedResponseType { response_type: format!("{other:?}") }.into())
            }
        }
    }

    fn single(&self, req: TransactionRequest) -> impl Promise<'static, Result<TransactionResponse>> {
        self.transaction_transmitter.single(req)
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
