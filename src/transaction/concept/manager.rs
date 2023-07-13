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

use std::sync::Arc;

use crate::{
    common::{stream::Stream, IID},
    concept::{Attribute, AttributeType, Entity, EntityType, Relation, RelationType, SchemaException, ValueType},
    connection::TransactionStream,
    Result,
};

#[derive(Debug)]
pub struct ConceptManager {
    pub(super) transaction_stream: Arc<TransactionStream>,
}

impl ConceptManager {
    pub(crate) fn new(transaction_stream: Arc<TransactionStream>) -> Self {
        Self { transaction_stream }
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_entity_type(&self, label: String) -> Result<Option<EntityType>> {
        self.transaction_stream.get_entity_type(label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_relation_type(&self, label: String) -> Result<Option<RelationType>> {
        self.transaction_stream.get_relation_type(label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_attribute_type(&self, label: String) -> Result<Option<AttributeType>> {
        self.transaction_stream.get_attribute_type(label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn put_entity_type(&self, label: String) -> Result<EntityType> {
        self.transaction_stream.put_entity_type(label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn put_relation_type(&self, label: String) -> Result<RelationType> {
        self.transaction_stream.put_relation_type(label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn put_attribute_type(&self, label: String, value_type: ValueType) -> Result<AttributeType> {
        self.transaction_stream.put_attribute_type(label, value_type).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_entity(&self, iid: IID) -> Result<Option<Entity>> {
        self.transaction_stream.get_entity(iid).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_relation(&self, iid: IID) -> Result<Option<Relation>> {
        self.transaction_stream.get_relation(iid).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    pub async fn get_attribute(&self, iid: IID) -> Result<Option<Attribute>> {
        self.transaction_stream.get_attribute(iid).await
    }

    pub fn get_schema_exceptions(&self) -> Result<impl Stream<Item = Result<SchemaException>>> {
        self.transaction_stream.get_schema_exceptions()
    }
}
