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
use tonic::Status;
use typedb_protocol::server::Address;

macro_rules! decode_from_status {
    ($status:expr, $target_ty:ty) => {{
        decode_from_status!($status, $target_ty, stringify!($target_ty))
    }};

    ($status:expr, $target_ty:ty, $target_suffix:expr) => {{
        use std::io::Cursor;

        use prost::{bytes::Buf, Message};
        use prost_types::Any;

        let mut buf = Cursor::new($status.details());
        while buf.has_remaining() {
            if let Ok(detail) = Any::decode_length_delimited(&mut buf) {
                if detail.type_url.ends_with($target_suffix) {
                    let mut value_buf = Cursor::new(&detail.value);
                    if let Ok(decoded) = <$target_ty>::decode(&mut value_buf) {
                        return Some(decoded);
                    }
                }
            } else {
                break;
            }
        }
        None
    }};
}

pub(crate) fn decode_address(status: &Status) -> Option<Address> {
    decode_from_status!(status, Address)
}
