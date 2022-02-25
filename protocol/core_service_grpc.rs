/*
 * Copyright (C) 2021 Vaticle
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

// This file is generated. Do not edit
// @generated

// https://github.com/Manishearth/rust-clippy/issues/702
#![allow(unknown_lints)]
#![allow(clippy::all)]

#![cfg_attr(rustfmt, rustfmt_skip)]

#![allow(box_pointers)]
#![allow(dead_code)]
#![allow(missing_docs)]
#![allow(non_camel_case_types)]
#![allow(non_snake_case)]
#![allow(non_upper_case_globals)]
#![allow(trivial_casts)]
#![allow(unsafe_code)]
#![allow(unused_imports)]
#![allow(unused_results)]


// interface

pub trait TypeDB {
    fn databases_contains(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabaseManager_Contains_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_Contains_Res>;

    fn databases_create(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabaseManager_Create_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_Create_Res>;

    fn databases_all(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabaseManager_All_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_All_Res>;

    fn database_schema(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabase_Schema_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabase_Schema_Res>;

    fn database_delete(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabase_Delete_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabase_Delete_Res>;

    fn session_open(&self, o: ::grpc::RequestOptions, p: super::session::Session_Open_Req) -> ::grpc::SingleResponse<super::session::Session_Open_Res>;

    fn session_close(&self, o: ::grpc::RequestOptions, p: super::session::Session_Close_Req) -> ::grpc::SingleResponse<super::session::Session_Close_Res>;

    fn session_pulse(&self, o: ::grpc::RequestOptions, p: super::session::Session_Pulse_Req) -> ::grpc::SingleResponse<super::session::Session_Pulse_Res>;

    fn transaction(&self, o: ::grpc::RequestOptions, p: ::grpc::StreamingRequest<super::transaction::Transaction_Client>) -> ::grpc::StreamingResponse<super::transaction::Transaction_Server>;
}

// client

pub struct TypeDBClient {
    grpc_client: ::std::sync::Arc<::grpc::Client>,
    method_databases_contains: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::core_database::CoreDatabaseManager_Contains_Req, super::core_database::CoreDatabaseManager_Contains_Res>>,
    method_databases_create: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::core_database::CoreDatabaseManager_Create_Req, super::core_database::CoreDatabaseManager_Create_Res>>,
    method_databases_all: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::core_database::CoreDatabaseManager_All_Req, super::core_database::CoreDatabaseManager_All_Res>>,
    method_database_schema: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::core_database::CoreDatabase_Schema_Req, super::core_database::CoreDatabase_Schema_Res>>,
    method_database_delete: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::core_database::CoreDatabase_Delete_Req, super::core_database::CoreDatabase_Delete_Res>>,
    method_session_open: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::session::Session_Open_Req, super::session::Session_Open_Res>>,
    method_session_close: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::session::Session_Close_Req, super::session::Session_Close_Res>>,
    method_session_pulse: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::session::Session_Pulse_Req, super::session::Session_Pulse_Res>>,
    method_transaction: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::transaction::Transaction_Client, super::transaction::Transaction_Server>>,
}

impl ::grpc::ClientStub for TypeDBClient {
    fn with_client(grpc_client: ::std::sync::Arc<::grpc::Client>) -> Self {
        TypeDBClient {
            grpc_client: grpc_client,
            method_databases_contains: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/databases_contains".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_databases_create: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/databases_create".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_databases_all: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/databases_all".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_database_schema: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/database_schema".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_database_delete: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/database_delete".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_session_open: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/session_open".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_session_close: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/session_close".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_session_pulse: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/session_pulse".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_transaction: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDB/transaction".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Bidi,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
        }
    }
}

impl TypeDB for TypeDBClient {
    fn databases_contains(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabaseManager_Contains_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_Contains_Res> {
        self.grpc_client.call_unary(o, p, self.method_databases_contains.clone())
    }

    fn databases_create(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabaseManager_Create_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_Create_Res> {
        self.grpc_client.call_unary(o, p, self.method_databases_create.clone())
    }

    fn databases_all(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabaseManager_All_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_All_Res> {
        self.grpc_client.call_unary(o, p, self.method_databases_all.clone())
    }

    fn database_schema(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabase_Schema_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabase_Schema_Res> {
        self.grpc_client.call_unary(o, p, self.method_database_schema.clone())
    }

    fn database_delete(&self, o: ::grpc::RequestOptions, p: super::core_database::CoreDatabase_Delete_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabase_Delete_Res> {
        self.grpc_client.call_unary(o, p, self.method_database_delete.clone())
    }

    fn session_open(&self, o: ::grpc::RequestOptions, p: super::session::Session_Open_Req) -> ::grpc::SingleResponse<super::session::Session_Open_Res> {
        self.grpc_client.call_unary(o, p, self.method_session_open.clone())
    }

    fn session_close(&self, o: ::grpc::RequestOptions, p: super::session::Session_Close_Req) -> ::grpc::SingleResponse<super::session::Session_Close_Res> {
        self.grpc_client.call_unary(o, p, self.method_session_close.clone())
    }

    fn session_pulse(&self, o: ::grpc::RequestOptions, p: super::session::Session_Pulse_Req) -> ::grpc::SingleResponse<super::session::Session_Pulse_Res> {
        self.grpc_client.call_unary(o, p, self.method_session_pulse.clone())
    }

    fn transaction(&self, o: ::grpc::RequestOptions, p: ::grpc::StreamingRequest<super::transaction::Transaction_Client>) -> ::grpc::StreamingResponse<super::transaction::Transaction_Server> {
        self.grpc_client.call_bidi(o, p, self.method_transaction.clone())
    }
}

// server

pub struct TypeDBServer;


impl TypeDBServer {
    pub fn new_service_def<H : TypeDB + 'static + Sync + Send + 'static>(handler: H) -> ::grpc::rt::ServerServiceDefinition {
        let handler_arc = ::std::sync::Arc::new(handler);
        ::grpc::rt::ServerServiceDefinition::new("/typedb.protocol.TypeDB",
            vec![
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/databases_contains".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.databases_contains(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/databases_create".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.databases_create(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/databases_all".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.databases_all(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/database_schema".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.database_schema(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/database_delete".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.database_delete(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/session_open".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.session_open(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/session_close".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.session_close(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/session_pulse".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.session_pulse(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDB/transaction".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Bidi,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerBidi::new(move |o, p| handler_copy.transaction(o, p))
                    },
                ),
            ],
        )
    }
}
