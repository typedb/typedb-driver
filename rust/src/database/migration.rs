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

use std::{
    cmp::max,
    fs::{File, OpenOptions},
    io::{BufRead},
    marker::PhantomData,
    path::Path,
};

use prost::{
    bytes::{Buf, BytesMut},
    Message,
};
use typedb_protocol::migration::Item as MigrationItemProto;

use crate::{error::MigrationError, Error, Result};

#[derive(Debug)]
pub(crate) enum DatabaseExportAnswer {
    Schema(String),
    Items(Vec<MigrationItemProto>),
    Done,
}

pub struct ProtoMessageIterator<M: Message + Default, R: BufRead> {
    reader: R,
    buffer: BytesMut,
    _phantom_data: PhantomData<M>,
}

impl<M: Message + Default, R: BufRead> ProtoMessageIterator<M, R> {
    const BUF_CAPACITY: usize = 8 * 1024;
    // prost's length delimiters take up to 10 bytes
    const MAX_LENGTH_DELIMITER_LEN: usize = 10;

    pub fn new(reader: R) -> Self {
        Self { reader, buffer: BytesMut::with_capacity(Self::BUF_CAPACITY), _phantom_data: PhantomData }
    }

    fn read_more(&mut self, bytes_to_read: usize) -> std::io::Result<usize> {
        if self.buffer.capacity() - self.buffer.len() < bytes_to_read {
            self.buffer.reserve(max(bytes_to_read, Self::BUF_CAPACITY));
        }
        let mut addition = vec![0u8; max(bytes_to_read, 1)];
        let bytes_read = self.reader.read(&mut addition)?;
        self.buffer.extend_from_slice(&addition[..bytes_read]);
        Ok(bytes_read)
    }

    fn decode_next_len(&mut self) -> Result<Option<(usize /*len*/, usize /*consumed*/)>> {
        loop {
            let mut cursor: &[u8] = &self.buffer;
            match prost::decode_length_delimiter(&mut cursor) {
                Ok(len) => {
                    let consumed = self.buffer.len() - cursor.len();
                    return Ok(Some((len, consumed)));
                }
                Err(_) => {
                    if self.buffer.len() >= Self::MAX_LENGTH_DELIMITER_LEN {
                        return Err(Error::Migration(MigrationError::CannotDecodeImportedConceptLength));
                    }
                    match self.read_more(Self::MAX_LENGTH_DELIMITER_LEN - self.buffer.len()) {
                        Ok(bytes_read) if bytes_read == 0 => {
                            return if self.buffer.is_empty() {
                                Ok(None)
                            } else {
                                Err(Error::Migration(MigrationError::CannotDecodeImportedConceptLength))
                            };
                        }
                        Err(_) => return Err(Error::Migration(MigrationError::CannotDecodeImportedConceptLength)),
                        Ok(_) => continue,
                    }
                }
            }
        }
    }
}

impl<M: Message + Default, R: BufRead> Iterator for ProtoMessageIterator<M, R> {
    type Item = Result<M>;

    fn next(&mut self) -> Option<Self::Item> {
        let (message_len, consumed) = match self.decode_next_len() {
            Ok(Some(res)) => res,
            Ok(None) => return None,
            Err(err) => return Some(Err(err)),
        };

        let required = consumed + message_len;
        while self.buffer.len() < required {
            let to_read = required - self.buffer.len();
            match self.read_more(max(to_read, Self::BUF_CAPACITY)) {
                Ok(0) | Err(_) => return Some(Err(Error::Migration(MigrationError::CannotDecodeImportedConcept))),
                Ok(_) => {}
            }
        }

        self.buffer.advance(consumed);
        let message_bytes = self.buffer.split_to(message_len).freeze();
        Some(M::decode(message_bytes).map_err(|_| Error::Migration(MigrationError::CannotDecodeImportedConcept)))
    }
}

pub(crate) fn try_create_export_file(path: impl AsRef<Path>) -> Result<File> {
    try_open_export_file(path, true)
}

pub(crate) fn try_open_existing_export_file(path: impl AsRef<Path>) -> Result<File> {
    try_open_export_file(path, false)
}

fn try_open_export_file(path: impl AsRef<Path>, is_new: bool) -> Result<File> {
    OpenOptions::new().write(true).create_new(is_new).open(path.as_ref()).map_err(|source| {
        Error::Migration(MigrationError::CannotCreateExportFile {
            path: path.as_ref().to_str().unwrap_or("").to_string(),
            reason: source.to_string(),
        })
    })
}

pub(crate) fn try_open_import_file(path: impl AsRef<Path>) -> Result<File> {
    File::open(path.as_ref()).map_err(|source| {
        Error::Migration(MigrationError::CannotOpenImportFile {
            path: path.as_ref().to_str().unwrap_or("").to_string(),
            reason: source.to_string(),
        })
    })
}
