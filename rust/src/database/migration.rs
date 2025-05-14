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
    fs::{File, OpenOptions},
    io::{BufReader, BufWriter, Read, Write},
    path::{Path, PathBuf},
};

use prost::{
    bytes::{Buf, Bytes},
    Message,
};
use typedb_protocol::{migration, migration::Item};

use crate::{
    error::{ConceptError, MigrationError, ServerError},
    Error, Result,
};

#[derive(Debug)]
pub(crate) enum DatabaseExportAnswer {
    Schema(String),
    Items(Vec<Item>),
    Done,
}

pub(crate) fn read_and_print_import_file(path: impl AsRef<Path>) -> Result {
    let items = read_and_print_import_file_inner(path.as_ref())?;

    // Temporary: test reverse-import
    let temp_path = temp_path_from(path)?;
    write_export_file(&temp_path, &items)?;
    let reimport_items = read_and_print_import_file_inner(&temp_path)?;

    if items == reimport_items {
        println!("All is good!");
    } else {
        println!("ITEMS AND REIMPORT ITEMS ARE DIFFERENT: '{items:?}' VS '{reimport_items:?}'");
    }

    Ok(())
}

pub(crate) fn read_and_print_import_file_inner(path: impl AsRef<Path>) -> Result<Vec<migration::Item>> {
    let file = try_opening_import_file(path)?;
    let mut reader = BufReader::new(file);
    let mut buffer = Vec::new();
    reader.read_to_end(&mut buffer)?;

    let mut items = Vec::new();

    let mut cursor = std::io::Cursor::new(buffer);
    while cursor.has_remaining() {
        let item = migration::Item::decode_length_delimited(&mut cursor)
            .map_err(|_| Error::Migration(MigrationError::CannotDecodeImportedConcept))?;
        println!("{:#?}", item);
        items.push(item);
    }

    Ok(items)
}

pub(crate) fn write_export_file(path: impl AsRef<Path>, items: &[migration::Item]) -> Result {
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

fn temp_path_from(path: impl AsRef<Path>) -> Result<PathBuf> {
    let path = path.as_ref();

    let mut temp_path = match path.file_name() {
        Some(name) => {
            let mut name_os = name.to_os_string();
            name_os.push(".temp");
            path.with_file_name(name_os)
        }
        None => {
            // Fallback: if there's no file name (e.g. just a directory), append ".temp"
            let mut path_buf = path.to_path_buf();
            path_buf.set_extension("temp");
            path_buf
        }
    };

    std::fs::remove_file(&temp_path)?;
    Ok(temp_path)
}
