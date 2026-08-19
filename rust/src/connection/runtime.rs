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

use std::{future::Future, thread, thread::JoinHandle, time::Duration};

use crossbeam::{
    atomic::AtomicCell,
    channel::{Sender, bounded as bounded_blocking, unbounded},
};
use tokio::{
    runtime,
    sync::{
        mpsc::{UnboundedSender, unbounded_channel as unbounded_async},
        oneshot::Sender as AsyncOneshotSender,
    },
};
use tracing::error;

use crate::common::{Callback, Result};

const RUNTIME_SHUTDOWN_TIMEOUT: Duration = Duration::from_secs(10);

pub(super) enum CallbackMessage {
    Invoke(Callback, AsyncOneshotSender<()>),
    Shutdown,
}

pub(crate) struct BackgroundRuntime {
    async_runtime_handle: runtime::Handle,
    is_open: AtomicCell<bool>,
    shutdown_sink: UnboundedSender<()>,

    async_runtime_worker: Option<JoinHandle<()>>,
    callback_handler: Option<JoinHandle<()>>,
    callback_handler_sink: Option<Sender<CallbackMessage>>,
}

impl BackgroundRuntime {
    pub(crate) fn new() -> Result<Self> {
        let is_open = AtomicCell::new(true);
        let (shutdown_sink, mut shutdown_source) = unbounded_async();
        let async_runtime = runtime::Builder::new_current_thread().enable_time().enable_io().build()?;
        let async_runtime_handle = async_runtime.handle().clone();
        let async_runtime_worker = thread::Builder::new().name("gRPC worker".to_owned()).spawn(move || {
            async_runtime.block_on(async move {
                shutdown_source.recv().await;
            });
            async_runtime.shutdown_timeout(RUNTIME_SHUTDOWN_TIMEOUT);
        })?;

        let (callback_handler_sink, callback_handler_source) = unbounded::<CallbackMessage>();
        let callback_handler = Some(thread::Builder::new().name("Callback handler".to_owned()).spawn(move || {
            while let Ok(message) = callback_handler_source.recv() {
                match message {
                    CallbackMessage::Invoke(callback, response_sink) => {
                        callback();
                        response_sink.send(()).ok();
                    }
                    CallbackMessage::Shutdown => break,
                }
            }
        })?);

        Ok(Self {
            async_runtime_handle,
            is_open,
            shutdown_sink,
            async_runtime_worker: Some(async_runtime_worker),
            callback_handler,
            callback_handler_sink: Some(callback_handler_sink),
        })
    }

    pub(super) fn callback_handler_sink(&self) -> Sender<CallbackMessage> {
        self.callback_handler_sink.clone().unwrap()
    }

    pub(crate) fn is_open(&self) -> bool {
        self.is_open.load()
    }

    pub(crate) fn force_close(&self) -> Result {
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

        // Returning before the runtime has stopped lets its teardown race whatever the caller does next -
        // most damagingly process exit, where it runs concurrently with libc and sanitiser cleanup and
        // crashes at random. Waiting here is bounded by the worker's own shutdown timeout.
        //
        // The callback handler is stopped only afterwards: tasks being torn down may still dispatch close
        // callbacks to it, and would deadlock if it were gone.
        if let Some(worker) = self.async_runtime_worker.take() {
            if let Err(err) = worker.join() {
                error!("Error shutting down the gRPC worker thread: {:?}", err);
            }
        }

        if let Some(callback_handler_sink) = self.callback_handler_sink.take() {
            callback_handler_sink.send(CallbackMessage::Shutdown).ok();
        }
        if let Some(callback_handler) = self.callback_handler.take() {
            // A close callback owning the driver releases the last reference from the handler thread
            // itself, and joining a thread from within it aborts. It stops on the message sent above
            // instead, once this callback returns.
            if callback_handler.thread().id() != thread::current().id()
                && let Err(err) = callback_handler.join()
            {
                error!("Error shutting down the callback handler thread: {:?}", err);
            }
        }
    }
}
