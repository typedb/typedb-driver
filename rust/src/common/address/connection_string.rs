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

use std::{collections::HashMap, fmt, str::FromStr};

use http::Uri;

use crate::{
    common::{
        Error, Result,
        address::{Address, Addresses, DEFAULT_PORT},
    },
    error::ConnectionError,
};

const CONNECTION_STRING_SCHEME: &str = "typedb://";

/// A parsed TypeDB connection string, bundling everything needed to open a driver into a single
/// value: `typedb://<username>:<password>@<addresses>[/<database>][?<params>]`.
///
/// Addresses are comma-separated, and each one must carry an explicit `http://` or `https://`
/// scheme; all addresses must agree on it, since one TLS configuration covers the whole
/// connection. Each address may instead be a `public;private` pair, which produces
/// [`Addresses::Translated`]; pairs cannot be mixed with plain addresses.
///
/// An address without a port is assumed to be on `DEFAULT_PORT`; an explicit port is kept as
/// written. The database path segment and any query parameters are accepted and discarded: they
/// are meaningful to connection-string producers such as TypeDB Studio, but not to the driver.
///
/// # Examples
///
/// ```rust
/// let connection_string: ConnectionString = "typedb://admin:password@https://127.0.0.1:1729".parse()?;
/// ```
#[derive(Clone, PartialEq, Eq)]
pub struct ConnectionString {
    addresses: Addresses,
    username: String,
    password: String,
    is_tls_enabled: bool,
}

impl ConnectionString {
    /// Retrieves the addresses to connect to.
    pub fn addresses(&self) -> &Addresses {
        &self.addresses
    }

    /// Retrieves the username to connect as.
    pub fn username(&self) -> &str {
        &self.username
    }

    /// Retrieves the password to connect with.
    pub fn password(&self) -> &str {
        &self.password
    }

    /// Retrieves whether the addresses' scheme requires TLS.
    pub fn is_tls_enabled(&self) -> bool {
        self.is_tls_enabled
    }
}

impl FromStr for ConnectionString {
    type Err = Error;

    fn from_str(value: &str) -> Result<Self> {
        let rest = value
            .strip_prefix(CONNECTION_STRING_SCHEME)
            .ok_or_else(|| invalid(format!("expected it to start with '{CONNECTION_STRING_SCHEME}'")))?;
        let rest = rest.split_once('?').map_or(rest, |(rest, _query)| rest);
        // A password may contain an unencoded '@', so the last one separates the addresses.
        let (credentials, addresses) = rest
            .rsplit_once('@')
            .ok_or_else(|| invalid("expected credentials, separated from the addresses by '@'"))?;
        let (username, password) = credentials
            .split_once(':')
            .filter(|(username, password)| !username.is_empty() && !password.is_empty())
            .ok_or_else(|| invalid("expected a non-empty username and password, separated by ':'"))?;

        let mut tls = Vec::new();
        let mut parse = |address: &str| {
            let (address, is_tls_enabled) = parse_address(address)?;
            tls.push(is_tls_enabled);
            Ok(address)
        };
        let pairs: Vec<_> = addresses.split(',').map(|entry| entry.split_once(';')).collect();
        let addresses = if pairs.iter().all(Option::is_none) {
            Addresses::from_addresses(addresses.split(',').map(&mut parse).collect::<Result<Vec<_>>>()?)
        } else if let Some(pairs) = pairs.into_iter().collect::<Option<Vec<_>>>() {
            let translation = pairs.into_iter().map(|(public, private)| Ok((parse(public)?, parse(private)?)));
            Addresses::from_translation(translation.collect::<Result<HashMap<_, _>>>()?)
        } else {
            return Err(invalid("expected either all addresses or none to be 'public;private' translation pairs"));
        };

        let is_tls_enabled = tls[0];
        if tls.iter().any(|is_tls_enabled| *is_tls_enabled != tls[0]) {
            return Err(invalid(
                "expected all addresses to use the same scheme, as one TLS configuration applies to the whole connection",
            ));
        }
        Ok(Self {
            addresses,
            username: percent_decode(username).ok_or_else(|| invalid("expected a percent-encoded UTF-8 username"))?,
            password: percent_decode(password).ok_or_else(|| invalid("expected a percent-encoded UTF-8 password"))?,
            is_tls_enabled,
        })
    }
}

/// Deliberately omits the password, so that a connection string cannot be leaked by logging it.
impl fmt::Debug for ConnectionString {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("ConnectionString")
            .field("addresses", &self.addresses)
            .field("username", &self.username)
            .field("is_tls_enabled", &self.is_tls_enabled)
            .finish_non_exhaustive()
    }
}

/// Parses `http[s]://host[:port]` and reports whether its scheme requires TLS. A path is discarded:
/// the last address is followed by the database segment.
fn parse_address(address: &str) -> Result<(Address, bool)> {
    let malformed = || invalid(format!("expected '{address}' to be a valid address"));
    let uri = address.parse::<Uri>().map_err(|_| malformed())?;
    let (Some(scheme @ ("http" | "https")), Some(authority)) = (uri.scheme_str(), uri.authority()) else {
        return Err(invalid(format!(
            "expected address '{address}' to specify either the 'http://' or the 'https://' scheme"
        )));
    };
    let host = authority.host();
    let port = match authority.port_u16() {
        _ if host.is_empty() => return Err(malformed()),
        Some(port) => port,
        None if authority.as_str() == host => DEFAULT_PORT,
        None => return Err(malformed()),
    };
    Ok((format!("{scheme}://{host}:{port}").parse()?, scheme == "https"))
}

fn percent_decode(value: &str) -> Option<String> {
    let mut bytes = value.bytes();
    let mut decoded = Vec::with_capacity(value.len());
    while let Some(byte) = bytes.next() {
        decoded.push(match byte {
            b'%' => {
                let (high, low) = (char::from(bytes.next()?).to_digit(16)?, char::from(bytes.next()?).to_digit(16)?);
                (high * 16 + low) as u8
            }
            byte => byte,
        });
    }
    String::from_utf8(decoded).ok()
}

fn invalid(reason: impl Into<String>) -> Error {
    Error::Connection(ConnectionError::InvalidConnectionString { reason: reason.into() })
}
