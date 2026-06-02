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
use std::sync::Arc;
use std::collections::HashMap;

use crate::{
    Result,
    concept::{Attribute, Entity, Relation, Value},
    error::{Error, QueryError},
};

#[derive(Debug, Clone)]
pub enum QueryGivenEntry {
    Empty,
    Entity(Entity),
    Relation(Relation),
    Attribute(Attribute),
    Value(Value),
}

#[derive(Debug, Clone)]
pub struct QueryGivenRows {
    header: GivenRowsHeader,
    rows: Vec<Vec<QueryGivenEntry>>,
}

impl QueryGivenRows {
    pub fn new(variables: Vec<String>) -> Self {
        Self::new_with_headers(GivenRowsHeader::new(variables))
    }

    fn new_with_headers(header: GivenRowsHeader) -> Self {
        let rows = Vec::new();
        Self { header, rows }
    }

    pub fn append_new_row(&mut self) -> QueryGivenRow<'_> {
        let mut row = Vec::with_capacity(self.header.width());
        row.resize(self.header.width(), QueryGivenEntry::Empty);
        self.rows.push(row);
        QueryGivenRow { header: self.header.clone(), row: self.rows.last_mut().unwrap() }
    }
}

#[derive(Debug, Clone)]
struct GivenRowsHeaderImpl {
    variables: Vec<String>,
    index: HashMap<String, usize>,
}

#[derive(Debug, Clone)]
pub struct GivenRowsHeader(Arc<GivenRowsHeaderImpl>);

impl GivenRowsHeader {
    fn new(variables: Vec<String>) -> Self {
        let index = variables.iter().cloned().enumerate().map(|(i, v)| (v, i)).collect();
        Self(Arc::new(GivenRowsHeaderImpl { variables, index }))
    }

    pub fn new_batch(&self) -> QueryGivenRows {
        QueryGivenRows::new_with_headers(self.clone())
    }

    pub fn width(&self) -> usize {
        self.0.variables.len()
    }
}



#[derive(Debug, Clone)]
pub struct QueryGivenRowImpl<T> {
    header: GivenRowsHeader,
    row: T,
}

pub type QueryGivenRow<'a> = QueryGivenRowImpl<&'a mut Vec<QueryGivenEntry>>;

impl<'a> QueryGivenRow<'a> {

    pub fn set(&mut self, variable: String, entry: QueryGivenEntry) -> Result<()> {
        let index = self.header.0.index.get(&variable).ok_or(
            Error::Query(QueryError::GivenRowUnknownVariable { variable })
        )?;
        self.set_at(*index, entry)
    }

    pub fn set_at(&mut self, index: usize, entry: QueryGivenEntry) -> Result<()> {
        if index < self.row.len() {
            self.row[index] = entry;
            Ok(())
        } else {
            let width = self.header.width();
            Err(Error::Query(QueryError::GivenRowIndexOutOfBounds { index, width }))
        }
    }
}
