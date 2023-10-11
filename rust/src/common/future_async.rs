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

pub use futures::future::BoxFuture as BoxPromise;
use futures::future::Future;

pub fn box_promise<'a, T>(future: impl Promise<'a, T>) -> BoxPromise<'a, T> {
    Box::pin(future)
}

pub trait Promise<'a, T>: Future<Output = T> + Send + 'a {}
impl<'a, T, U: Future<Output = T> + Send + 'a> Promise<'a, T> for U {}

#[macro_export]
macro_rules! promisify {
    {$($fut:stmt);*} => {
        #[allow(redundant_semicolons)]
        async move { $($fut);* }
    };
}

#[macro_export]
macro_rules! resolve {
    ($fut:expr) => {
        ($fut).await
    };
}
