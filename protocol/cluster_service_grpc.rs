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

pub trait TypeDBCluster {
    fn servers_all(&self, o: ::grpc::RequestOptions, p: super::cluster_server::ServerManager_All_Req) -> ::grpc::SingleResponse<super::cluster_server::ServerManager_All_Res>;

    fn users_contains(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUserManager_Contains_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_Contains_Res>;

    fn users_create(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUserManager_Create_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_Create_Res>;

    fn users_all(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUserManager_All_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_All_Res>;

    fn user_password(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUser_Password_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Password_Res>;

    fn user_token(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUser_Token_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Token_Res>;

    fn user_delete(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUser_Delete_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Delete_Res>;

    fn databases_get(&self, o: ::grpc::RequestOptions, p: super::cluster_database::ClusterDatabaseManager_Get_Req) -> ::grpc::SingleResponse<super::cluster_database::ClusterDatabaseManager_Get_Res>;

    fn databases_all(&self, o: ::grpc::RequestOptions, p: super::cluster_database::ClusterDatabaseManager_All_Req) -> ::grpc::SingleResponse<super::cluster_database::ClusterDatabaseManager_All_Res>;
}

// client

pub struct TypeDBClusterClient {
    grpc_client: ::std::sync::Arc<::grpc::Client>,
    method_servers_all: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_server::ServerManager_All_Req, super::cluster_server::ServerManager_All_Res>>,
    method_users_contains: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_user::ClusterUserManager_Contains_Req, super::cluster_user::ClusterUserManager_Contains_Res>>,
    method_users_create: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_user::ClusterUserManager_Create_Req, super::cluster_user::ClusterUserManager_Create_Res>>,
    method_users_all: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_user::ClusterUserManager_All_Req, super::cluster_user::ClusterUserManager_All_Res>>,
    method_user_password: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_user::ClusterUser_Password_Req, super::cluster_user::ClusterUser_Password_Res>>,
    method_user_token: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_user::ClusterUser_Token_Req, super::cluster_user::ClusterUser_Token_Res>>,
    method_user_delete: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_user::ClusterUser_Delete_Req, super::cluster_user::ClusterUser_Delete_Res>>,
    method_databases_get: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_database::ClusterDatabaseManager_Get_Req, super::cluster_database::ClusterDatabaseManager_Get_Res>>,
    method_databases_all: ::std::sync::Arc<::grpc::rt::MethodDescriptor<super::cluster_database::ClusterDatabaseManager_All_Req, super::cluster_database::ClusterDatabaseManager_All_Res>>,
}

impl ::grpc::ClientStub for TypeDBClusterClient {
    fn with_client(grpc_client: ::std::sync::Arc<::grpc::Client>) -> Self {
        TypeDBClusterClient {
            grpc_client: grpc_client,
            method_servers_all: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/servers_all".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_users_contains: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/users_contains".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_users_create: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/users_create".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_users_all: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/users_all".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_user_password: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/user_password".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_user_token: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/user_token".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_user_delete: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/user_delete".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_databases_get: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/databases_get".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
            method_databases_all: ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                name: "/typedb.protocol.TypeDBCluster/databases_all".to_string(),
                streaming: ::grpc::rt::GrpcStreaming::Unary,
                req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
            }),
        }
    }
}

impl TypeDBCluster for TypeDBClusterClient {
    fn servers_all(&self, o: ::grpc::RequestOptions, p: super::cluster_server::ServerManager_All_Req) -> ::grpc::SingleResponse<super::cluster_server::ServerManager_All_Res> {
        self.grpc_client.call_unary(o, p, self.method_servers_all.clone())
    }

    fn users_contains(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUserManager_Contains_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_Contains_Res> {
        self.grpc_client.call_unary(o, p, self.method_users_contains.clone())
    }

    fn users_create(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUserManager_Create_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_Create_Res> {
        self.grpc_client.call_unary(o, p, self.method_users_create.clone())
    }

    fn users_all(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUserManager_All_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_All_Res> {
        self.grpc_client.call_unary(o, p, self.method_users_all.clone())
    }

    fn user_password(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUser_Password_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Password_Res> {
        self.grpc_client.call_unary(o, p, self.method_user_password.clone())
    }

    fn user_token(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUser_Token_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Token_Res> {
        self.grpc_client.call_unary(o, p, self.method_user_token.clone())
    }

    fn user_delete(&self, o: ::grpc::RequestOptions, p: super::cluster_user::ClusterUser_Delete_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Delete_Res> {
        self.grpc_client.call_unary(o, p, self.method_user_delete.clone())
    }

    fn databases_get(&self, o: ::grpc::RequestOptions, p: super::cluster_database::ClusterDatabaseManager_Get_Req) -> ::grpc::SingleResponse<super::cluster_database::ClusterDatabaseManager_Get_Res> {
        self.grpc_client.call_unary(o, p, self.method_databases_get.clone())
    }

    fn databases_all(&self, o: ::grpc::RequestOptions, p: super::cluster_database::ClusterDatabaseManager_All_Req) -> ::grpc::SingleResponse<super::cluster_database::ClusterDatabaseManager_All_Res> {
        self.grpc_client.call_unary(o, p, self.method_databases_all.clone())
    }
}

// server

pub struct TypeDBClusterServer;


impl TypeDBClusterServer {
    pub fn new_service_def<H : TypeDBCluster + 'static + Sync + Send + 'static>(handler: H) -> ::grpc::rt::ServerServiceDefinition {
        let handler_arc = ::std::sync::Arc::new(handler);
        ::grpc::rt::ServerServiceDefinition::new("/typedb.protocol.TypeDBCluster",
            vec![
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/servers_all".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.servers_all(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/users_contains".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.users_contains(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/users_create".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.users_create(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/users_all".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.users_all(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/user_password".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.user_password(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/user_token".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.user_token(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/user_delete".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.user_delete(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/databases_get".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.databases_get(o, p))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::std::sync::Arc::new(::grpc::rt::MethodDescriptor {
                        name: "/typedb.protocol.TypeDBCluster/databases_all".to_string(),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                        resp_marshaller: Box::new(::grpc::protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |o, p| handler_copy.databases_all(o, p))
                    },
                ),
            ],
        )
    }
}
