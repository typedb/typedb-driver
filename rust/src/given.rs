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
use std::{collections::HashMap, sync::Arc};

use chrono::{DateTime, NaiveDate, NaiveDateTime};

use crate::{
    Result,
    concept::{
        Attribute, Concept, Entity, Relation, Value,
        value::{Decimal, Duration, TimeZone},
    },
    error::{Error, QueryError},
};

#[derive(Debug, Clone)]
pub enum GivenRowEntry {
    Empty,
    Entity(Entity),
    Relation(Relation),
    Attribute(Attribute),
    Value(Value),
}

macro_rules! impl_from_for_given_row_entry {
    ($($t:ty),*) => {
        $(impl From<$t> for GivenRowEntry {
            fn from(value: $t) -> Self { Self::Value(Value::from(value)) }
        })*
    };
}

impl_from_for_given_row_entry!(bool, i64, f64, Decimal, String, NaiveDate, NaiveDateTime, DateTime<TimeZone>, Duration);

impl From<&str> for GivenRowEntry {
    fn from(value: &str) -> Self {
        Self::Value(Value::from(value))
    }
}

impl TryFrom<Concept> for GivenRowEntry {
    type Error = Error;

    fn try_from(concept: Concept) -> Result<Self> {
        match concept {
            Concept::Entity(e) => Ok(Self::Entity(e)),
            Concept::Relation(r) => Ok(Self::Relation(r)),
            Concept::Attribute(a) => Ok(Self::Attribute(a)),
            Concept::Value(v) => Ok(Self::Value(v)),

            Concept::EntityType(_) | Concept::RelationType(_) | Concept::RoleType(_) | Concept::AttributeType(_) => {
                Err(Error::Query(QueryError::InvalidTypeToGivenRow))
            }
        }
    }
}

impl From<Entity> for GivenRowEntry {
    fn from(value: Entity) -> Self {
        Self::Entity(value)
    }
}

impl From<Relation> for GivenRowEntry {
    fn from(value: Relation) -> Self {
        Self::Relation(value)
    }
}

impl From<Attribute> for GivenRowEntry {
    fn from(value: Attribute) -> Self {
        Self::Attribute(value)
    }
}

impl From<Value> for GivenRowEntry {
    fn from(value: Value) -> Self {
        Self::Value(value)
    }
}

/// Rows of data to be used as input to a TypeQL query using a `given` stage.
///
/// # Examples
///
/// ```rust
/// let query = "given $n: string, $a: integer; insert $p isa person, has name == $n, has age == $a;";
/// let mut rows = GivenRows::new(vec!["n".to_owned(), "a".to_owned()], 1);
/// rows.push_row(vec![GivenRowEntry::Value(Value::String("Alice".to_owned())), GivenRowEntry::Value(Value::Integer(28))]);
/// rows.push_row(vec!["Bob".into(), 26.into()]);
/// transaction.query_with_rows(query, rows)
/// ```
#[derive(Debug, Clone)]
pub struct GivenRows {
    pub header: Arc<GivenRowsHeader>,
    pub(crate) rows: Vec<Vec<GivenRowEntry>>,
}

impl GivenRows {
    pub fn new(variables: Vec<String>, row_count_hint: usize) -> Self {
        Self::new_with_headers(Arc::new(GivenRowsHeader::new(variables)), row_count_hint)
    }

    pub fn new_with_headers(header: Arc<GivenRowsHeader>, row_count_hint: usize) -> Self {
        let rows = Vec::with_capacity(row_count_hint);
        Self { header, rows }
    }

    pub fn into_parts(self) -> (Arc<GivenRowsHeader>, Vec<Vec<GivenRowEntry>>) {
        let Self { header, rows } = self;
        (header, rows)
    }

    pub fn push(&mut self, row: GivenRow) -> Result {
        self.push_row(row.row)
    }

    pub fn push_row(&mut self, row: Vec<GivenRowEntry>) -> Result {
        if row.len() == self.header.width() {
            self.rows.push(row);
            Ok(())
        } else {
            Err(Error::Query(QueryError::GivenRowsSizeMismatch { actual: row.len(), expected: self.header.width() }))
        }
    }

    pub fn push_map(&mut self, values: impl IntoIterator<Item = (String, GivenRowEntry)>) -> Result {
        let mut row = GivenRow::new(self.header.clone());
        values.into_iter().try_for_each(|(var, entry)| row.set(var, entry))?;
        self.push(row)
    }
}

#[derive(Debug, Clone)]
pub struct GivenRowsHeader {
    pub(crate) variables: Vec<String>,
    pub(crate) index: HashMap<String, usize>,
}

impl GivenRowsHeader {
    pub fn new(variables: Vec<String>) -> Self {
        let index = variables.iter().cloned().enumerate().map(|(i, v)| (v, i)).collect();
        Self { variables, index }
    }

    pub fn width(&self) -> usize {
        self.variables.len()
    }
}

/// Helper for building a single row to add to a <code>GivenRows</code> instance.
#[derive(Debug, Clone)]
pub struct GivenRow {
    header: Arc<GivenRowsHeader>,
    row: Vec<GivenRowEntry>,
}

impl GivenRow {
    pub fn new(header: Arc<GivenRowsHeader>) -> GivenRow {
        let mut row = Vec::with_capacity(header.width());
        row.resize(header.width(), GivenRowEntry::Empty);
        GivenRow { header, row }
    }

    pub fn width(&self) -> usize {
        self.header.width()
    }

    pub fn set(&mut self, variable: String, entry: GivenRowEntry) -> Result {
        let index =
            self.header.index.get(&variable).ok_or(Error::Query(QueryError::GivenRowUnknownVariable { variable }))?;
        self.set_at(*index, entry)
    }

    pub fn set_at(&mut self, index: usize, entry: GivenRowEntry) -> Result {
        if index < self.row.len() {
            self.row[index] = entry;
            Ok(())
        } else {
            let width = self.header.width();
            Err(Error::Query(QueryError::GivenRowIndexOutOfBounds { index, width }))
        }
    }
}
