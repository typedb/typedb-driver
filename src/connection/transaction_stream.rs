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

use std::{fmt, iter};

use futures::{stream, Stream, StreamExt};

use super::network::transmitter::TransactionTransmitter;
use crate::{
    answer::{ConceptMap, Numeric},
    common::Result,
    connection::message::{QueryRequest, QueryResponse, TransactionRequest, TransactionResponse},
    error::InternalError,
    Options, TransactionType,
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

    pub(crate) fn type_(&self) -> TransactionType {
        self.type_
    }

    pub(crate) fn options(&self) -> &Options {
        &self.options
    }

    pub(crate) async fn commit(&self) -> Result {
        self.single(TransactionRequest::Commit).await?;
        Ok(())
    }

    pub(crate) async fn rollback(&self) -> Result {
        self.single(TransactionRequest::Rollback).await?;
        Ok(())
    }

    pub(crate) async fn define(&self, query: String, options: Options) -> Result {
        self.single(TransactionRequest::Query(QueryRequest::Define { query, options })).await?;
        Ok(())
    }

    pub(crate) async fn undefine(&self, query: String, options: Options) -> Result {
        self.single(TransactionRequest::Query(QueryRequest::Undefine { query, options })).await?;
        Ok(())
    }

    pub(crate) async fn delete(&self, query: String, options: Options) -> Result {
        self.single(TransactionRequest::Query(QueryRequest::Delete { query, options })).await?;
        Ok(())
    }

    pub(crate) fn match_(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Match { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Match { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn insert(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Insert { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Insert { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn update(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Update { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Update { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) async fn match_aggregate(&self, query: String, options: Options) -> Result<Numeric> {
        match self.query_single(QueryRequest::MatchAggregate { query, options }).await? {
            QueryResponse::MatchAggregate { answer } => Ok(answer),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    async fn single(&self, req: TransactionRequest) -> Result<TransactionResponse> {
        self.transaction_transmitter.single(req).await
    }

    async fn query_single(&self, req: QueryRequest) -> Result<QueryResponse> {
        match self.single(TransactionRequest::Query(req)).await? {
            TransactionResponse::Query(query) => Ok(query),
            other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
        }
    }

    fn stream(&self, req: TransactionRequest) -> Result<impl Stream<Item = Result<TransactionResponse>>> {
        self.transaction_transmitter.stream(req)
    }

    fn query_stream(&self, req: QueryRequest) -> Result<impl Stream<Item = Result<QueryResponse>>> {
        Ok(self.stream(TransactionRequest::Query(req))?.map(|response| match response {
            Ok(TransactionResponse::Query(query)) => Ok(query),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }
}

impl fmt::Debug for TransactionStream {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("TransactionStream").field("type_", &self.type_).field("options", &self.options).finish()
    }
}

fn stream_once<'a, T: Send + 'a>(value: T) -> stream::BoxStream<'a, T> {
    stream_iter(iter::once(value))
}

fn stream_iter<'a, T: Send + 'a>(iter: impl Iterator<Item = T> + Send + 'a) -> stream::BoxStream<'a, T> {
    Box::pin(stream::iter(iter))
}
