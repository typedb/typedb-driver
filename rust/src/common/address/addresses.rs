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
    collections::HashMap,
    fmt,
    fmt::{Formatter, Write},
};

use itertools::Itertools;

use crate::common::address::{address_translation::AddressTranslation, Address};

/// A collection of server addresses used for connection.
#[derive(Debug, Clone, Eq, PartialEq)]
pub enum Addresses {
    Direct(Vec<Address>),
    Translated(HashMap<Address, Address>),
}

impl Addresses {
    /// Prepare addresses based on a single "host:port" string.
    ///
    /// # Examples
    ///
    /// ```rust
    /// Addresses::try_from_address_str("127.0.0.1:11729")
    /// ```
    pub fn try_from_address_str(address_str: impl AsRef<str>) -> crate::Result<Self> {
        let address = address_str.as_ref().parse()?;
        Ok(Self::from_address(address))
    }

    /// Prepare addresses based on a single TypeDB address.
    ///
    /// # Examples
    ///
    /// ```rust
    /// let address = "127.0.0.1:11729".parse().unwrap();
    /// Addresses::from_address(address)
    /// ```
    pub fn from_address(address: Address) -> Self {
        Self::Direct(Vec::from([address]))
    }

    /// Prepare addresses based on multiple "host:port" strings.
    /// Is used to specify multiple addresses connect to.
    ///
    /// # Examples
    ///
    /// ```rust
    /// Addresses::try_from_addresses_str(["127.0.0.1:11729", "127.0.0.1:11730", "127.0.0.1:11731"])
    /// ```
    pub fn try_from_addresses_str(addresses_str: impl IntoIterator<Item = impl AsRef<str>>) -> crate::Result<Self> {
        let addresses: Vec<Address> =
            addresses_str.into_iter().map(|address_str| address_str.as_ref().parse()).try_collect()?;
        Ok(Self::from_addresses(addresses))
    }

    /// Prepare addresses based on multiple TypeDB addresses.
    ///
    /// # Examples
    ///
    /// ```rust
    /// let address1 = "127.0.0.1:11729".parse().unwrap();
    /// let address2 = "127.0.0.1:11730".parse().unwrap();
    /// let address3 = "127.0.0.1:11731".parse().unwrap();
    /// Addresses::from_addresses([address1, address2, address3])
    /// ```
    pub fn from_addresses(addresses: impl IntoIterator<Item = Address>) -> Self {
        Self::Direct(addresses.into_iter().collect())
    }

    /// Prepare addresses based on multiple key-value (public-private) "key:port" string pairs.
    /// Translation map from addresses to be used by the driver for connection to addresses received
    /// from the TypeDB server(s).
    ///
    /// # Examples
    ///
    /// ```rust
    /// Addresses::try_from_addresses_str(
    ///     [
    ///         ("typedb-cloud.ext:11729", "127.0.0.1:11729"),
    ///         ("typedb-cloud.ext:11730", "127.0.0.1:11730"),
    ///         ("typedb-cloud.ext:11731", "127.0.0.1:11731")
    ///     ].into()
    /// )
    /// ```
    pub fn try_from_translation_str(addresses_str: HashMap<impl AsRef<str>, impl AsRef<str>>) -> crate::Result<Self> {
        let mut addresses = HashMap::new();
        for (address_key, address_value) in addresses_str.into_iter() {
            addresses.insert(address_key.as_ref().parse()?, address_value.as_ref().parse()?);
        }
        Ok(Self::from_translation(addresses))
    }

    /// Prepare addresses based on multiple key-value (public-private) TypeDB address pairs.
    /// Translation map from addresses to be used by the driver for connection to addresses received
    /// from the TypeDB server(s).
    ///
    /// # Examples
    ///
    /// ```rust
    /// let translation: HashMap<Address, Address> = [
    ///     ("typedb-cloud.ext:11729".parse()?, "127.0.0.1:11729".parse()?),
    ///     ("typedb-cloud.ext:11730".parse()?, "127.0.0.1:11730".parse()?),
    ///     ("typedb-cloud.ext:11731".parse()?, "127.0.0.1:11731".parse()?)
    /// ].into();
    /// Addresses::from_translation(translation)
    /// ```
    pub fn from_translation(addresses: HashMap<Address, Address>) -> Self {
        Self::Translated(addresses)
    }

    /// Returns the number of address entries (addresses or address pairs) in the collection.
    ///
    /// # Examples
    ///
    /// ```rust
    /// addresses.len()
    /// ```
    pub fn len(&self) -> usize {
        match self {
            Addresses::Direct(vec) => vec.len(),
            Addresses::Translated(map) => map.len(),
        }
    }

    /// Checks if the public address is a part of the addresses.
    ///
    /// # Examples
    ///
    /// ```rust
    /// addresses.contains(&address)
    /// ```
    pub fn contains(&self, address: &Address) -> bool {
        match self {
            Addresses::Direct(vec) => vec.contains(address),
            Addresses::Translated(map) => map.contains_key(address),
        }
    }

    pub(crate) fn addresses(&self) -> AddressIter<'_> {
        match self {
            Addresses::Direct(vec) => AddressIter::Direct(vec.iter()),
            Addresses::Translated(map) => AddressIter::Translated(map.keys()),
        }
    }

    pub(crate) fn address_translation(&self) -> AddressTranslation {
        match self {
            Addresses::Direct(addresses) => AddressTranslation::Mapping(
                addresses.into_iter().map(|address| (address.clone(), address.clone())).collect(),
            ),
            Addresses::Translated(translation) => AddressTranslation::Mapping(translation.clone()),
        }
    }

    pub(crate) fn exclude_addresses(&mut self, excluded_addresses: &Addresses) {
        match self {
            Addresses::Direct(addresses) => {
                addresses.retain(|address| excluded_addresses.contains(address));
            }
            Addresses::Translated(translation) => translation.retain(|address, _| excluded_addresses.contains(address)),
        }
    }
}

impl fmt::Display for Addresses {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        match self {
            Addresses::Direct(addresses) => {
                f.write_char('[')?;
                for (i, address) in addresses.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, "{address}")?;
                }
                f.write_char(']')
            }
            Addresses::Translated(translation) => {
                f.write_char('{')?;
                for (i, (public, private)) in translation.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, "{public}: {private}")?;
                }
                f.write_char('}')
            }
        }
    }
}

impl Default for Addresses {
    fn default() -> Self {
        Self::Direct(Vec::default())
    }
}

pub(crate) enum AddressIter<'a> {
    Direct(std::slice::Iter<'a, Address>),
    Translated(std::collections::hash_map::Keys<'a, Address, Address>),
}

impl<'a> Iterator for AddressIter<'a> {
    type Item = &'a Address;

    fn next(&mut self) -> Option<Self::Item> {
        match self {
            AddressIter::Direct(iter) => iter.next(),
            AddressIter::Translated(iter) => iter.next(),
        }
    }
}
