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
use typedb_driver::concept::Concept;
use typedb_driver::transaction::{QueryGivenEntry, QueryGivenRow, QueryGivenRows};
use crate::common::error::record_ffi_error;
use crate::common::memory::{borrow, borrow_mut, free, release, take_ownership};

/// Creates a new <code>givenRow</code> of the specified capacity,
#[unsafe(no_mangle)]
pub extern "C" fn given_rows_new(size_hint: usize) -> *mut QueryGivenRows {
    release(QueryGivenRows(Vec::with_capacity(size_hint)))
}

/// Creates a new <code>givenRow</code> of the specified capacity,
#[unsafe(no_mangle)]
pub extern "C" fn given_rows_push(rows: *mut QueryGivenRows, row: *mut QueryGivenRow) {
    borrow_mut(rows).0.push(take_ownership(row))
}

/// Creates a new <code>givenRow</code> of the specified width,
/// initialised with <code>QueryGivenEntry::Empty</code>
#[unsafe(no_mangle)]
pub extern "C" fn given_row_new(width: usize) -> *mut QueryGivenRow {
    let row = vec![QueryGivenEntry::Empty; width];
    release(QueryGivenRow(row))
}

/// Sets the entry at `index` in the given row to the specified entity
/// Will panic if out-of-bounds
#[unsafe(no_mangle)]
pub extern "C" fn given_row_set_index_to_concept(row: *mut QueryGivenRow, index: usize, concept: *const Concept) -> bool {
    let mut row = &mut borrow_mut(row).0;
    if let Some(entry_mut) = row.get_mut(index) {
        *entry_mut = match borrow(concept).clone() {
            Concept::Entity(entity) => QueryGivenEntry::Entity(entity),
            Concept::Relation(relation) => QueryGivenEntry::Relation(relation),
            Concept::Attribute(attribute) => QueryGivenEntry::Attribute(attribute),
            Concept::Value(value) => QueryGivenEntry::Value(value),
            Concept::EntityType(_)
            | Concept::RelationType(_)
            | Concept::RoleType(_)
            | Concept::AttributeType(_) => {
                record_ffi_error(format!("A type was passed as a given row entry at column: {index}. Only instances and values are allowed."));
                return false;
            }
        };
        true
    } else {
        record_ffi_error(format!("Given column index '{}' out of range for row of width '{}'", index, row.len()));
        false
    }
}

/// Sets the entry at `index` in the given row to the Empty Optional value
/// Will panic if out-of-bounds
#[unsafe(no_mangle)]
pub extern "C" fn given_row_set_index_to_empty(row: *mut QueryGivenRow, index: usize) -> bool {
    let mut row = &mut borrow_mut(row).0;
    if let Some(entry_mut) = row.get_mut(index) {
        *entry_mut  = QueryGivenEntry::Empty;
        true
    } else {
        record_ffi_error(format!("Given column index '{}' out of range for row of width '{}'", index, row.len()));
        false
    }
}

/// Frees the native rust <code>QueryGivenRows</code> object
#[unsafe(no_mangle)]
pub extern "C" fn given_rows_drop(rows: *mut QueryGivenRows) {
    free(rows);
}

/// Frees the native rust <code>QueryGivenRow</code> object
#[unsafe(no_mangle)]
pub extern "C" fn given_row_drop(row: *mut QueryGivenRow) {
    free(row);
}
