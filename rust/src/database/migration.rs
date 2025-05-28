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
    collections::VecDeque,
    fs::{File, OpenOptions},
    io::{BufRead, BufWriter, Read, Write},
    marker::PhantomData,
    path::Path,
};

use prost::Message;
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
    buffer: VecDeque<u8>,
    _phantom_data: PhantomData<M>,
}

impl<M: Message + Default, R: BufRead> ProtoMessageIterator<M, R> {
    const BUF_CAPACITY: usize = 1024;
    // prost's length delimiters take up to 10 bytes
    const MAX_LENGTH_DELIMITER_LEN: usize = 10;

    pub fn new(reader: R) -> Self {
        Self { reader, buffer: VecDeque::with_capacity(Self::BUF_CAPACITY), _phantom_data: PhantomData }
    }

    fn try_read_more(&mut self, bytes_to_read: usize) -> std::io::Result<usize> {
        let mut addition = vec![0; bytes_to_read];
        let bytes_read = self.reader.read(&mut addition)?;
        self.buffer.extend(&addition[..bytes_read]);
        Ok(bytes_read)
    }

    fn try_get_next_message_len(&mut self) -> Result<Option<usize>> {
        loop {
            if let Ok(len) = prost::decode_length_delimiter(&mut self.buffer) {
                return Ok(Some(len));
            } else {
                if self.buffer.len() < Self::MAX_LENGTH_DELIMITER_LEN {
                    assert!(Self::MAX_LENGTH_DELIMITER_LEN < Self::BUF_CAPACITY);
                    let to_read = max(Self::MAX_LENGTH_DELIMITER_LEN, Self::BUF_CAPACITY - self.buffer.len());
                    match self.try_read_more(to_read) {
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
                } else {
                    return Err(Error::Migration(MigrationError::CannotDecodeImportedConceptLength));
                }
            }
        }
    }

    fn get_message_buf(&mut self, len: usize) -> VecDeque<u8> {
        let message_buf = self.buffer.split_off(len);
        std::mem::replace(&mut self.buffer, message_buf)
    }
}

impl<M: Message + Default, R: BufRead> Iterator for ProtoMessageIterator<M, R> {
    type Item = Result<M>;

    fn next(&mut self) -> Option<Self::Item> {
        let message_len = match self.try_get_next_message_len() {
            Ok(Some(len)) => len,
            Ok(None) => return None,
            Err(err) => return Some(Err(err)),
        };

        if self.buffer.len() < message_len {
            let to_read = max(message_len - self.buffer.len(), Self::BUF_CAPACITY);
            if let Err(_) = self.try_read_more(to_read) {
                return Some(Err(Error::Migration(MigrationError::CannotDecodeImportedConcept)));
            }
        }

        let mut message_buf = self.get_message_buf(message_len);
        Some(M::decode(&mut message_buf).map_err(|_| Error::Migration(MigrationError::CannotDecodeImportedConcept)))
    }
}

pub(crate) fn write_export_file(path: impl AsRef<Path>, items: &[MigrationItemProto]) -> Result {
    let file = try_creating_export_file(path)?;
    let mut writer = BufWriter::new(file);

    for item in items {
        let mut buf = Vec::new();
        item.encode_length_delimited(&mut buf)
            .map_err(|_| Error::Migration(MigrationError::CannotEncodeExportedConcept))?;
        writer.write_all(&buf)?;
    }

    writer.flush()?;
    Ok(())
}

pub(crate) fn try_creating_export_file(path: impl AsRef<Path>) -> Result<File> {
    OpenOptions::new().write(true).create_new(true).open(path.as_ref()).map_err(|source| {
        Error::Migration(MigrationError::CannotCreateExportFile {
            path: path.as_ref().to_str().unwrap_or("").to_string(),
            reason: source.to_string(),
        })
    })
}

pub(crate) fn try_opening_import_file(path: impl AsRef<Path>) -> Result<File> {
    File::open(path.as_ref()).map_err(|source| {
        Error::Migration(MigrationError::CannotOpenImportFile {
            path: path.as_ref().to_str().unwrap_or("").to_string(),
            reason: source.to_string(),
        })
    })
}
