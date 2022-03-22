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

pub(crate) mod query_manager {
    use typedb_protocol::query::{QueryManager_Define_Req, QueryManager_Delete_Req, QueryManager_Explain_Req, QueryManager_Insert_Req, QueryManager_Match_Req, QueryManager_MatchAggregate_Req, QueryManager_MatchGroup_Req, QueryManager_MatchGroupAggregate_Req, QueryManager_Req, QueryManager_Undefine_Req, QueryManager_Update_Req};
    use typedb_protocol::transaction::Transaction_Req;

    pub(crate) fn query_manager_req(req: QueryManager_Req) -> Transaction_Req {
        let mut tx_req = Transaction_Req::new();
        tx_req.set_query_manager_req(req);
        tx_req
    }

    pub(crate) fn define_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut define_req = QueryManager_Define_Req::new();
        define_req.query = String::from(query);
        req.set_define_req(define_req);
        query_manager_req(req)
    }

    pub(crate) fn undefine_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut undefine_req = QueryManager_Undefine_Req::new();
        undefine_req.query = String::from(query);
        req.set_undefine_req(undefine_req);
        query_manager_req(req)
    }

    pub(crate) fn match_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut match_req = QueryManager_Match_Req::new();
        match_req.query = String::from(query);
        req.set_match_req(match_req);
        query_manager_req(req)
    }

    pub(crate) fn match_aggregate_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut match_aggregate_req = QueryManager_MatchAggregate_Req::new();
        match_aggregate_req.query = String::from(query);
        req.set_match_aggregate_req(match_aggregate_req);
        query_manager_req(req)
    }

    pub(crate) fn match_group_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut match_group_req = QueryManager_MatchGroup_Req::new();
        match_group_req.query = String::from(query);
        req.set_match_group_req(match_group_req);
        query_manager_req(req)
    }

    pub(crate) fn match_group_aggregate_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut match_group_aggregate_req = QueryManager_MatchGroupAggregate_Req::new();
        match_group_aggregate_req.query = String::from(query);
        req.set_match_group_aggregate_req(match_group_aggregate_req);
        query_manager_req(req)
    }

    pub(crate) fn insert_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut insert_req = QueryManager_Insert_Req::new();
        insert_req.query = String::from(query);
        req.set_insert_req(insert_req);
        query_manager_req(req)
    }

    pub(crate) fn delete_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut delete_req = QueryManager_Delete_Req::new();
        delete_req.query = String::from(query);
        req.set_delete_req(delete_req);
        query_manager_req(req)
    }

    pub(crate) fn update_req(query: &str) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut update_req = QueryManager_Update_Req::new();
        update_req.query = String::from(query);
        req.set_update_req(update_req);
        query_manager_req(req)
    }

    pub(crate) fn explain_req(id: i64) -> Transaction_Req {
        let mut req = QueryManager_Req::new();
        let mut explain_req = QueryManager_Explain_Req::new();
        explain_req.explainable_id = id;
        req.set_explain_req(explain_req);
        query_manager_req(req)
    }
}
