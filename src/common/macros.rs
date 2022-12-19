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

#[macro_export]
macro_rules! async_enum_dispatch {
    {
        $variants:tt
        $($vis:vis async fn $name:ident(&mut self, $arg:ident : $arg_type:ty $(,)?) -> $res:ty);+ $(;)?
    } => { $(async_enum_dispatch!(@impl $variants, $vis, $name, $arg, $arg_type, $res);)+ };

    (@impl {$($variant:ident),+}, $vis:vis, $name:ident, $arg:ident, $arg_type:ty, $res:ty) => {
        $vis async fn $name(&mut self, $arg: $arg_type) -> $res {
            match self {
                $(Self::$variant(inner) => inner.$name($arg).await,)+
            }
        }
    }
}
