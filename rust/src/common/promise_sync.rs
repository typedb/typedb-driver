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

pub type BoxPromise<'a, T> = Box<dyn FnOnce() -> T + 'a>;

pub fn box_promise<'a, T>(promise: impl Promise<'a, T> + 'a) -> BoxPromise<'a, T> {
    Box::new(promise)
}

/// A resolvable promise that can be resolved at a later time.
/// a `BoxPromise` is in practical terms a `Box<dyn Promise>` and resolves with `.resolve()`.
///
/// # Examples
///
/// ```rust
/// promise.resolve()
/// ```
pub trait Promise<'a, T>: FnOnce() -> T + 'a {
    fn resolve(self) -> T;
}

impl<'a, T, U: FnOnce() -> T + 'a> Promise<'a, T> for U {
    fn resolve(self) -> T {
        (self)()
    }
}

#[macro_export]
macro_rules! promisify {
    {$($promise:tt)*} => {
        #[allow(redundant_semicolons)]
        move || { $($promise)* }
    };
}

#[macro_export]
macro_rules! resolve {
    ($promise:expr $(,)?) => {
        ($promise)()
    };
}
