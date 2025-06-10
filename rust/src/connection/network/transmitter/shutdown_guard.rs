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
use std::ops::Deref;

use tokio::sync::mpsc::UnboundedSender;

// Makes sure that the shutdown signal is sent when it's dropped. Can try sending redundant shutdown
// signals after another shutdown source: the channel is expected to be closed (no-op).
pub(crate) struct ShutdownGuard<T: Default> {
    shutdown_sender: UnboundedSender<T>,
}

impl<T: Default> ShutdownGuard<T> {
    pub(crate) fn new(shutdown_sender: UnboundedSender<T>) -> Self {
        Self { shutdown_sender }
    }
}

impl<T: Default> Deref for ShutdownGuard<T> {
    type Target = UnboundedSender<T>;

    fn deref(&self) -> &Self::Target {
        &self.shutdown_sender
    }
}

impl<T: Default> Drop for ShutdownGuard<T> {
    fn drop(&mut self) {
        self.shutdown_sender.send(T::default()).ok();
    }
}
