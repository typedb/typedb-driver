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

use std::{future::Future, thread};

use crossbeam::{atomic::AtomicCell, channel::bounded as bounded_blocking};
use tokio::{
    runtime,
    sync::mpsc::{unbounded_channel as unbounded_async, UnboundedSender},
};

use crate::common::Result;

pub(super) struct BackgroundRuntime {
    async_runtime_handle: runtime::Handle,
    is_open: AtomicCell<bool>,
    shutdown_sink: UnboundedSender<()>,
}

impl BackgroundRuntime {
    pub(super) fn new() -> Result<Self> {
        let is_open = AtomicCell::new(true);
        let (shutdown_sink, mut shutdown_source) = unbounded_async();
        let async_runtime = runtime::Builder::new_current_thread().enable_time().enable_io().build()?;
        let async_runtime_handle = async_runtime.handle().clone();
        thread::Builder::new().name("gRPC worker".to_string()).spawn(move || {
            async_runtime.block_on(async move {
                shutdown_source.recv().await;
            });
        })?;
        Ok(Self { async_runtime_handle, is_open, shutdown_sink })
    }

    pub(super) fn is_open(&self) -> bool {
        self.is_open.load()
    }

    pub(super) fn force_close(&self) -> Result {
        self.is_open.store(false);
        self.shutdown_sink.send(())?;
        Ok(())
    }

    pub(super) fn spawn<F>(&self, future: F)
    where
        F: Future + Send + 'static,
        F::Output: Send + 'static,
    {
        self.async_runtime_handle.spawn(future);
    }

    pub(super) fn run_blocking<F>(&self, future: F) -> F::Output
    where
        F: Future + Send + 'static,
        F::Output: Send + 'static,
    {
        let (response_sink, response) = bounded_blocking(0);
        self.async_runtime_handle.spawn(async move {
            response_sink.send(future.await).ok();
        });
        response.recv().unwrap()
    }
}

impl Drop for BackgroundRuntime {
    fn drop(&mut self) {
        self.is_open.store(false);
        self.shutdown_sink.send(()).ok();
    }
}
