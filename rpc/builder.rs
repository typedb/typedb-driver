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

pub(crate) mod core {
    pub(crate) mod database_manager {
        use typedb_protocol::core_database::{CoreDatabaseManager_All_Req, CoreDatabaseManager_Contains_Req, CoreDatabaseManager_Create_Req};

        pub(crate) fn contains_req(name: &str) -> CoreDatabaseManager_Contains_Req {
            let mut req = CoreDatabaseManager_Contains_Req::new();
            req.set_name(String::from(name));
            req
        }

        pub(crate) fn create_req(name: &str) -> CoreDatabaseManager_Create_Req {
            let mut req = CoreDatabaseManager_Create_Req::new();
            req.set_name(String::from(name));
            req
        }

        pub(crate) fn all_req() -> CoreDatabaseManager_All_Req {
            CoreDatabaseManager_All_Req::new()
        }
    }

    pub(crate) mod database {
        use typedb_protocol::core_database::{CoreDatabase_Delete_Req, CoreDatabase_Schema_Req};

        pub(crate) fn schema_req(name: String) -> CoreDatabase_Schema_Req {
            let mut req = CoreDatabase_Schema_Req::new();
            req.name = name;
            req
        }

        pub(crate) fn delete_req(name: String) -> CoreDatabase_Delete_Req {
            let mut req = CoreDatabase_Delete_Req::new();
            req.name = name;
            req
        }
    }
}

pub(crate) mod cluster {
    pub(crate) mod server_manager {
        use typedb_protocol::cluster_server::ServerManager_All_Req;

        pub(crate) fn all_req() -> ServerManager_All_Req {
            ServerManager_All_Req::new()
        }
    }

    pub(crate) mod user_manager {
        use typedb_protocol::cluster_user::{ClusterUserManager_All_Req, ClusterUserManager_Contains_Req, ClusterUserManager_Create_Req};

        pub(crate) fn contains_req(username: &str) -> ClusterUserManager_Contains_Req {
            let mut req = ClusterUserManager_Contains_Req::new();
            req.username = String::from(username);
            req
        }

        pub(crate) fn create_req(username: &str, password: &str) -> ClusterUserManager_Create_Req {
            let mut req = ClusterUserManager_Create_Req::new();
            req.username = String::from(username);
            req.password = String::from(password);
            req
        }

        pub(crate) fn all_req() -> ClusterUserManager_All_Req {
            ClusterUserManager_All_Req::new()
        }
    }

    pub(crate) mod user {
        use typedb_protocol::cluster_user::{ClusterUser_Delete_Req, ClusterUser_Password_Req, ClusterUser_Token_Req};

        pub(crate) fn password_req(username: &str, password: &str) -> ClusterUser_Password_Req {
            let mut req = ClusterUser_Password_Req::new();
            req.username = String::from(username);
            req.password = String::from(password);
            req
        }

        pub(crate) fn token_req(username: &str) -> ClusterUser_Token_Req {
            let mut req = ClusterUser_Token_Req::new();
            req.username = String::from(username);
            req
        }

        pub(crate) fn delete_req(username: &str) -> ClusterUser_Delete_Req {
            let mut req = ClusterUser_Delete_Req::new();
            req.username = String::from(username);
            req
        }
    }

    pub(crate) mod database_manager {
        use typedb_protocol::cluster_database::{ClusterDatabaseManager_All_Req, ClusterDatabaseManager_Get_Req};

        pub(crate) fn get_req(name: &str) -> ClusterDatabaseManager_Get_Req {
            let mut req = ClusterDatabaseManager_Get_Req::new();
            req.name = String::from(name);
            req
        }

        pub(crate) fn all_req() -> ClusterDatabaseManager_All_Req {
            ClusterDatabaseManager_All_Req::new()
        }
    }
}

pub(crate) mod session {
    use typedb_protocol::options::Options;
    use typedb_protocol::session::{Session_Close_Req, Session_Open_Req, Session_Type};

    pub(crate) fn open_req(database: &str, session_type: Session_Type, options: Options) -> Session_Open_Req {
        let mut req = Session_Open_Req::new();
        req.database = String::from(database);
        req.field_type = session_type;
        req.set_options(options);
        req
    }

    pub(crate) fn close_req(session_id: Vec<u8>) -> Session_Close_Req {
        let mut req = Session_Close_Req::new();
        req.session_id = session_id;
        req
    }
}

pub(crate) mod transaction {
    use protobuf::RepeatedField;
    use typedb_protocol::transaction::{Transaction_Client, Transaction_Commit_Req, Transaction_Open_Req, Transaction_Req, Transaction_Rollback_Req, Transaction_Stream_Req, Transaction_Type};

    pub(crate) fn client_msg(reqs: Vec<Transaction_Req>) -> Transaction_Client {
        let mut req = Transaction_Client::new();
        req.reqs = RepeatedField::from(reqs);
        req
    }

    pub(crate) fn stream_req(req_id: Vec<u8>) -> Transaction_Req {
        let mut req = Transaction_Req::new();
        req.req_id = req_id;
        req.set_stream_req(Transaction_Stream_Req::new());
        req
    }

    pub(crate) fn open_req(session_id: Vec<u8>, transaction_type: Transaction_Type, network_latency_millis: u32) -> Transaction_Req {
        let mut req = Transaction_Req::new();
        let mut open_req = Transaction_Open_Req::new();
        open_req.session_id = session_id;
        open_req.field_type = transaction_type;
        open_req.network_latency_millis = network_latency_millis as i32;
        req.set_open_req(open_req);
        req
    }

    pub(crate) fn commit_req() -> Transaction_Req {
        let mut req = Transaction_Req::new();
        req.set_commit_req(Transaction_Commit_Req::new());
        req
    }

    pub(crate) fn rollback_req() -> Transaction_Req {
        let mut req = Transaction_Req::new();
        req.set_rollback_req(Transaction_Rollback_Req::new());
        req
    }
}
