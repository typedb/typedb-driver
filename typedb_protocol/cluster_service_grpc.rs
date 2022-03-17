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


// server interface

pub trait TypeDBCluster {
    fn servers_all(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_server::ServerManager_All_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_server::ServerManager_All_Res>) -> ::grpc::Result<()>;

    fn users_contains(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_user::ClusterUserManager_Contains_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_user::ClusterUserManager_Contains_Res>) -> ::grpc::Result<()>;

    fn users_create(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_user::ClusterUserManager_Create_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_user::ClusterUserManager_Create_Res>) -> ::grpc::Result<()>;

    fn users_all(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_user::ClusterUserManager_All_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_user::ClusterUserManager_All_Res>) -> ::grpc::Result<()>;

    fn user_password(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_user::ClusterUser_Password_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_user::ClusterUser_Password_Res>) -> ::grpc::Result<()>;

    fn user_token(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_user::ClusterUser_Token_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_user::ClusterUser_Token_Res>) -> ::grpc::Result<()>;

    fn user_delete(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_user::ClusterUser_Delete_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_user::ClusterUser_Delete_Res>) -> ::grpc::Result<()>;

    fn databases_get(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_database::ClusterDatabaseManager_Get_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_database::ClusterDatabaseManager_Get_Res>) -> ::grpc::Result<()>;

    fn databases_all(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::cluster_database::ClusterDatabaseManager_All_Req>, resp: ::grpc::ServerResponseUnarySink<super::cluster_database::ClusterDatabaseManager_All_Res>) -> ::grpc::Result<()>;
}

// client

pub struct TypeDBClusterClient {
    grpc_client: ::std::sync::Arc<::grpc::Client>,
}

impl ::grpc::ClientStub for TypeDBClusterClient {
    fn with_client(grpc_client: ::std::sync::Arc<::grpc::Client>) -> Self {
        TypeDBClusterClient {
            grpc_client: grpc_client,
        }
    }
}

impl TypeDBClusterClient {
    pub fn servers_all(&self, o: ::grpc::RequestOptions, req: super::cluster_server::ServerManager_All_Req) -> ::grpc::SingleResponse<super::cluster_server::ServerManager_All_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/servers_all"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn users_contains(&self, o: ::grpc::RequestOptions, req: super::cluster_user::ClusterUserManager_Contains_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_Contains_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/users_contains"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn users_create(&self, o: ::grpc::RequestOptions, req: super::cluster_user::ClusterUserManager_Create_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_Create_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/users_create"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn users_all(&self, o: ::grpc::RequestOptions, req: super::cluster_user::ClusterUserManager_All_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUserManager_All_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/users_all"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn user_password(&self, o: ::grpc::RequestOptions, req: super::cluster_user::ClusterUser_Password_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Password_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/user_password"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn user_token(&self, o: ::grpc::RequestOptions, req: super::cluster_user::ClusterUser_Token_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Token_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/user_token"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn user_delete(&self, o: ::grpc::RequestOptions, req: super::cluster_user::ClusterUser_Delete_Req) -> ::grpc::SingleResponse<super::cluster_user::ClusterUser_Delete_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/user_delete"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn databases_get(&self, o: ::grpc::RequestOptions, req: super::cluster_database::ClusterDatabaseManager_Get_Req) -> ::grpc::SingleResponse<super::cluster_database::ClusterDatabaseManager_Get_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/databases_get"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn databases_all(&self, o: ::grpc::RequestOptions, req: super::cluster_database::ClusterDatabaseManager_All_Req) -> ::grpc::SingleResponse<super::cluster_database::ClusterDatabaseManager_All_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/databases_all"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
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
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/servers_all"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).servers_all(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/users_contains"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).users_contains(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/users_create"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).users_create(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/users_all"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).users_all(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/user_password"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).user_password(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/user_token"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).user_token(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/user_delete"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).user_delete(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/databases_get"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).databases_get(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDBCluster/databases_all"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).databases_all(ctx, req, resp))
                    },
                ),
            ],
        )
    }
}
