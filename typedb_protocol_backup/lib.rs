// tonic::include_proto!("typedb.protocol");

// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ClusterDatabaseManager {
// }
// /// Nested message and enum types in `ClusterDatabaseManager`.
// pub mod cluster_database_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Get {
//     }
//     /// Nested message and enum types in `Get`.
//     pub mod get {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub database: ::core::option::Option<super::super::ClusterDatabase>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct All {
//     }
//     /// Nested message and enum types in `All`.
//     pub mod all {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, repeated, tag="1")]
//             pub databases: ::prost::alloc::vec::Vec<super::super::ClusterDatabase>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ClusterDatabase {
//     #[prost(string, tag="1")]
//     pub name: ::prost::alloc::string::String,
//     #[prost(message, repeated, tag="2")]
//     pub replicas: ::prost::alloc::vec::Vec<cluster_database::Replica>,
// }
// /// Nested message and enum types in `ClusterDatabase`.
// pub mod cluster_database {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Replica {
//         #[prost(string, tag="1")]
//         pub address: ::prost::alloc::string::String,
//         #[prost(bool, tag="2")]
//         pub primary: bool,
//         #[prost(bool, tag="3")]
//         pub preferred: bool,
//         #[prost(int64, tag="4")]
//         pub term: i64,
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ServerManager {
// }
// /// Nested message and enum types in `ServerManager`.
// pub mod server_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct All {
//     }
//     /// Nested message and enum types in `All`.
//     pub mod all {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, repeated, tag="1")]
//             pub servers: ::prost::alloc::vec::Vec<super::super::Server>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Server {
//     #[prost(string, tag="1")]
//     pub address: ::prost::alloc::string::String,
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ClusterUserManager {
// }
// /// Nested message and enum types in `ClusterUserManager`.
// pub mod cluster_user_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Contains {
//     }
//     /// Nested message and enum types in `Contains`.
//     pub mod contains {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub username: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(bool, tag="1")]
//             pub contains: bool,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Create {
//     }
//     /// Nested message and enum types in `Create`.
//     pub mod create {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub username: ::prost::alloc::string::String,
//             #[prost(string, tag="2")]
//             pub password: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct All {
//     }
//     /// Nested message and enum types in `All`.
//     pub mod all {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, repeated, tag="1")]
//             pub names: ::prost::alloc::vec::Vec<::prost::alloc::string::String>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ClusterUser {
// }
// /// Nested message and enum types in `ClusterUser`.
// pub mod cluster_user {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Password {
//     }
//     /// Nested message and enum types in `Password`.
//     pub mod password {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub username: ::prost::alloc::string::String,
//             #[prost(string, tag="2")]
//             pub password: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Token {
//     }
//     /// Nested message and enum types in `Token`.
//     pub mod token {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub username: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, tag="1")]
//             pub token: ::prost::alloc::string::String,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Delete {
//     }
//     /// Nested message and enum types in `Delete`.
//     pub mod delete {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub username: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
// }
// /// Generated client implementations.
// pub mod type_db_cluster_client {
//     #![allow(unused_variables, dead_code, missing_docs, clippy::let_unit_value)]
//     use tonic::codegen::*;
//     use tonic::codegen::http::Uri;
//     #[derive(Debug, Clone)]
//     pub struct TypeDbClusterClient<T> {
//         inner: tonic::client::Grpc<T>,
//     }
//     impl TypeDbClusterClient<tonic::transport::Channel> {
//         /// Attempt to create a new client by connecting to a given endpoint.
//         pub async fn connect<D>(dst: D) -> Result<Self, tonic::transport::Error>
//         where
//             D: std::convert::TryInto<tonic::transport::Endpoint>,
//             D::Error: Into<StdError>,
//         {
//             let conn = tonic::transport::Endpoint::new(dst)?.connect().await?;
//             Ok(Self::new(conn))
//         }
//     }
//     impl<T> TypeDbClusterClient<T>
//     where
//         T: tonic::client::GrpcService<tonic::body::BoxBody>,
//         T::Error: Into<StdError>,
//         T::ResponseBody: Body<Data = Bytes> + Send + 'static,
//         <T::ResponseBody as Body>::Error: Into<StdError> + Send,
//     {
//         pub fn new(inner: T) -> Self {
//             let inner = tonic::client::Grpc::new(inner);
//             Self { inner }
//         }
//         pub fn with_origin(inner: T, origin: Uri) -> Self {
//             let inner = tonic::client::Grpc::with_origin(inner, origin);
//             Self { inner }
//         }
//         pub fn with_interceptor<F>(
//             inner: T,
//             interceptor: F,
//         ) -> TypeDbClusterClient<InterceptedService<T, F>>
//         where
//             F: tonic::service::Interceptor,
//             T::ResponseBody: Default,
//             T: tonic::codegen::Service<
//                 http::Request<tonic::body::BoxBody>,
//                 Response = http::Response<
//                     <T as tonic::client::GrpcService<tonic::body::BoxBody>>::ResponseBody,
//                 >,
//             >,
//             <T as tonic::codegen::Service<
//                 http::Request<tonic::body::BoxBody>,
//             >>::Error: Into<StdError> + Send + Sync,
//         {
//             TypeDbClusterClient::new(InterceptedService::new(inner, interceptor))
//         }
//         /// Compress requests with the given encoding.
//         ///
//         /// This requires the server to support it otherwise it might respond with an
//         /// error.
//         #[must_use]
//         pub fn send_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.inner = self.inner.send_compressed(encoding);
//             self
//         }
//         /// Enable decompressing responses.
//         #[must_use]
//         pub fn accept_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.inner = self.inner.accept_compressed(encoding);
//             self
//         }
//         /// Server Manager API
//         pub async fn servers_all(
//             &mut self,
//             request: impl tonic::IntoRequest<super::server_manager::all::Req>,
//         ) -> Result<tonic::Response<super::server_manager::all::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/servers_all",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// User Manager API
//         pub async fn users_contains(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_user_manager::contains::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_user_manager::contains::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/users_contains",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn users_create(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_user_manager::create::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_user_manager::create::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/users_create",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn users_all(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_user_manager::all::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_user_manager::all::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/users_all",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// User API
//         pub async fn user_password(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_user::password::Req>,
//         ) -> Result<tonic::Response<super::cluster_user::password::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/user_password",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn user_token(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_user::token::Req>,
//         ) -> Result<tonic::Response<super::cluster_user::token::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/user_token",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn user_delete(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_user::delete::Req>,
//         ) -> Result<tonic::Response<super::cluster_user::delete::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/user_delete",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// Database Manager API
//         pub async fn databases_get(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_database_manager::get::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_database_manager::get::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/databases_get",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn databases_all(
//             &mut self,
//             request: impl tonic::IntoRequest<super::cluster_database_manager::all::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_database_manager::all::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDBCluster/databases_all",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//     }
// }
// /// Generated server implementations.
// pub mod type_db_cluster_server {
//     #![allow(unused_variables, dead_code, missing_docs, clippy::let_unit_value)]
//     use tonic::codegen::*;
//     ///Generated trait containing gRPC methods that should be implemented for use with TypeDbClusterServer.
//     #[async_trait]
//     pub trait TypeDbCluster: Send + Sync + 'static {
//         /// Server Manager API
//         async fn servers_all(
//             &self,
//             request: tonic::Request<super::server_manager::all::Req>,
//         ) -> Result<tonic::Response<super::server_manager::all::Res>, tonic::Status>;
//         /// User Manager API
//         async fn users_contains(
//             &self,
//             request: tonic::Request<super::cluster_user_manager::contains::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_user_manager::contains::Res>,
//             tonic::Status,
//         >;
//         async fn users_create(
//             &self,
//             request: tonic::Request<super::cluster_user_manager::create::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_user_manager::create::Res>,
//             tonic::Status,
//         >;
//         async fn users_all(
//             &self,
//             request: tonic::Request<super::cluster_user_manager::all::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_user_manager::all::Res>,
//             tonic::Status,
//         >;
//         /// User API
//         async fn user_password(
//             &self,
//             request: tonic::Request<super::cluster_user::password::Req>,
//         ) -> Result<tonic::Response<super::cluster_user::password::Res>, tonic::Status>;
//         async fn user_token(
//             &self,
//             request: tonic::Request<super::cluster_user::token::Req>,
//         ) -> Result<tonic::Response<super::cluster_user::token::Res>, tonic::Status>;
//         async fn user_delete(
//             &self,
//             request: tonic::Request<super::cluster_user::delete::Req>,
//         ) -> Result<tonic::Response<super::cluster_user::delete::Res>, tonic::Status>;
//         /// Database Manager API
//         async fn databases_get(
//             &self,
//             request: tonic::Request<super::cluster_database_manager::get::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_database_manager::get::Res>,
//             tonic::Status,
//         >;
//         async fn databases_all(
//             &self,
//             request: tonic::Request<super::cluster_database_manager::all::Req>,
//         ) -> Result<
//             tonic::Response<super::cluster_database_manager::all::Res>,
//             tonic::Status,
//         >;
//     }
//     #[derive(Debug)]
//     pub struct TypeDbClusterServer<T: TypeDbCluster> {
//         inner: _Inner<T>,
//         accept_compression_encodings: EnabledCompressionEncodings,
//         send_compression_encodings: EnabledCompressionEncodings,
//     }
//     struct _Inner<T>(Arc<T>);
//     impl<T: TypeDbCluster> TypeDbClusterServer<T> {
//         pub fn new(inner: T) -> Self {
//             Self::from_arc(Arc::new(inner))
//         }
//         pub fn from_arc(inner: Arc<T>) -> Self {
//             let inner = _Inner(inner);
//             Self {
//                 inner,
//                 accept_compression_encodings: Default::default(),
//                 send_compression_encodings: Default::default(),
//             }
//         }
//         pub fn with_interceptor<F>(
//             inner: T,
//             interceptor: F,
//         ) -> InterceptedService<Self, F>
//         where
//             F: tonic::service::Interceptor,
//         {
//             InterceptedService::new(Self::new(inner), interceptor)
//         }
//         /// Enable decompressing requests with the given encoding.
//         #[must_use]
//         pub fn accept_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.accept_compression_encodings.enable(encoding);
//             self
//         }
//         /// Compress responses with the given encoding, if the client supports it.
//         #[must_use]
//         pub fn send_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.send_compression_encodings.enable(encoding);
//             self
//         }
//     }
//     impl<T, B> tonic::codegen::Service<http::Request<B>> for TypeDbClusterServer<T>
//     where
//         T: TypeDbCluster,
//         B: Body + Send + 'static,
//         B::Error: Into<StdError> + Send + 'static,
//     {
//         type Response = http::Response<tonic::body::BoxBody>;
//         type Error = std::convert::Infallible;
//         type Future = BoxFuture<Self::Response, Self::Error>;
//         fn poll_ready(
//             &mut self,
//             _cx: &mut Context<'_>,
//         ) -> Poll<Result<(), Self::Error>> {
//             Poll::Ready(Ok(()))
//         }
//         fn call(&mut self, req: http::Request<B>) -> Self::Future {
//             let inner = self.inner.clone();
//             match req.uri().path() {
//                 "/typedb.protocol.TypeDBCluster/servers_all" => {
//                     #[allow(non_camel_case_types)]
//                     struct servers_allSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<super::server_manager::all::Req>
//                     for servers_allSvc<T> {
//                         type Response = super::server_manager::all::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::server_manager::all::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move { (*inner).servers_all(request).await };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = servers_allSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/users_contains" => {
//                     #[allow(non_camel_case_types)]
//                     struct users_containsSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<
//                         super::cluster_user_manager::contains::Req,
//                     > for users_containsSvc<T> {
//                         type Response = super::cluster_user_manager::contains::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::cluster_user_manager::contains::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).users_contains(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = users_containsSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/users_create" => {
//                     #[allow(non_camel_case_types)]
//                     struct users_createSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<
//                         super::cluster_user_manager::create::Req,
//                     > for users_createSvc<T> {
//                         type Response = super::cluster_user_manager::create::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::cluster_user_manager::create::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).users_create(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = users_createSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/users_all" => {
//                     #[allow(non_camel_case_types)]
//                     struct users_allSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<super::cluster_user_manager::all::Req>
//                     for users_allSvc<T> {
//                         type Response = super::cluster_user_manager::all::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::cluster_user_manager::all::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move { (*inner).users_all(request).await };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = users_allSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/user_password" => {
//                     #[allow(non_camel_case_types)]
//                     struct user_passwordSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<super::cluster_user::password::Req>
//                     for user_passwordSvc<T> {
//                         type Response = super::cluster_user::password::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::cluster_user::password::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).user_password(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = user_passwordSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/user_token" => {
//                     #[allow(non_camel_case_types)]
//                     struct user_tokenSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<super::cluster_user::token::Req>
//                     for user_tokenSvc<T> {
//                         type Response = super::cluster_user::token::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::cluster_user::token::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move { (*inner).user_token(request).await };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = user_tokenSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/user_delete" => {
//                     #[allow(non_camel_case_types)]
//                     struct user_deleteSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<super::cluster_user::delete::Req>
//                     for user_deleteSvc<T> {
//                         type Response = super::cluster_user::delete::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::cluster_user::delete::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move { (*inner).user_delete(request).await };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = user_deleteSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/databases_get" => {
//                     #[allow(non_camel_case_types)]
//                     struct databases_getSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<
//                         super::cluster_database_manager::get::Req,
//                     > for databases_getSvc<T> {
//                         type Response = super::cluster_database_manager::get::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::cluster_database_manager::get::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).databases_get(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = databases_getSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDBCluster/databases_all" => {
//                     #[allow(non_camel_case_types)]
//                     struct databases_allSvc<T: TypeDbCluster>(pub Arc<T>);
//                     impl<
//                         T: TypeDbCluster,
//                     > tonic::server::UnaryService<
//                         super::cluster_database_manager::all::Req,
//                     > for databases_allSvc<T> {
//                         type Response = super::cluster_database_manager::all::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::cluster_database_manager::all::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).databases_all(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = databases_allSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 _ => {
//                     Box::pin(async move {
//                         Ok(
//                             http::Response::builder()
//                                 .status(200)
//                                 .header("grpc-status", "12")
//                                 .header("content-type", "application/grpc")
//                                 .body(empty_body())
//                                 .unwrap(),
//                         )
//                     })
//                 }
//             }
//         }
//     }
//     impl<T: TypeDbCluster> Clone for TypeDbClusterServer<T> {
//         fn clone(&self) -> Self {
//             let inner = self.inner.clone();
//             Self {
//                 inner,
//                 accept_compression_encodings: self.accept_compression_encodings,
//                 send_compression_encodings: self.send_compression_encodings,
//             }
//         }
//     }
//     impl<T: TypeDbCluster> Clone for _Inner<T> {
//         fn clone(&self) -> Self {
//             Self(self.0.clone())
//         }
//     }
//     impl<T: std::fmt::Debug> std::fmt::Debug for _Inner<T> {
//         fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
//             write!(f, "{:?}", self.0)
//         }
//     }
//     impl<T: TypeDbCluster> tonic::server::NamedService for TypeDbClusterServer<T> {
//         const NAME: &'static str = "typedb.protocol.TypeDBCluster";
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct CoreDatabaseManager {
// }
// /// Nested message and enum types in `CoreDatabaseManager`.
// pub mod core_database_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Contains {
//     }
//     /// Nested message and enum types in `Contains`.
//     pub mod contains {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(bool, tag="1")]
//             pub contains: bool,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Create {
//     }
//     /// Nested message and enum types in `Create`.
//     pub mod create {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct All {
//     }
//     /// Nested message and enum types in `All`.
//     pub mod all {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, repeated, tag="1")]
//             pub names: ::prost::alloc::vec::Vec<::prost::alloc::string::String>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct CoreDatabase {
// }
// /// Nested message and enum types in `CoreDatabase`.
// pub mod core_database {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Schema {
//     }
//     /// Nested message and enum types in `Schema`.
//     pub mod schema {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, tag="1")]
//             pub schema: ::prost::alloc::string::String,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct TypeSchema {
//     }
//     /// Nested message and enum types in `TypeSchema`.
//     pub mod type_schema {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, tag="1")]
//             pub schema: ::prost::alloc::string::String,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct RuleSchema {
//     }
//     /// Nested message and enum types in `RuleSchema`.
//     pub mod rule_schema {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, tag="1")]
//             pub schema: ::prost::alloc::string::String,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Delete {
//     }
//     /// Nested message and enum types in `Delete`.
//     pub mod delete {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub name: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
// }
// // TODO: Replace 'oneof' with 'optional' when upgraded to Protobuf 3.13 everywhere
// // <https://github.com/protocolbuffers/protobuf/issues/1606#issuecomment-618687169>
//
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Options {
//     #[prost(oneof="options::InferOpt", tags="1")]
//     pub infer_opt: ::core::option::Option<options::InferOpt>,
//     #[prost(oneof="options::TraceInferenceOpt", tags="2")]
//     pub trace_inference_opt: ::core::option::Option<options::TraceInferenceOpt>,
//     #[prost(oneof="options::ExplainOpt", tags="3")]
//     pub explain_opt: ::core::option::Option<options::ExplainOpt>,
//     #[prost(oneof="options::ParallelOpt", tags="4")]
//     pub parallel_opt: ::core::option::Option<options::ParallelOpt>,
//     #[prost(oneof="options::PrefetchSizeOpt", tags="5")]
//     pub prefetch_size_opt: ::core::option::Option<options::PrefetchSizeOpt>,
//     #[prost(oneof="options::PrefetchOpt", tags="6")]
//     pub prefetch_opt: ::core::option::Option<options::PrefetchOpt>,
//     #[prost(oneof="options::SessionIdleTimeoutOpt", tags="7")]
//     pub session_idle_timeout_opt: ::core::option::Option<options::SessionIdleTimeoutOpt>,
//     #[prost(oneof="options::TransactionTimeoutOpt", tags="8")]
//     pub transaction_timeout_opt: ::core::option::Option<options::TransactionTimeoutOpt>,
//     #[prost(oneof="options::SchemaLockAcquireTimeoutOpt", tags="9")]
//     pub schema_lock_acquire_timeout_opt: ::core::option::Option<options::SchemaLockAcquireTimeoutOpt>,
//     #[prost(oneof="options::ReadAnyReplicaOpt", tags="10")]
//     pub read_any_replica_opt: ::core::option::Option<options::ReadAnyReplicaOpt>,
// }
// /// Nested message and enum types in `Options`.
// pub mod options {
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum InferOpt {
//         #[prost(bool, tag="1")]
//         Infer(bool),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum TraceInferenceOpt {
//         #[prost(bool, tag="2")]
//         TraceInference(bool),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum ExplainOpt {
//         #[prost(bool, tag="3")]
//         Explain(bool),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum ParallelOpt {
//         #[prost(bool, tag="4")]
//         Parallel(bool),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum PrefetchSizeOpt {
//         #[prost(int32, tag="5")]
//         PrefetchSize(i32),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum PrefetchOpt {
//         #[prost(bool, tag="6")]
//         Prefetch(bool),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum SessionIdleTimeoutOpt {
//         #[prost(int32, tag="7")]
//         SessionIdleTimeoutMillis(i32),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum TransactionTimeoutOpt {
//         #[prost(int32, tag="8")]
//         TransactionTimeoutMillis(i32),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum SchemaLockAcquireTimeoutOpt {
//         #[prost(int32, tag="9")]
//         SchemaLockAcquireTimeoutMillis(i32),
//     }
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum ReadAnyReplicaOpt {
//         #[prost(bool, tag="10")]
//         ReadAnyReplica(bool),
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Session {
// }
// /// Nested message and enum types in `Session`.
// pub mod session {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Open {
//     }
//     /// Nested message and enum types in `Open`.
//     pub mod open {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub database: ::prost::alloc::string::String,
//             #[prost(enumeration="super::Type", tag="2")]
//             pub r#type: i32,
//             #[prost(message, optional, tag="3")]
//             pub options: ::core::option::Option<super::super::Options>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(bytes="vec", tag="1")]
//             pub session_id: ::prost::alloc::vec::Vec<u8>,
//             #[prost(int32, tag="2")]
//             pub server_duration_millis: i32,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Close {
//     }
//     /// Nested message and enum types in `Close`.
//     pub mod close {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bytes="vec", tag="1")]
//             pub session_id: ::prost::alloc::vec::Vec<u8>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Pulse {
//     }
//     /// Nested message and enum types in `Pulse`.
//     pub mod pulse {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bytes="vec", tag="1")]
//             pub session_id: ::prost::alloc::vec::Vec<u8>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(bool, tag="1")]
//             pub alive: bool,
//         }
//     }
//     #[derive(Clone, Copy, Debug, PartialEq, Eq, Hash, PartialOrd, Ord, ::prost::Enumeration)]
//     #[repr(i32)]
//     pub enum Type {
//         Data = 0,
//         Schema = 1,
//     }
//     impl Type {
//         /// String value of the enum field names used in the ProtoBuf definition.
//         ///
//         /// The values are not transformed in any way and thus are considered stable
//         /// (if the ProtoBuf definition does not change) and safe for programmatic use.
//         pub fn as_str_name(&self) -> &'static str {
//             match self {
//                 Type::Data => "DATA",
//                 Type::Schema => "SCHEMA",
//             }
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ConceptManager {
// }
// /// Nested message and enum types in `ConceptManager`.
// pub mod concept_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(oneof="req::Req", tags="1, 2, 3, 4, 5")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             #[prost(message, tag="1")]
//             GetThingTypeReq(super::get_thing_type::Req),
//             #[prost(message, tag="2")]
//             GetThingReq(super::get_thing::Req),
//             #[prost(message, tag="3")]
//             PutEntityTypeReq(super::put_entity_type::Req),
//             #[prost(message, tag="4")]
//             PutAttributeTypeReq(super::put_attribute_type::Req),
//             #[prost(message, tag="5")]
//             PutRelationTypeReq(super::put_relation_type::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(oneof="res::Res", tags="1, 2, 3, 4, 5")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="1")]
//             GetThingTypeRes(super::get_thing_type::Res),
//             #[prost(message, tag="2")]
//             GetThingRes(super::get_thing::Res),
//             #[prost(message, tag="3")]
//             PutEntityTypeRes(super::put_entity_type::Res),
//             #[prost(message, tag="4")]
//             PutAttributeTypeRes(super::put_attribute_type::Res),
//             #[prost(message, tag="5")]
//             PutRelationTypeRes(super::put_relation_type::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetThingType {
//     }
//     /// Nested message and enum types in `GetThingType`.
//     pub mod get_thing_type {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 ThingType(super::super::super::Type),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetThing {
//     }
//     /// Nested message and enum types in `GetThing`.
//     pub mod get_thing {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bytes="vec", tag="1")]
//             pub iid: ::prost::alloc::vec::Vec<u8>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 Thing(super::super::super::Thing),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct PutEntityType {
//     }
//     /// Nested message and enum types in `PutEntityType`.
//     pub mod put_entity_type {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub entity_type: ::core::option::Option<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct PutAttributeType {
//     }
//     /// Nested message and enum types in `PutAttributeType`.
//     pub mod put_attribute_type {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//             #[prost(enumeration="super::super::attribute_type::ValueType", tag="2")]
//             pub value_type: i32,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub attribute_type: ::core::option::Option<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct PutRelationType {
//     }
//     /// Nested message and enum types in `PutRelationType`.
//     pub mod put_relation_type {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub relation_type: ::core::option::Option<super::super::Type>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Concept {
//     #[prost(oneof="concept::Concept", tags="1, 2")]
//     pub concept: ::core::option::Option<concept::Concept>,
// }
// /// Nested message and enum types in `Concept`.
// pub mod concept {
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum Concept {
//         #[prost(message, tag="1")]
//         Thing(super::Thing),
//         #[prost(message, tag="2")]
//         Type(super::Type),
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Thing {
//     #[prost(bytes="vec", tag="1")]
//     pub iid: ::prost::alloc::vec::Vec<u8>,
//     #[prost(message, optional, tag="2")]
//     pub r#type: ::core::option::Option<Type>,
//     #[prost(message, optional, tag="3")]
//     pub value: ::core::option::Option<attribute::Value>,
//     #[prost(bool, tag="4")]
//     pub inferred: bool,
// }
// /// Nested message and enum types in `Thing`.
// pub mod thing {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(bytes="vec", tag="1")]
//         pub iid: ::prost::alloc::vec::Vec<u8>,
//         #[prost(oneof="req::Req", tags="100, 101, 102, 103, 104, 105, 106, 200, 201, 202, 203, 204, 300")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             /// Thing method requests
//             #[prost(message, tag="100")]
//             ThingDeleteReq(super::delete::Req),
//             #[prost(message, tag="101")]
//             ThingGetTypeReq(super::get_type::Req),
//             #[prost(message, tag="102")]
//             ThingGetHasReq(super::get_has::Req),
//             #[prost(message, tag="103")]
//             ThingSetHasReq(super::set_has::Req),
//             #[prost(message, tag="104")]
//             ThingUnsetHasReq(super::unset_has::Req),
//             #[prost(message, tag="105")]
//             ThingGetRelationsReq(super::get_relations::Req),
//             #[prost(message, tag="106")]
//             ThingGetPlayingReq(super::get_playing::Req),
//             /// Relation method requests
//             #[prost(message, tag="200")]
//             RelationAddPlayerReq(super::super::relation::add_player::Req),
//             #[prost(message, tag="201")]
//             RelationRemovePlayerReq(super::super::relation::remove_player::Req),
//             #[prost(message, tag="202")]
//             RelationGetPlayersReq(super::super::relation::get_players::Req),
//             #[prost(message, tag="203")]
//             RelationGetPlayersByRoleTypeReq(super::super::relation::get_players_by_role_type::Req),
//             #[prost(message, tag="204")]
//             RelationGetRelatingReq(super::super::relation::get_relating::Req),
//             /// Attribute method requests
//             #[prost(message, tag="300")]
//             AttributeGetOwnersReq(super::super::attribute::get_owners::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(oneof="res::Res", tags="100, 101, 102, 103, 200, 201")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             /// Thing method responses
//             #[prost(message, tag="100")]
//             ThingDeleteRes(super::delete::Res),
//             #[prost(message, tag="101")]
//             ThingGetTypeRes(super::get_type::Res),
//             #[prost(message, tag="102")]
//             ThingSetHasRes(super::set_has::Res),
//             #[prost(message, tag="103")]
//             ThingUnsetHasRes(super::unset_has::Res),
//             /// Relation method responses
//             #[prost(message, tag="200")]
//             RelationAddPlayerRes(super::super::relation::add_player::Res),
//             #[prost(message, tag="201")]
//             RelationRemovePlayerRes(super::super::relation::remove_player::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct ResPart {
//         #[prost(oneof="res_part::Res", tags="100, 101, 102, 200, 201, 202, 300")]
//         pub res: ::core::option::Option<res_part::Res>,
//     }
//     /// Nested message and enum types in `ResPart`.
//     pub mod res_part {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             /// Thing method responses
//             #[prost(message, tag="100")]
//             ThingGetHasResPart(super::get_has::ResPart),
//             #[prost(message, tag="101")]
//             ThingGetRelationsResPart(super::get_relations::ResPart),
//             #[prost(message, tag="102")]
//             ThingGetPlayingResPart(super::get_playing::ResPart),
//             /// Relation method responses
//             #[prost(message, tag="200")]
//             RelationGetPlayersResPart(super::super::relation::get_players::ResPart),
//             #[prost(message, tag="201")]
//             RelationGetPlayersByRoleTypeResPart(super::super::relation::get_players_by_role_type::ResPart),
//             #[prost(message, tag="202")]
//             RelationGetRelatingResPart(super::super::relation::get_relating::ResPart),
//             /// Attribute method responses
//             #[prost(message, tag="300")]
//             AttributeGetOwnersResPart(super::super::attribute::get_owners::ResPart),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Delete {
//     }
//     /// Nested message and enum types in `Delete`.
//     pub mod delete {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetType {
//     }
//     /// Nested message and enum types in `GetType`.
//     pub mod get_type {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub thing_type: ::core::option::Option<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetHas {
//     }
//     /// Nested message and enum types in `SetHas`.
//     pub mod set_has {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub attribute: ::core::option::Option<super::super::Thing>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct UnsetHas {
//     }
//     /// Nested message and enum types in `UnsetHas`.
//     pub mod unset_has {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub attribute: ::core::option::Option<super::super::Thing>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetHas {
//     }
//     /// Nested message and enum types in `GetHas`.
//     pub mod get_has {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             /// Only one filter can be set at a time (attribute_types or keys_only). repeated can't be used in a oneof
//             #[prost(message, repeated, tag="1")]
//             pub attribute_types: ::prost::alloc::vec::Vec<super::super::Type>,
//             #[prost(bool, tag="2")]
//             pub keys_only: bool,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub attributes: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlaying {
//     }
//     /// Nested message and enum types in `GetPlaying`.
//     pub mod get_playing {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelations {
//     }
//     /// Nested message and enum types in `GetRelations`.
//     pub mod get_relations {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub relations: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Relation {
// }
// /// Nested message and enum types in `Relation`.
// pub mod relation {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct AddPlayer {
//     }
//     /// Nested message and enum types in `AddPlayer`.
//     pub mod add_player {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub role_type: ::core::option::Option<super::super::Type>,
//             #[prost(message, optional, tag="2")]
//             pub player: ::core::option::Option<super::super::Thing>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct RemovePlayer {
//     }
//     /// Nested message and enum types in `RemovePlayer`.
//     pub mod remove_player {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub role_type: ::core::option::Option<super::super::Type>,
//             #[prost(message, optional, tag="2")]
//             pub player: ::core::option::Option<super::super::Thing>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlayers {
//     }
//     /// Nested message and enum types in `GetPlayers`.
//     pub mod get_players {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub things: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlayersByRoleType {
//     }
//     /// Nested message and enum types in `GetPlayersByRoleType`.
//     pub mod get_players_by_role_type {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct RoleTypeWithPlayer {
//             #[prost(message, optional, tag="1")]
//             pub role_type: ::core::option::Option<super::super::Type>,
//             #[prost(message, optional, tag="2")]
//             pub player: ::core::option::Option<super::super::Thing>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types_with_players: ::prost::alloc::vec::Vec<RoleTypeWithPlayer>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelating {
//     }
//     /// Nested message and enum types in `GetRelating`.
//     pub mod get_relating {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Attribute {
// }
// /// Nested message and enum types in `Attribute`.
// pub mod attribute {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Value {
//         #[prost(oneof="value::Value", tags="1, 2, 3, 4, 5")]
//         pub value: ::core::option::Option<value::Value>,
//     }
//     /// Nested message and enum types in `Value`.
//     pub mod value {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Value {
//             #[prost(string, tag="1")]
//             String(::prost::alloc::string::String),
//             #[prost(bool, tag="2")]
//             Boolean(bool),
//             #[prost(int64, tag="3")]
//             Long(i64),
//             #[prost(double, tag="4")]
//             Double(f64),
//             /// time since epoch in milliseconds
//             #[prost(int64, tag="5")]
//             DateTime(i64),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetOwners {
//     }
//     /// Nested message and enum types in `GetOwners`.
//     pub mod get_owners {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(oneof="req::Filter", tags="1")]
//             pub filter: ::core::option::Option<req::Filter>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Filter {
//                 #[prost(message, tag="1")]
//                 ThingType(super::super::super::Type),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub things: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Type {
//     #[prost(string, tag="1")]
//     pub label: ::prost::alloc::string::String,
//     #[prost(string, tag="2")]
//     pub scope: ::prost::alloc::string::String,
//     #[prost(enumeration="r#type::Encoding", tag="3")]
//     pub encoding: i32,
//     #[prost(enumeration="attribute_type::ValueType", tag="4")]
//     pub value_type: i32,
//     #[prost(bool, tag="5")]
//     pub root: bool,
// }
// /// Nested message and enum types in `Type`.
// pub mod r#type {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(string, tag="1")]
//         pub label: ::prost::alloc::string::String,
//         #[prost(string, tag="2")]
//         pub scope: ::prost::alloc::string::String,
//         #[prost(oneof="req::Req", tags="100, 101, 102, 103, 104, 105, 106, 107, 200, 201, 202, 203, 204, 205, 300, 309, 301, 302, 303, 310, 311, 304, 305, 306, 312, 313, 307, 308, 314, 400, 500, 502, 505, 501, 506, 503, 504, 600, 601, 602, 603, 604, 605")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             /// Type method requests
//             #[prost(message, tag="100")]
//             TypeDeleteReq(super::delete::Req),
//             #[prost(message, tag="101")]
//             TypeSetLabelReq(super::set_label::Req),
//             #[prost(message, tag="102")]
//             TypeIsAbstractReq(super::is_abstract::Req),
//             #[prost(message, tag="103")]
//             TypeGetSupertypeReq(super::get_supertype::Req),
//             #[prost(message, tag="104")]
//             TypeSetSupertypeReq(super::set_supertype::Req),
//             #[prost(message, tag="105")]
//             TypeGetSupertypesReq(super::get_supertypes::Req),
//             #[prost(message, tag="106")]
//             TypeGetSubtypesReq(super::get_subtypes::Req),
//             #[prost(message, tag="107")]
//             TypeGetSubtypesExplicitReq(super::get_subtypes_explicit::Req),
//             /// RoleType method requests
//             #[prost(message, tag="200")]
//             RoleTypeGetRelationTypesReq(super::super::role_type::get_relation_types::Req),
//             #[prost(message, tag="201")]
//             RoleTypeGetPlayerTypesReq(super::super::role_type::get_player_types::Req),
//             #[prost(message, tag="202")]
//             RoleTypeGetRelationInstancesReq(super::super::role_type::get_relation_instances::Req),
//             #[prost(message, tag="203")]
//             RoleTypeGetRelationInstancesExplicitReq(super::super::role_type::get_relation_instances_explicit::Req),
//             #[prost(message, tag="204")]
//             RoleTypeGetPlayerInstancesReq(super::super::role_type::get_player_instances::Req),
//             #[prost(message, tag="205")]
//             RoleTypeGetPlayerInstancesExplicitReq(super::super::role_type::get_player_instances_explicit::Req),
//             /// ThingType method requests
//             #[prost(message, tag="300")]
//             ThingTypeGetInstancesReq(super::super::thing_type::get_instances::Req),
//             /// TODO: reorder
//             #[prost(message, tag="309")]
//             ThingTypeGetInstancesExplicitReq(super::super::thing_type::get_instances_explicit::Req),
//             #[prost(message, tag="301")]
//             ThingTypeSetAbstractReq(super::super::thing_type::set_abstract::Req),
//             #[prost(message, tag="302")]
//             ThingTypeUnsetAbstractReq(super::super::thing_type::unset_abstract::Req),
//             #[prost(message, tag="303")]
//             ThingTypeGetOwnsReq(super::super::thing_type::get_owns::Req),
//             /// TODO: reorder
//             #[prost(message, tag="310")]
//             ThingTypeGetOwnsExplicitReq(super::super::thing_type::get_owns_explicit::Req),
//             /// TODO: reorder
//             #[prost(message, tag="311")]
//             ThingTypeGetOwnsOverriddenReq(super::super::thing_type::get_owns_overridden::Req),
//             #[prost(message, tag="304")]
//             ThingTypeSetOwnsReq(super::super::thing_type::set_owns::Req),
//             #[prost(message, tag="305")]
//             ThingTypeUnsetOwnsReq(super::super::thing_type::unset_owns::Req),
//             #[prost(message, tag="306")]
//             ThingTypeGetPlaysReq(super::super::thing_type::get_plays::Req),
//             /// TODO: reorder
//             #[prost(message, tag="312")]
//             ThingTypeGetPlaysExplicitReq(super::super::thing_type::get_plays_explicit::Req),
//             /// TODO: reorder
//             #[prost(message, tag="313")]
//             ThingTypeGetPlaysOverriddenReq(super::super::thing_type::get_plays_overridden::Req),
//             #[prost(message, tag="307")]
//             ThingTypeSetPlaysReq(super::super::thing_type::set_plays::Req),
//             #[prost(message, tag="308")]
//             ThingTypeUnsetPlaysReq(super::super::thing_type::unset_plays::Req),
//             /// TODO: reorder
//             #[prost(message, tag="314")]
//             ThingTypeGetSyntaxReq(super::super::thing_type::get_syntax::Req),
//             /// EntityType method requests
//             #[prost(message, tag="400")]
//             EntityTypeCreateReq(super::super::entity_type::create::Req),
//             /// RelationType method requests
//             #[prost(message, tag="500")]
//             RelationTypeCreateReq(super::super::relation_type::create::Req),
//             /// TODO: reorder
//             #[prost(message, tag="502")]
//             RelationTypeGetRelatesReq(super::super::relation_type::get_relates::Req),
//             /// TODO: reorder
//             #[prost(message, tag="505")]
//             RelationTypeGetRelatesExplicitReq(super::super::relation_type::get_relates_explicit::Req),
//             /// TODO: reorder
//             #[prost(message, tag="501")]
//             RelationTypeGetRelatesForRoleLabelReq(super::super::relation_type::get_relates_for_role_label::Req),
//             /// TODO: reorder
//             #[prost(message, tag="506")]
//             RelationTypeGetRelatesOverriddenReq(super::super::relation_type::get_relates_overridden::Req),
//             #[prost(message, tag="503")]
//             RelationTypeSetRelatesReq(super::super::relation_type::set_relates::Req),
//             #[prost(message, tag="504")]
//             RelationTypeUnsetRelatesReq(super::super::relation_type::unset_relates::Req),
//             /// AttributeType method requests
//             #[prost(message, tag="600")]
//             AttributeTypePutReq(super::super::attribute_type::put::Req),
//             #[prost(message, tag="601")]
//             AttributeTypeGetReq(super::super::attribute_type::get::Req),
//             #[prost(message, tag="602")]
//             AttributeTypeGetRegexReq(super::super::attribute_type::get_regex::Req),
//             #[prost(message, tag="603")]
//             AttributeTypeSetRegexReq(super::super::attribute_type::set_regex::Req),
//             #[prost(message, tag="604")]
//             AttributeTypeGetOwnersReq(super::super::attribute_type::get_owners::Req),
//             #[prost(message, tag="605")]
//             AttributeTypeGetOwnersExplicitReq(super::super::attribute_type::get_owners_explicit::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(oneof="res::Res", tags="100, 101, 102, 103, 104, 300, 301, 306, 302, 303, 307, 304, 305, 308, 400, 500, 501, 504, 502, 503, 600, 601, 602, 603")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             /// Type method responses
//             #[prost(message, tag="100")]
//             TypeDeleteRes(super::delete::Res),
//             #[prost(message, tag="101")]
//             TypeSetLabelRes(super::set_label::Res),
//             #[prost(message, tag="102")]
//             TypeIsAbstractRes(super::is_abstract::Res),
//             #[prost(message, tag="103")]
//             TypeGetSupertypeRes(super::get_supertype::Res),
//             #[prost(message, tag="104")]
//             TypeSetSupertypeRes(super::set_supertype::Res),
//             /// ThingType method responses
//             #[prost(message, tag="300")]
//             ThingTypeSetAbstractRes(super::super::thing_type::set_abstract::Res),
//             #[prost(message, tag="301")]
//             ThingTypeUnsetAbstractRes(super::super::thing_type::unset_abstract::Res),
//             /// TODO: reorder
//             #[prost(message, tag="306")]
//             ThingTypeGetOwnsOverriddenRes(super::super::thing_type::get_owns_overridden::Res),
//             #[prost(message, tag="302")]
//             ThingTypeSetOwnsRes(super::super::thing_type::set_owns::Res),
//             #[prost(message, tag="303")]
//             ThingTypeUnsetOwnsRes(super::super::thing_type::unset_owns::Res),
//             /// TODO: reorder
//             #[prost(message, tag="307")]
//             ThingTypeGetPlaysOverriddenRes(super::super::thing_type::get_plays_overridden::Res),
//             #[prost(message, tag="304")]
//             ThingTypeSetPlaysRes(super::super::thing_type::set_plays::Res),
//             #[prost(message, tag="305")]
//             ThingTypeUnsetPlaysRes(super::super::thing_type::unset_plays::Res),
//             /// TODO: reorder
//             #[prost(message, tag="308")]
//             ThingTypeGetSyntaxRes(super::super::thing_type::get_syntax::Res),
//             /// EntityType method responses
//             #[prost(message, tag="400")]
//             EntityTypeCreateRes(super::super::entity_type::create::Res),
//             /// RelationType method responses
//             #[prost(message, tag="500")]
//             RelationTypeCreateRes(super::super::relation_type::create::Res),
//             #[prost(message, tag="501")]
//             RelationTypeGetRelatesForRoleLabelRes(super::super::relation_type::get_relates_for_role_label::Res),
//             /// TODO: reorder
//             #[prost(message, tag="504")]
//             RelationTypeGetRelatesOverriddenRes(super::super::relation_type::get_relates_overridden::Res),
//             #[prost(message, tag="502")]
//             RelationTypeSetRelatesRes(super::super::relation_type::set_relates::Res),
//             #[prost(message, tag="503")]
//             RelationTypeUnsetRelatesRes(super::super::relation_type::unset_relates::Res),
//             /// AttributeType method responses
//             #[prost(message, tag="600")]
//             AttributeTypePutRes(super::super::attribute_type::put::Res),
//             #[prost(message, tag="601")]
//             AttributeTypeGetRes(super::super::attribute_type::get::Res),
//             #[prost(message, tag="602")]
//             AttributeTypeGetRegexRes(super::super::attribute_type::get_regex::Res),
//             #[prost(message, tag="603")]
//             AttributeTypeSetRegexRes(super::super::attribute_type::set_regex::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct ResPart {
//         #[prost(oneof="res_part::Res", tags="100, 101, 102, 200, 201, 202, 203, 204, 205, 300, 303, 301, 304, 302, 305, 500, 501, 600, 601")]
//         pub res: ::core::option::Option<res_part::Res>,
//     }
//     /// Nested message and enum types in `ResPart`.
//     pub mod res_part {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             /// Type method responses
//             #[prost(message, tag="100")]
//             TypeGetSupertypesResPart(super::get_supertypes::ResPart),
//             #[prost(message, tag="101")]
//             TypeGetSubtypesResPart(super::get_subtypes::ResPart),
//             #[prost(message, tag="102")]
//             TypeGetSubtypesExplicitResPart(super::get_subtypes_explicit::ResPart),
//             /// RoleType method responses
//             #[prost(message, tag="200")]
//             RoleTypeGetRelationTypesResPart(super::super::role_type::get_relation_types::ResPart),
//             #[prost(message, tag="201")]
//             RoleTypeGetPlayerTypesResPart(super::super::role_type::get_player_types::ResPart),
//             #[prost(message, tag="202")]
//             RoleTypeGetRelationInstancesResPart(super::super::role_type::get_relation_instances::ResPart),
//             #[prost(message, tag="203")]
//             RoleTypeGetRelationInstancesExplicitResPart(super::super::role_type::get_relation_instances_explicit::ResPart),
//             #[prost(message, tag="204")]
//             RoleTypeGetPlayerInstancesResPart(super::super::role_type::get_player_instances::ResPart),
//             #[prost(message, tag="205")]
//             RoleTypeGetPlayerInstancesExplicitResPart(super::super::role_type::get_player_instances_explicit::ResPart),
//             /// ThingType method responses
//             #[prost(message, tag="300")]
//             ThingTypeGetInstancesResPart(super::super::thing_type::get_instances::ResPart),
//             /// TODO: reorder
//             #[prost(message, tag="303")]
//             ThingTypeGetInstancesExplicitResPart(super::super::thing_type::get_instances_explicit::ResPart),
//             #[prost(message, tag="301")]
//             ThingTypeGetOwnsResPart(super::super::thing_type::get_owns::ResPart),
//             /// TODO: reorder
//             #[prost(message, tag="304")]
//             ThingTypeGetOwnsExplicitResPart(super::super::thing_type::get_owns_explicit::ResPart),
//             #[prost(message, tag="302")]
//             ThingTypeGetPlaysResPart(super::super::thing_type::get_plays::ResPart),
//             /// TODO: reorder
//             #[prost(message, tag="305")]
//             ThingTypeGetPlaysExplicitResPart(super::super::thing_type::get_plays_explicit::ResPart),
//             /// RelationType method responses
//             #[prost(message, tag="500")]
//             RelationTypeGetRelatesResPart(super::super::relation_type::get_relates::ResPart),
//             #[prost(message, tag="501")]
//             RelationTypeGetRelatesExplicitResPart(super::super::relation_type::get_relates_explicit::ResPart),
//             /// AttributeType method responses
//             #[prost(message, tag="600")]
//             AttributeTypeGetOwnersResPart(super::super::attribute_type::get_owners::ResPart),
//             #[prost(message, tag="601")]
//             AttributeTypeGetOwnersExplicitResPart(super::super::attribute_type::get_owners_explicit::ResPart),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Delete {
//     }
//     /// Nested message and enum types in `Delete`.
//     pub mod delete {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetLabel {
//     }
//     /// Nested message and enum types in `SetLabel`.
//     pub mod set_label {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct IsAbstract {
//     }
//     /// Nested message and enum types in `IsAbstract`.
//     pub mod is_abstract {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(bool, tag="1")]
//             pub r#abstract: bool,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetSupertype {
//     }
//     /// Nested message and enum types in `GetSupertype`.
//     pub mod get_supertype {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 Type(super::super::super::Type),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetSupertype {
//     }
//     /// Nested message and enum types in `SetSupertype`.
//     pub mod set_supertype {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub r#type: ::core::option::Option<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetSupertypes {
//     }
//     /// Nested message and enum types in `GetSupertypes`.
//     pub mod get_supertypes {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetSubtypes {
//     }
//     /// Nested message and enum types in `GetSubtypes`.
//     pub mod get_subtypes {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetSubtypesExplicit {
//     }
//     /// Nested message and enum types in `GetSubtypesExplicit`.
//     pub mod get_subtypes_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, Copy, Debug, PartialEq, Eq, Hash, PartialOrd, Ord, ::prost::Enumeration)]
//     #[repr(i32)]
//     pub enum Encoding {
//         ThingType = 0,
//         EntityType = 1,
//         RelationType = 2,
//         AttributeType = 3,
//         RoleType = 4,
//     }
//     impl Encoding {
//         /// String value of the enum field names used in the ProtoBuf definition.
//         ///
//         /// The values are not transformed in any way and thus are considered stable
//         /// (if the ProtoBuf definition does not change) and safe for programmatic use.
//         pub fn as_str_name(&self) -> &'static str {
//             match self {
//                 Encoding::ThingType => "THING_TYPE",
//                 Encoding::EntityType => "ENTITY_TYPE",
//                 Encoding::RelationType => "RELATION_TYPE",
//                 Encoding::AttributeType => "ATTRIBUTE_TYPE",
//                 Encoding::RoleType => "ROLE_TYPE",
//             }
//         }
//     }
// }
// // RoleType methods
//
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct RoleType {
// }
// /// Nested message and enum types in `RoleType`.
// pub mod role_type {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelationTypes {
//     }
//     /// Nested message and enum types in `GetRelationTypes`.
//     pub mod get_relation_types {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub relation_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlayerTypes {
//     }
//     /// Nested message and enum types in `GetPlayerTypes`.
//     pub mod get_player_types {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub thing_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelationInstances {
//     }
//     /// Nested message and enum types in `GetRelationInstances`.
//     pub mod get_relation_instances {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub relations: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelationInstancesExplicit {
//     }
//     /// Nested message and enum types in `GetRelationInstancesExplicit`.
//     pub mod get_relation_instances_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub relations: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlayerInstances {
//     }
//     /// Nested message and enum types in `GetPlayerInstances`.
//     pub mod get_player_instances {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub things: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlayerInstancesExplicit {
//     }
//     /// Nested message and enum types in `GetPlayerInstancesExplicit`.
//     pub mod get_player_instances_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub things: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
// }
// // ThingType methods
//
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ThingType {
// }
// /// Nested message and enum types in `ThingType`.
// pub mod thing_type {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetAbstract {
//     }
//     /// Nested message and enum types in `SetAbstract`.
//     pub mod set_abstract {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct UnsetAbstract {
//     }
//     /// Nested message and enum types in `UnsetAbstract`.
//     pub mod unset_abstract {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetInstances {
//     }
//     /// Nested message and enum types in `GetInstances`.
//     pub mod get_instances {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub things: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetInstancesExplicit {
//     }
//     /// Nested message and enum types in `GetInstancesExplicit`.
//     pub mod get_instances_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub things: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetOwns {
//     }
//     /// Nested message and enum types in `GetOwns`.
//     pub mod get_owns {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bool, tag="3")]
//             pub keys_only: bool,
//             #[prost(oneof="req::Filter", tags="1")]
//             pub filter: ::core::option::Option<req::Filter>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Filter {
//                 #[prost(enumeration="super::super::super::attribute_type::ValueType", tag="1")]
//                 ValueType(i32),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub attribute_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetOwnsExplicit {
//     }
//     /// Nested message and enum types in `GetOwnsExplicit`.
//     pub mod get_owns_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bool, tag="3")]
//             pub keys_only: bool,
//             #[prost(oneof="req::Filter", tags="1")]
//             pub filter: ::core::option::Option<req::Filter>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Filter {
//                 #[prost(enumeration="super::super::super::attribute_type::ValueType", tag="1")]
//                 ValueType(i32),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub attribute_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetOwnsOverridden {
//     }
//     /// Nested message and enum types in `GetOwnsOverridden`.
//     pub mod get_owns_overridden {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub attribute_type: ::core::option::Option<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 AttributeType(super::super::super::Type),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetOwns {
//     }
//     /// Nested message and enum types in `SetOwns`.
//     pub mod set_owns {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub attribute_type: ::core::option::Option<super::super::Type>,
//             #[prost(bool, tag="3")]
//             pub is_key: bool,
//             #[prost(oneof="req::Overridden", tags="2")]
//             pub overridden: ::core::option::Option<req::Overridden>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Overridden {
//                 #[prost(message, tag="2")]
//                 OverriddenType(super::super::super::Type),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct UnsetOwns {
//     }
//     /// Nested message and enum types in `UnsetOwns`.
//     pub mod unset_owns {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub attribute_type: ::core::option::Option<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlays {
//     }
//     /// Nested message and enum types in `GetPlays`.
//     pub mod get_plays {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlaysExplicit {
//     }
//     /// Nested message and enum types in `GetPlaysExplicit`.
//     pub mod get_plays_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetPlaysOverridden {
//     }
//     /// Nested message and enum types in `GetPlaysOverridden`.
//     pub mod get_plays_overridden {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub role_type: ::core::option::Option<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 RoleType(super::super::super::Type),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetPlays {
//     }
//     /// Nested message and enum types in `SetPlays`.
//     pub mod set_plays {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub role_type: ::core::option::Option<super::super::Type>,
//             #[prost(oneof="req::Overridden", tags="2")]
//             pub overridden: ::core::option::Option<req::Overridden>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Overridden {
//                 #[prost(message, tag="2")]
//                 OverriddenType(super::super::super::Type),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct UnsetPlays {
//     }
//     /// Nested message and enum types in `UnsetPlays`.
//     pub mod unset_plays {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub role_type: ::core::option::Option<super::super::Type>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetSyntax {
//     }
//     /// Nested message and enum types in `GetSyntax`.
//     pub mod get_syntax {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, tag="1")]
//             pub syntax: ::prost::alloc::string::String,
//         }
//     }
// }
// // EntityType methods
//
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct EntityType {
// }
// /// Nested message and enum types in `EntityType`.
// pub mod entity_type {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Create {
//     }
//     /// Nested message and enum types in `Create`.
//     pub mod create {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub entity: ::core::option::Option<super::super::Thing>,
//         }
//     }
// }
// // RelationType methods
//
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct RelationType {
// }
// /// Nested message and enum types in `RelationType`.
// pub mod relation_type {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Create {
//     }
//     /// Nested message and enum types in `Create`.
//     pub mod create {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub relation: ::core::option::Option<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelates {
//     }
//     /// Nested message and enum types in `GetRelates`.
//     pub mod get_relates {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelatesExplicit {
//     }
//     /// Nested message and enum types in `GetRelatesExplicit`.
//     pub mod get_relates_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub role_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelatesForRoleLabel {
//     }
//     /// Nested message and enum types in `GetRelatesForRoleLabel`.
//     pub mod get_relates_for_role_label {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Role", tags="1")]
//             pub role: ::core::option::Option<res::Role>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Role {
//                 #[prost(message, tag="1")]
//                 RoleType(super::super::super::Type),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRelatesOverridden {
//     }
//     /// Nested message and enum types in `GetRelatesOverridden`.
//     pub mod get_relates_overridden {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 RoleType(super::super::super::Type),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetRelates {
//     }
//     /// Nested message and enum types in `SetRelates`.
//     pub mod set_relates {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//             #[prost(oneof="req::Overridden", tags="2")]
//             pub overridden: ::core::option::Option<req::Overridden>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Overridden {
//                 #[prost(string, tag="2")]
//                 OverriddenLabel(::prost::alloc::string::String),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct UnsetRelates {
//     }
//     /// Nested message and enum types in `UnsetRelates`.
//     pub mod unset_relates {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
// }
// // AttributeType methods
//
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct AttributeType {
// }
// /// Nested message and enum types in `AttributeType`.
// pub mod attribute_type {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Put {
//     }
//     /// Nested message and enum types in `Put`.
//     pub mod put {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub value: ::core::option::Option<super::super::attribute::Value>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub attribute: ::core::option::Option<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Get {
//     }
//     /// Nested message and enum types in `Get`.
//     pub mod get {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(message, optional, tag="1")]
//             pub value: ::core::option::Option<super::super::attribute::Value>,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 Attribute(super::super::super::Thing),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetOwners {
//     }
//     /// Nested message and enum types in `GetOwners`.
//     pub mod get_owners {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bool, tag="1")]
//             pub only_key: bool,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub thing_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetOwnersExplicit {
//     }
//     /// Nested message and enum types in `GetOwnersExplicit`.
//     pub mod get_owners_explicit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bool, tag="1")]
//             pub only_key: bool,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub thing_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRegex {
//     }
//     /// Nested message and enum types in `GetRegex`.
//     pub mod get_regex {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(string, tag="1")]
//             pub regex: ::prost::alloc::string::String,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetRegex {
//     }
//     /// Nested message and enum types in `SetRegex`.
//     pub mod set_regex {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub regex: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetSubtypes {
//     }
//     /// Nested message and enum types in `GetSubtypes`.
//     pub mod get_subtypes {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(oneof="req::Req", tags="1")]
//             pub req: ::core::option::Option<req::Req>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Req {
//                 #[prost(enumeration="super::super::ValueType", tag="1")]
//                 ValueType(i32),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub attribute_types: ::prost::alloc::vec::Vec<super::super::Type>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetInstances {
//     }
//     /// Nested message and enum types in `GetInstances`.
//     pub mod get_instances {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(oneof="req::Req", tags="1")]
//             pub req: ::core::option::Option<req::Req>,
//         }
//         /// Nested message and enum types in `Req`.
//         pub mod req {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Req {
//                 #[prost(enumeration="super::super::ValueType", tag="1")]
//                 ValueType(i32),
//             }
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub attributes: ::prost::alloc::vec::Vec<super::super::Thing>,
//         }
//     }
//     #[derive(Clone, Copy, Debug, PartialEq, Eq, Hash, PartialOrd, Ord, ::prost::Enumeration)]
//     #[repr(i32)]
//     pub enum ValueType {
//         Object = 0,
//         Boolean = 1,
//         Long = 2,
//         Double = 3,
//         String = 4,
//         Datetime = 5,
//     }
//     impl ValueType {
//         /// String value of the enum field names used in the ProtoBuf definition.
//         ///
//         /// The values are not transformed in any way and thus are considered stable
//         /// (if the ProtoBuf definition does not change) and safe for programmatic use.
//         pub fn as_str_name(&self) -> &'static str {
//             match self {
//                 ValueType::Object => "OBJECT",
//                 ValueType::Boolean => "BOOLEAN",
//                 ValueType::Long => "LONG",
//                 ValueType::Double => "DOUBLE",
//                 ValueType::String => "STRING",
//                 ValueType::Datetime => "DATETIME",
//             }
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ConceptMap {
//     #[prost(map="string, message", tag="1")]
//     pub map: ::std::collections::HashMap<::prost::alloc::string::String, Concept>,
//     #[prost(message, optional, tag="2")]
//     pub explainables: ::core::option::Option<Explainables>,
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Explainables {
//     #[prost(map="string, message", tag="1")]
//     pub relations: ::std::collections::HashMap<::prost::alloc::string::String, Explainable>,
//     #[prost(map="string, message", tag="2")]
//     pub attributes: ::std::collections::HashMap<::prost::alloc::string::String, Explainable>,
//     #[prost(map="string, message", tag="3")]
//     pub ownerships: ::std::collections::HashMap<::prost::alloc::string::String, explainables::Owned>,
// }
// /// Nested message and enum types in `Explainables`.
// pub mod explainables {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Owned {
//         #[prost(map="string, message", tag="1")]
//         pub owned: ::std::collections::HashMap<::prost::alloc::string::String, super::Explainable>,
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Explainable {
//     #[prost(string, tag="1")]
//     pub conjunction: ::prost::alloc::string::String,
//     #[prost(int64, tag="2")]
//     pub id: i64,
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct ConceptMapGroup {
//     #[prost(message, optional, tag="1")]
//     pub owner: ::core::option::Option<Concept>,
//     #[prost(message, repeated, tag="2")]
//     pub concept_maps: ::prost::alloc::vec::Vec<ConceptMap>,
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Numeric {
//     #[prost(oneof="numeric::Value", tags="1, 2, 3")]
//     pub value: ::core::option::Option<numeric::Value>,
// }
// /// Nested message and enum types in `Numeric`.
// pub mod numeric {
//     #[derive(Clone, PartialEq, ::prost::Oneof)]
//     pub enum Value {
//         #[prost(int64, tag="1")]
//         LongValue(i64),
//         #[prost(double, tag="2")]
//         DoubleValue(f64),
//         #[prost(bool, tag="3")]
//         Nan(bool),
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct NumericGroup {
//     #[prost(message, optional, tag="1")]
//     pub owner: ::core::option::Option<Concept>,
//     #[prost(message, optional, tag="2")]
//     pub number: ::core::option::Option<Numeric>,
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct LogicManager {
// }
// /// Nested message and enum types in `LogicManager`.
// pub mod logic_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(oneof="req::Req", tags="1, 2, 3")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             #[prost(message, tag="1")]
//             GetRuleReq(super::get_rule::Req),
//             #[prost(message, tag="2")]
//             PutRuleReq(super::put_rule::Req),
//             #[prost(message, tag="3")]
//             GetRulesReq(super::get_rules::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(oneof="res::Res", tags="1, 2")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="1")]
//             GetRuleRes(super::get_rule::Res),
//             #[prost(message, tag="2")]
//             PutRuleRes(super::put_rule::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct ResPart {
//         #[prost(message, optional, tag="1")]
//         pub get_rules_res_part: ::core::option::Option<get_rules::ResPart>,
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRule {
//     }
//     /// Nested message and enum types in `GetRule`.
//     pub mod get_rule {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(oneof="res::Res", tags="1")]
//             pub res: ::core::option::Option<res::Res>,
//         }
//         /// Nested message and enum types in `Res`.
//         pub mod res {
//             #[derive(Clone, PartialEq, ::prost::Oneof)]
//             pub enum Res {
//                 #[prost(message, tag="1")]
//                 Rule(super::super::super::Rule),
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct PutRule {
//     }
//     /// Nested message and enum types in `PutRule`.
//     pub mod put_rule {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//             #[prost(string, tag="2")]
//             pub when: ::prost::alloc::string::String,
//             #[prost(string, tag="3")]
//             pub then: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub rule: ::core::option::Option<super::super::Rule>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct GetRules {
//     }
//     /// Nested message and enum types in `GetRules`.
//     pub mod get_rules {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub rules: ::prost::alloc::vec::Vec<super::super::Rule>,
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Rule {
//     #[prost(string, tag="1")]
//     pub label: ::prost::alloc::string::String,
//     #[prost(string, tag="2")]
//     pub when: ::prost::alloc::string::String,
//     #[prost(string, tag="3")]
//     pub then: ::prost::alloc::string::String,
// }
// /// Nested message and enum types in `Rule`.
// pub mod rule {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(string, tag="1")]
//         pub label: ::prost::alloc::string::String,
//         #[prost(oneof="req::Req", tags="100, 101")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             #[prost(message, tag="100")]
//             RuleDeleteReq(super::delete::Req),
//             #[prost(message, tag="101")]
//             RuleSetLabelReq(super::set_label::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(oneof="res::Res", tags="100, 101")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="100")]
//             RuleDeleteRes(super::delete::Res),
//             #[prost(message, tag="101")]
//             RuleSetLabelRes(super::set_label::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Delete {
//     }
//     /// Nested message and enum types in `Delete`.
//     pub mod delete {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct SetLabel {
//     }
//     /// Nested message and enum types in `SetLabel`.
//     pub mod set_label {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub label: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Explanation {
//     #[prost(message, optional, tag="1")]
//     pub rule: ::core::option::Option<Rule>,
//     #[prost(map="string, message", tag="2")]
//     pub var_mapping: ::std::collections::HashMap<::prost::alloc::string::String, explanation::VarList>,
//     #[prost(message, optional, tag="3")]
//     pub condition: ::core::option::Option<ConceptMap>,
//     #[prost(message, optional, tag="4")]
//     pub conclusion: ::core::option::Option<ConceptMap>,
// }
// /// Nested message and enum types in `Explanation`.
// pub mod explanation {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct VarList {
//         #[prost(string, repeated, tag="1")]
//         pub vars: ::prost::alloc::vec::Vec<::prost::alloc::string::String>,
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct QueryManager {
// }
// /// Nested message and enum types in `QueryManager`.
// pub mod query_manager {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(message, optional, tag="1")]
//         pub options: ::core::option::Option<super::Options>,
//         #[prost(oneof="req::Req", tags="100, 101, 102, 103, 104, 105, 106, 107, 108, 109")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             #[prost(message, tag="100")]
//             DefineReq(super::define::Req),
//             #[prost(message, tag="101")]
//             UndefineReq(super::undefine::Req),
//             #[prost(message, tag="102")]
//             MatchReq(super::r#match::Req),
//             #[prost(message, tag="103")]
//             MatchAggregateReq(super::match_aggregate::Req),
//             #[prost(message, tag="104")]
//             MatchGroupReq(super::match_group::Req),
//             #[prost(message, tag="105")]
//             MatchGroupAggregateReq(super::match_group_aggregate::Req),
//             #[prost(message, tag="106")]
//             InsertReq(super::insert::Req),
//             #[prost(message, tag="107")]
//             DeleteReq(super::delete::Req),
//             #[prost(message, tag="108")]
//             UpdateReq(super::update::Req),
//             #[prost(message, tag="109")]
//             ExplainReq(super::explain::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(oneof="res::Res", tags="100, 101, 102, 104")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="100")]
//             DefineRes(super::define::Res),
//             #[prost(message, tag="101")]
//             UndefineRes(super::undefine::Res),
//             #[prost(message, tag="102")]
//             MatchAggregateRes(super::match_aggregate::Res),
//             #[prost(message, tag="104")]
//             DeleteRes(super::delete::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct ResPart {
//         #[prost(oneof="res_part::Res", tags="100, 101, 102, 103, 104, 105")]
//         pub res: ::core::option::Option<res_part::Res>,
//     }
//     /// Nested message and enum types in `ResPart`.
//     pub mod res_part {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="100")]
//             MatchResPart(super::r#match::ResPart),
//             #[prost(message, tag="101")]
//             MatchGroupResPart(super::match_group::ResPart),
//             #[prost(message, tag="102")]
//             MatchGroupAggregateResPart(super::match_group_aggregate::ResPart),
//             #[prost(message, tag="103")]
//             InsertResPart(super::insert::ResPart),
//             #[prost(message, tag="104")]
//             UpdateResPart(super::update::ResPart),
//             #[prost(message, tag="105")]
//             ExplainResPart(super::explain::ResPart),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Match {
//     }
//     /// Nested message and enum types in `Match`.
//     pub mod r#match {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub answers: ::prost::alloc::vec::Vec<super::super::ConceptMap>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct MatchAggregate {
//     }
//     /// Nested message and enum types in `MatchAggregate`.
//     pub mod match_aggregate {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//             #[prost(message, optional, tag="1")]
//             pub answer: ::core::option::Option<super::super::Numeric>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct MatchGroup {
//     }
//     /// Nested message and enum types in `MatchGroup`.
//     pub mod match_group {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub answers: ::prost::alloc::vec::Vec<super::super::ConceptMapGroup>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct MatchGroupAggregate {
//     }
//     /// Nested message and enum types in `MatchGroupAggregate`.
//     pub mod match_group_aggregate {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub answers: ::prost::alloc::vec::Vec<super::super::NumericGroup>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Explain {
//     }
//     /// Nested message and enum types in `Explain`.
//     pub mod explain {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(int64, tag="1")]
//             pub explainable_id: i64,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub explanations: ::prost::alloc::vec::Vec<super::super::Explanation>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Insert {
//     }
//     /// Nested message and enum types in `Insert`.
//     pub mod insert {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub answers: ::prost::alloc::vec::Vec<super::super::ConceptMap>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Delete {
//     }
//     /// Nested message and enum types in `Delete`.
//     pub mod delete {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Update {
//     }
//     /// Nested message and enum types in `Update`.
//     pub mod update {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(message, repeated, tag="1")]
//             pub answers: ::prost::alloc::vec::Vec<super::super::ConceptMap>,
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Define {
//     }
//     /// Nested message and enum types in `Define`.
//     pub mod define {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Undefine {
//     }
//     /// Nested message and enum types in `Undefine`.
//     pub mod undefine {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(string, tag="1")]
//             pub query: ::prost::alloc::string::String,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
// }
// #[derive(Clone, PartialEq, ::prost::Message)]
// pub struct Transaction {
// }
// /// Nested message and enum types in `Transaction`.
// pub mod transaction {
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Client {
//         #[prost(message, repeated, tag="1")]
//         pub reqs: ::prost::alloc::vec::Vec<Req>,
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Server {
//         #[prost(oneof="server::Server", tags="2, 3")]
//         pub server: ::core::option::Option<server::Server>,
//     }
//     /// Nested message and enum types in `Server`.
//     pub mod server {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Server {
//             #[prost(message, tag="2")]
//             Res(super::Res),
//             #[prost(message, tag="3")]
//             ResPart(super::ResPart),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Req {
//         #[prost(bytes="vec", tag="1")]
//         pub req_id: ::prost::alloc::vec::Vec<u8>,
//         #[prost(map="string, string", tag="2")]
//         pub metadata: ::std::collections::HashMap<::prost::alloc::string::String, ::prost::alloc::string::String>,
//         #[prost(oneof="req::Req", tags="3, 4, 5, 6, 7, 8, 9, 10, 11, 12")]
//         pub req: ::core::option::Option<req::Req>,
//     }
//     /// Nested message and enum types in `Req`.
//     pub mod req {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Req {
//             #[prost(message, tag="3")]
//             OpenReq(super::open::Req),
//             #[prost(message, tag="4")]
//             StreamReq(super::stream::Req),
//             #[prost(message, tag="5")]
//             CommitReq(super::commit::Req),
//             #[prost(message, tag="6")]
//             RollbackReq(super::rollback::Req),
//             #[prost(message, tag="7")]
//             QueryManagerReq(super::super::query_manager::Req),
//             #[prost(message, tag="8")]
//             ConceptManagerReq(super::super::concept_manager::Req),
//             #[prost(message, tag="9")]
//             LogicManagerReq(super::super::logic_manager::Req),
//             #[prost(message, tag="10")]
//             RuleReq(super::super::rule::Req),
//             #[prost(message, tag="11")]
//             TypeReq(super::super::r#type::Req),
//             #[prost(message, tag="12")]
//             ThingReq(super::super::thing::Req),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Res {
//         #[prost(bytes="vec", tag="1")]
//         pub req_id: ::prost::alloc::vec::Vec<u8>,
//         #[prost(oneof="res::Res", tags="2, 3, 4, 5, 6, 7, 8, 9, 10")]
//         pub res: ::core::option::Option<res::Res>,
//     }
//     /// Nested message and enum types in `Res`.
//     pub mod res {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="2")]
//             OpenRes(super::open::Res),
//             #[prost(message, tag="3")]
//             CommitRes(super::commit::Res),
//             #[prost(message, tag="4")]
//             RollbackRes(super::rollback::Res),
//             #[prost(message, tag="5")]
//             QueryManagerRes(super::super::query_manager::Res),
//             #[prost(message, tag="6")]
//             ConceptManagerRes(super::super::concept_manager::Res),
//             #[prost(message, tag="7")]
//             LogicManagerRes(super::super::logic_manager::Res),
//             #[prost(message, tag="8")]
//             RuleRes(super::super::rule::Res),
//             #[prost(message, tag="9")]
//             TypeRes(super::super::r#type::Res),
//             #[prost(message, tag="10")]
//             ThingRes(super::super::thing::Res),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct ResPart {
//         #[prost(bytes="vec", tag="1")]
//         pub req_id: ::prost::alloc::vec::Vec<u8>,
//         #[prost(oneof="res_part::Res", tags="2, 3, 4, 5, 6")]
//         pub res: ::core::option::Option<res_part::Res>,
//     }
//     /// Nested message and enum types in `ResPart`.
//     pub mod res_part {
//         #[derive(Clone, PartialEq, ::prost::Oneof)]
//         pub enum Res {
//             #[prost(message, tag="2")]
//             StreamResPart(super::stream::ResPart),
//             #[prost(message, tag="3")]
//             QueryManagerResPart(super::super::query_manager::ResPart),
//             #[prost(message, tag="4")]
//             LogicManagerResPart(super::super::logic_manager::ResPart),
//             #[prost(message, tag="5")]
//             TypeResPart(super::super::r#type::ResPart),
//             #[prost(message, tag="6")]
//             ThingResPart(super::super::thing::ResPart),
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Open {
//     }
//     /// Nested message and enum types in `Open`.
//     pub mod open {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//             #[prost(bytes="vec", tag="1")]
//             pub session_id: ::prost::alloc::vec::Vec<u8>,
//             #[prost(enumeration="super::Type", tag="2")]
//             pub r#type: i32,
//             #[prost(message, optional, tag="3")]
//             pub options: ::core::option::Option<super::super::Options>,
//             #[prost(int32, tag="4")]
//             pub network_latency_millis: i32,
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Stream {
//     }
//     /// Nested message and enum types in `Stream`.
//     pub mod stream {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct ResPart {
//             #[prost(enumeration="State", tag="1")]
//             pub state: i32,
//         }
//         #[derive(Clone, Copy, Debug, PartialEq, Eq, Hash, PartialOrd, Ord, ::prost::Enumeration)]
//         #[repr(i32)]
//         pub enum State {
//             Continue = 0,
//             Done = 1,
//         }
//         impl State {
//             /// String value of the enum field names used in the ProtoBuf definition.
//             ///
//             /// The values are not transformed in any way and thus are considered stable
//             /// (if the ProtoBuf definition does not change) and safe for programmatic use.
//             pub fn as_str_name(&self) -> &'static str {
//                 match self {
//                     State::Continue => "CONTINUE",
//                     State::Done => "DONE",
//                 }
//             }
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Commit {
//     }
//     /// Nested message and enum types in `Commit`.
//     pub mod commit {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, PartialEq, ::prost::Message)]
//     pub struct Rollback {
//     }
//     /// Nested message and enum types in `Rollback`.
//     pub mod rollback {
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Req {
//         }
//         #[derive(Clone, PartialEq, ::prost::Message)]
//         pub struct Res {
//         }
//     }
//     #[derive(Clone, Copy, Debug, PartialEq, Eq, Hash, PartialOrd, Ord, ::prost::Enumeration)]
//     #[repr(i32)]
//     pub enum Type {
//         Read = 0,
//         Write = 1,
//     }
//     impl Type {
//         /// String value of the enum field names used in the ProtoBuf definition.
//         ///
//         /// The values are not transformed in any way and thus are considered stable
//         /// (if the ProtoBuf definition does not change) and safe for programmatic use.
//         pub fn as_str_name(&self) -> &'static str {
//             match self {
//                 Type::Read => "READ",
//                 Type::Write => "WRITE",
//             }
//         }
//     }
// }
// /// Generated client implementations.
// pub mod type_db_client {
//     #![allow(unused_variables, dead_code, missing_docs, clippy::let_unit_value)]
//     use tonic::codegen::*;
//     use tonic::codegen::http::Uri;
//     #[derive(Debug, Clone)]
//     pub struct TypeDbClient<T> {
//         inner: tonic::client::Grpc<T>,
//     }
//     impl TypeDbClient<tonic::transport::Channel> {
//         /// Attempt to create a new client by connecting to a given endpoint.
//         pub async fn connect<D>(dst: D) -> Result<Self, tonic::transport::Error>
//         where
//             D: std::convert::TryInto<tonic::transport::Endpoint>,
//             D::Error: Into<StdError>,
//         {
//             let conn = tonic::transport::Endpoint::new(dst)?.connect().await?;
//             Ok(Self::new(conn))
//         }
//     }
//     impl<T> TypeDbClient<T>
//     where
//         T: tonic::client::GrpcService<tonic::body::BoxBody>,
//         T::Error: Into<StdError>,
//         T::ResponseBody: Body<Data = Bytes> + Send + 'static,
//         <T::ResponseBody as Body>::Error: Into<StdError> + Send,
//     {
//         pub fn new(inner: T) -> Self {
//             let inner = tonic::client::Grpc::new(inner);
//             Self { inner }
//         }
//         pub fn with_origin(inner: T, origin: Uri) -> Self {
//             let inner = tonic::client::Grpc::with_origin(inner, origin);
//             Self { inner }
//         }
//         pub fn with_interceptor<F>(
//             inner: T,
//             interceptor: F,
//         ) -> TypeDbClient<InterceptedService<T, F>>
//         where
//             F: tonic::service::Interceptor,
//             T::ResponseBody: Default,
//             T: tonic::codegen::Service<
//                 http::Request<tonic::body::BoxBody>,
//                 Response = http::Response<
//                     <T as tonic::client::GrpcService<tonic::body::BoxBody>>::ResponseBody,
//                 >,
//             >,
//             <T as tonic::codegen::Service<
//                 http::Request<tonic::body::BoxBody>,
//             >>::Error: Into<StdError> + Send + Sync,
//         {
//             TypeDbClient::new(InterceptedService::new(inner, interceptor))
//         }
//         /// Compress requests with the given encoding.
//         ///
//         /// This requires the server to support it otherwise it might respond with an
//         /// error.
//         #[must_use]
//         pub fn send_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.inner = self.inner.send_compressed(encoding);
//             self
//         }
//         /// Enable decompressing responses.
//         #[must_use]
//         pub fn accept_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.inner = self.inner.accept_compressed(encoding);
//             self
//         }
//         /// Database Manager API
//         pub async fn databases_contains(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database_manager::contains::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database_manager::contains::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/databases_contains",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn databases_create(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database_manager::create::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database_manager::create::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/databases_create",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn databases_all(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database_manager::all::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database_manager::all::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/databases_all",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// Database API
//         pub async fn database_schema(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database::schema::Req>,
//         ) -> Result<tonic::Response<super::core_database::schema::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/database_schema",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn database_type_schema(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database::type_schema::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database::type_schema::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/database_type_schema",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn database_rule_schema(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database::rule_schema::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database::rule_schema::Res>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/database_rule_schema",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn database_delete(
//             &mut self,
//             request: impl tonic::IntoRequest<super::core_database::delete::Req>,
//         ) -> Result<tonic::Response<super::core_database::delete::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/database_delete",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// Session API
//         pub async fn session_open(
//             &mut self,
//             request: impl tonic::IntoRequest<super::session::open::Req>,
//         ) -> Result<tonic::Response<super::session::open::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/session_open",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         pub async fn session_close(
//             &mut self,
//             request: impl tonic::IntoRequest<super::session::close::Req>,
//         ) -> Result<tonic::Response<super::session::close::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/session_close",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// Checks with the server that the session is still alive, and informs it that it should be kept alive.
//         pub async fn session_pulse(
//             &mut self,
//             request: impl tonic::IntoRequest<super::session::pulse::Req>,
//         ) -> Result<tonic::Response<super::session::pulse::Res>, tonic::Status> {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/session_pulse",
//             );
//             self.inner.unary(request.into_request(), path, codec).await
//         }
//         /// Transaction Streaming API
//         /// Opens a bi-directional stream representing a stateful transaction, streaming
//         /// requests and responses back-and-forth. The first transaction client message must
//         /// be {Transaction.Open.Req}. Closing the stream closes the transaction.
//         pub async fn transaction(
//             &mut self,
//             request: impl tonic::IntoStreamingRequest<
//                 Message = super::transaction::Client,
//             >,
//         ) -> Result<
//             tonic::Response<tonic::codec::Streaming<super::transaction::Server>>,
//             tonic::Status,
//         > {
//             self.inner
//                 .ready()
//                 .await
//                 .map_err(|e| {
//                     tonic::Status::new(
//                         tonic::Code::Unknown,
//                         format!("Service was not ready: {}", e.into()),
//                     )
//                 })?;
//             let codec = tonic::codec::ProstCodec::default();
//             let path = http::uri::PathAndQuery::from_static(
//                 "/typedb.protocol.TypeDB/transaction",
//             );
//             self.inner.streaming(request.into_streaming_request(), path, codec).await
//         }
//     }
// }
// /// Generated server implementations.
// pub mod type_db_server {
//     #![allow(unused_variables, dead_code, missing_docs, clippy::let_unit_value)]
//     use tonic::codegen::*;
//     ///Generated trait containing gRPC methods that should be implemented for use with TypeDbServer.
//     #[async_trait]
//     pub trait TypeDb: Send + Sync + 'static {
//         /// Database Manager API
//         async fn databases_contains(
//             &self,
//             request: tonic::Request<super::core_database_manager::contains::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database_manager::contains::Res>,
//             tonic::Status,
//         >;
//         async fn databases_create(
//             &self,
//             request: tonic::Request<super::core_database_manager::create::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database_manager::create::Res>,
//             tonic::Status,
//         >;
//         async fn databases_all(
//             &self,
//             request: tonic::Request<super::core_database_manager::all::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database_manager::all::Res>,
//             tonic::Status,
//         >;
//         /// Database API
//         async fn database_schema(
//             &self,
//             request: tonic::Request<super::core_database::schema::Req>,
//         ) -> Result<tonic::Response<super::core_database::schema::Res>, tonic::Status>;
//         async fn database_type_schema(
//             &self,
//             request: tonic::Request<super::core_database::type_schema::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database::type_schema::Res>,
//             tonic::Status,
//         >;
//         async fn database_rule_schema(
//             &self,
//             request: tonic::Request<super::core_database::rule_schema::Req>,
//         ) -> Result<
//             tonic::Response<super::core_database::rule_schema::Res>,
//             tonic::Status,
//         >;
//         async fn database_delete(
//             &self,
//             request: tonic::Request<super::core_database::delete::Req>,
//         ) -> Result<tonic::Response<super::core_database::delete::Res>, tonic::Status>;
//         /// Session API
//         async fn session_open(
//             &self,
//             request: tonic::Request<super::session::open::Req>,
//         ) -> Result<tonic::Response<super::session::open::Res>, tonic::Status>;
//         async fn session_close(
//             &self,
//             request: tonic::Request<super::session::close::Req>,
//         ) -> Result<tonic::Response<super::session::close::Res>, tonic::Status>;
//         /// Checks with the server that the session is still alive, and informs it that it should be kept alive.
//         async fn session_pulse(
//             &self,
//             request: tonic::Request<super::session::pulse::Req>,
//         ) -> Result<tonic::Response<super::session::pulse::Res>, tonic::Status>;
//         ///Server streaming response type for the transaction method.
//         type transactionStream: futures_core::Stream<
//                 Item = Result<super::transaction::Server, tonic::Status>,
//             >
//             + Send
//             + 'static;
//         /// Transaction Streaming API
//         /// Opens a bi-directional stream representing a stateful transaction, streaming
//         /// requests and responses back-and-forth. The first transaction client message must
//         /// be {Transaction.Open.Req}. Closing the stream closes the transaction.
//         async fn transaction(
//             &self,
//             request: tonic::Request<tonic::Streaming<super::transaction::Client>>,
//         ) -> Result<tonic::Response<Self::transactionStream>, tonic::Status>;
//     }
//     #[derive(Debug)]
//     pub struct TypeDbServer<T: TypeDb> {
//         inner: _Inner<T>,
//         accept_compression_encodings: EnabledCompressionEncodings,
//         send_compression_encodings: EnabledCompressionEncodings,
//     }
//     struct _Inner<T>(Arc<T>);
//     impl<T: TypeDb> TypeDbServer<T> {
//         pub fn new(inner: T) -> Self {
//             Self::from_arc(Arc::new(inner))
//         }
//         pub fn from_arc(inner: Arc<T>) -> Self {
//             let inner = _Inner(inner);
//             Self {
//                 inner,
//                 accept_compression_encodings: Default::default(),
//                 send_compression_encodings: Default::default(),
//             }
//         }
//         pub fn with_interceptor<F>(
//             inner: T,
//             interceptor: F,
//         ) -> InterceptedService<Self, F>
//         where
//             F: tonic::service::Interceptor,
//         {
//             InterceptedService::new(Self::new(inner), interceptor)
//         }
//         /// Enable decompressing requests with the given encoding.
//         #[must_use]
//         pub fn accept_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.accept_compression_encodings.enable(encoding);
//             self
//         }
//         /// Compress responses with the given encoding, if the client supports it.
//         #[must_use]
//         pub fn send_compressed(mut self, encoding: CompressionEncoding) -> Self {
//             self.send_compression_encodings.enable(encoding);
//             self
//         }
//     }
//     impl<T, B> tonic::codegen::Service<http::Request<B>> for TypeDbServer<T>
//     where
//         T: TypeDb,
//         B: Body + Send + 'static,
//         B::Error: Into<StdError> + Send + 'static,
//     {
//         type Response = http::Response<tonic::body::BoxBody>;
//         type Error = std::convert::Infallible;
//         type Future = BoxFuture<Self::Response, Self::Error>;
//         fn poll_ready(
//             &mut self,
//             _cx: &mut Context<'_>,
//         ) -> Poll<Result<(), Self::Error>> {
//             Poll::Ready(Ok(()))
//         }
//         fn call(&mut self, req: http::Request<B>) -> Self::Future {
//             let inner = self.inner.clone();
//             match req.uri().path() {
//                 "/typedb.protocol.TypeDB/databases_contains" => {
//                     #[allow(non_camel_case_types)]
//                     struct databases_containsSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<
//                         super::core_database_manager::contains::Req,
//                     > for databases_containsSvc<T> {
//                         type Response = super::core_database_manager::contains::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::core_database_manager::contains::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).databases_contains(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = databases_containsSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/databases_create" => {
//                     #[allow(non_camel_case_types)]
//                     struct databases_createSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<
//                         super::core_database_manager::create::Req,
//                     > for databases_createSvc<T> {
//                         type Response = super::core_database_manager::create::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::core_database_manager::create::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).databases_create(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = databases_createSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/databases_all" => {
//                     #[allow(non_camel_case_types)]
//                     struct databases_allSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::core_database_manager::all::Req>
//                     for databases_allSvc<T> {
//                         type Response = super::core_database_manager::all::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::core_database_manager::all::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).databases_all(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = databases_allSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/database_schema" => {
//                     #[allow(non_camel_case_types)]
//                     struct database_schemaSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::core_database::schema::Req>
//                     for database_schemaSvc<T> {
//                         type Response = super::core_database::schema::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::core_database::schema::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).database_schema(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = database_schemaSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/database_type_schema" => {
//                     #[allow(non_camel_case_types)]
//                     struct database_type_schemaSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::core_database::type_schema::Req>
//                     for database_type_schemaSvc<T> {
//                         type Response = super::core_database::type_schema::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::core_database::type_schema::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).database_type_schema(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = database_type_schemaSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/database_rule_schema" => {
//                     #[allow(non_camel_case_types)]
//                     struct database_rule_schemaSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::core_database::rule_schema::Req>
//                     for database_rule_schemaSvc<T> {
//                         type Response = super::core_database::rule_schema::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 super::core_database::rule_schema::Req,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).database_rule_schema(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = database_rule_schemaSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/database_delete" => {
//                     #[allow(non_camel_case_types)]
//                     struct database_deleteSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::core_database::delete::Req>
//                     for database_deleteSvc<T> {
//                         type Response = super::core_database::delete::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::core_database::delete::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).database_delete(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = database_deleteSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/session_open" => {
//                     #[allow(non_camel_case_types)]
//                     struct session_openSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::session::open::Req>
//                     for session_openSvc<T> {
//                         type Response = super::session::open::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::session::open::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).session_open(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = session_openSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/session_close" => {
//                     #[allow(non_camel_case_types)]
//                     struct session_closeSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::session::close::Req>
//                     for session_closeSvc<T> {
//                         type Response = super::session::close::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::session::close::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).session_close(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = session_closeSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/session_pulse" => {
//                     #[allow(non_camel_case_types)]
//                     struct session_pulseSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::UnaryService<super::session::pulse::Req>
//                     for session_pulseSvc<T> {
//                         type Response = super::session::pulse::Res;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::Response>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<super::session::pulse::Req>,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move {
//                                 (*inner).session_pulse(request).await
//                             };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = session_pulseSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.unary(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 "/typedb.protocol.TypeDB/transaction" => {
//                     #[allow(non_camel_case_types)]
//                     struct transactionSvc<T: TypeDb>(pub Arc<T>);
//                     impl<
//                         T: TypeDb,
//                     > tonic::server::StreamingService<super::transaction::Client>
//                     for transactionSvc<T> {
//                         type Response = super::transaction::Server;
//                         type ResponseStream = T::transactionStream;
//                         type Future = BoxFuture<
//                             tonic::Response<Self::ResponseStream>,
//                             tonic::Status,
//                         >;
//                         fn call(
//                             &mut self,
//                             request: tonic::Request<
//                                 tonic::Streaming<super::transaction::Client>,
//                             >,
//                         ) -> Self::Future {
//                             let inner = self.0.clone();
//                             let fut = async move { (*inner).transaction(request).await };
//                             Box::pin(fut)
//                         }
//                     }
//                     let accept_compression_encodings = self.accept_compression_encodings;
//                     let send_compression_encodings = self.send_compression_encodings;
//                     let inner = self.inner.clone();
//                     let fut = async move {
//                         let inner = inner.0;
//                         let method = transactionSvc(inner);
//                         let codec = tonic::codec::ProstCodec::default();
//                         let mut grpc = tonic::server::Grpc::new(codec)
//                             .apply_compression_config(
//                                 accept_compression_encodings,
//                                 send_compression_encodings,
//                             );
//                         let res = grpc.streaming(method, req).await;
//                         Ok(res)
//                     };
//                     Box::pin(fut)
//                 }
//                 _ => {
//                     Box::pin(async move {
//                         Ok(
//                             http::Response::builder()
//                                 .status(200)
//                                 .header("grpc-status", "12")
//                                 .header("content-type", "application/grpc")
//                                 .body(empty_body())
//                                 .unwrap(),
//                         )
//                     })
//                 }
//             }
//         }
//     }
//     impl<T: TypeDb> Clone for TypeDbServer<T> {
//         fn clone(&self) -> Self {
//             let inner = self.inner.clone();
//             Self {
//                 inner,
//                 accept_compression_encodings: self.accept_compression_encodings,
//                 send_compression_encodings: self.send_compression_encodings,
//             }
//         }
//     }
//     impl<T: TypeDb> Clone for _Inner<T> {
//         fn clone(&self) -> Self {
//             Self(self.0.clone())
//         }
//     }
//     impl<T: std::fmt::Debug> std::fmt::Debug for _Inner<T> {
//         fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
//             write!(f, "{:?}", self.0)
//         }
//     }
//     impl<T: TypeDb> tonic::server::NamedService for TypeDbServer<T> {
//         const NAME: &'static str = "typedb.protocol.TypeDB";
//     }
// }
