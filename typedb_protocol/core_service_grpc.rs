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

pub trait TypeDB {
    fn databases_contains(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::core_database::CoreDatabaseManager_Contains_Req>, resp: ::grpc::ServerResponseUnarySink<super::core_database::CoreDatabaseManager_Contains_Res>) -> ::grpc::Result<()>;

    fn databases_create(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::core_database::CoreDatabaseManager_Create_Req>, resp: ::grpc::ServerResponseUnarySink<super::core_database::CoreDatabaseManager_Create_Res>) -> ::grpc::Result<()>;

    fn databases_all(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::core_database::CoreDatabaseManager_All_Req>, resp: ::grpc::ServerResponseUnarySink<super::core_database::CoreDatabaseManager_All_Res>) -> ::grpc::Result<()>;

    fn database_schema(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::core_database::CoreDatabase_Schema_Req>, resp: ::grpc::ServerResponseUnarySink<super::core_database::CoreDatabase_Schema_Res>) -> ::grpc::Result<()>;

    fn database_delete(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::core_database::CoreDatabase_Delete_Req>, resp: ::grpc::ServerResponseUnarySink<super::core_database::CoreDatabase_Delete_Res>) -> ::grpc::Result<()>;

    fn session_open(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::session::Session_Open_Req>, resp: ::grpc::ServerResponseUnarySink<super::session::Session_Open_Res>) -> ::grpc::Result<()>;

    fn session_close(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::session::Session_Close_Req>, resp: ::grpc::ServerResponseUnarySink<super::session::Session_Close_Res>) -> ::grpc::Result<()>;

    fn session_pulse(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequestSingle<super::session::Session_Pulse_Req>, resp: ::grpc::ServerResponseUnarySink<super::session::Session_Pulse_Res>) -> ::grpc::Result<()>;

    fn transaction(&self, o: ::grpc::ServerHandlerContext, req: ::grpc::ServerRequest<super::transaction::Transaction_Client>, resp: ::grpc::ServerResponseSink<super::transaction::Transaction_Server>) -> ::grpc::Result<()>;
}

// client

pub struct TypeDBClient {
    grpc_client: ::std::sync::Arc<::grpc::Client>,
}

impl ::grpc::ClientStub for TypeDBClient {
    fn with_client(grpc_client: ::std::sync::Arc<::grpc::Client>) -> Self {
        TypeDBClient {
            grpc_client: grpc_client,
        }
    }
}

impl TypeDBClient {
    pub fn databases_contains(&self, o: ::grpc::RequestOptions, req: super::core_database::CoreDatabaseManager_Contains_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_Contains_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/databases_contains"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn databases_create(&self, o: ::grpc::RequestOptions, req: super::core_database::CoreDatabaseManager_Create_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_Create_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/databases_create"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn databases_all(&self, o: ::grpc::RequestOptions, req: super::core_database::CoreDatabaseManager_All_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabaseManager_All_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/databases_all"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn database_schema(&self, o: ::grpc::RequestOptions, req: super::core_database::CoreDatabase_Schema_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabase_Schema_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/database_schema"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn database_delete(&self, o: ::grpc::RequestOptions, req: super::core_database::CoreDatabase_Delete_Req) -> ::grpc::SingleResponse<super::core_database::CoreDatabase_Delete_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/database_delete"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn session_open(&self, o: ::grpc::RequestOptions, req: super::session::Session_Open_Req) -> ::grpc::SingleResponse<super::session::Session_Open_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/session_open"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn session_close(&self, o: ::grpc::RequestOptions, req: super::session::Session_Close_Req) -> ::grpc::SingleResponse<super::session::Session_Close_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/session_close"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn session_pulse(&self, o: ::grpc::RequestOptions, req: super::session::Session_Pulse_Req) -> ::grpc::SingleResponse<super::session::Session_Pulse_Res> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/session_pulse"),
            streaming: ::grpc::rt::GrpcStreaming::Unary,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_unary(o, req, descriptor)
    }

    pub fn transaction(&self, o: ::grpc::RequestOptions) -> impl ::std::future::Future<Output=::grpc::Result<(::grpc::ClientRequestSink<super::transaction::Transaction_Client>, ::grpc::StreamingResponse<super::transaction::Transaction_Server>)>> {
        let descriptor = ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
            name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/transaction"),
            streaming: ::grpc::rt::GrpcStreaming::Bidi,
            req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
            resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
        });
        self.grpc_client.call_bidi(o, descriptor)
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
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/databases_contains"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).databases_contains(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/databases_create"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).databases_create(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/databases_all"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).databases_all(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/database_schema"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).database_schema(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/database_delete"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).database_delete(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/session_open"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).session_open(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/session_close"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).session_close(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/session_pulse"),
                        streaming: ::grpc::rt::GrpcStreaming::Unary,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerUnary::new(move |ctx, req, resp| (*handler_copy).session_pulse(ctx, req, resp))
                    },
                ),
                ::grpc::rt::ServerMethod::new(
                    ::grpc::rt::ArcOrStatic::Static(&::grpc::rt::MethodDescriptor {
                        name: ::grpc::rt::StringOrStatic::Static("/typedb.protocol.TypeDB/transaction"),
                        streaming: ::grpc::rt::GrpcStreaming::Bidi,
                        req_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                        resp_marshaller: ::grpc::rt::ArcOrStatic::Static(&::grpc_protobuf::MarshallerProtobuf),
                    }),
                    {
                        let handler_copy = handler_arc.clone();
                        ::grpc::rt::MethodHandlerBidi::new(move |ctx, req, resp| (*handler_copy).transaction(ctx, req, resp))
                    },
                ),
            ],
        )
    }
}
