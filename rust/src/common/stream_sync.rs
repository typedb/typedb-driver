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

pub use std::iter::Iterator as Stream;

use tokio::sync::mpsc::UnboundedReceiver;

pub type BoxStream<'a, T> = Box<dyn Stream<Item = T> + Send + 'a>;

pub(crate) fn box_stream<'a, T>(stream: impl Iterator<Item = T> + Send + 'a) -> BoxStream<'a, T> {
    Box::new(stream) as BoxStream<'a, T>
}

pub struct NetworkStream<T> {
    receiver: UnboundedReceiver<T>,
}

impl<T> NetworkStream<T> {
    pub fn new(receiver: UnboundedReceiver<T>) -> Self {
        Self { receiver }
    }
}

impl<T> Iterator for NetworkStream<T> {
    type Item = T;
    fn next(&mut self) -> Option<Self::Item> {
        self.receiver.blocking_recv()
    }
}
