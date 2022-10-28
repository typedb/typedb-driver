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
        use typedb_protocol::core_database_manager::{all, create, contains};

        pub(crate) fn contains_req(name: &str) -> contains::Req {
            contains::Req { name: name.into() }
        }

        pub(crate) fn create_req(name: &str) -> create::Req {
            create::Req { name: name.into() }
        }

        pub(crate) fn all_req() -> all::Req {
            all::Req {}
        }
    }

    pub(crate) mod database {
        use typedb_protocol::core_database::{delete, rule_schema, schema, type_schema};

        pub(crate) fn delete_req(name: &str) -> delete::Req {
            delete::Req { name: name.into() }
        }

        pub(crate) fn rule_schema_req(name: &str) -> rule_schema::Req {
            rule_schema::Req { name: name.into() }
        }

        pub(crate) fn schema_req(name: &str) -> schema::Req {
            schema::Req { name: name.into() }
        }

        pub(crate) fn type_schema_req(name: &str) -> type_schema::Req {
            type_schema::Req { name: name.into() }
        }
    }
}

// #[allow(dead_code)]
// pub(crate) mod cluster {
//     pub(crate) mod server_manager {
//         use typedb_protocol_backup::cluster_server::ServerManager_All_Req;
//
//         pub(crate) fn all_req() -> ServerManager_All_Req {
//             ServerManager_All_Req::new()
//         }
//     }
//
//     pub(crate) mod user_manager {
//         use typedb_protocol_backup::cluster_user::{ClusterUserManager_All_Req, ClusterUserManager_Contains_Req, ClusterUserManager_Create_Req};
//
//         pub(crate) fn contains_req(username: &str) -> ClusterUserManager_Contains_Req {
//             let mut req = ClusterUserManager_Contains_Req::new();
//             req.username = String::from(username);
//             req
//         }
//
//         pub(crate) fn create_req(username: &str, password: &str) -> ClusterUserManager_Create_Req {
//             let mut req = ClusterUserManager_Create_Req::new();
//             req.username = String::from(username);
//             req.password = String::from(password);
//             req
//         }
//
//         pub(crate) fn all_req() -> ClusterUserManager_All_Req {
//             ClusterUserManager_All_Req::new()
//         }
//     }
//
//     pub(crate) mod user {
//         use typedb_protocol_backup::cluster_user::{ClusterUser_Delete_Req, ClusterUser_Password_Req, ClusterUser_Token_Req};
//
//         pub(crate) fn password_req(username: &str, password: &str) -> ClusterUser_Password_Req {
//             let mut req = ClusterUser_Password_Req::new();
//             req.username = String::from(username);
//             req.password = String::from(password);
//             req
//         }
//
//         pub(crate) fn token_req(username: &str) -> ClusterUser_Token_Req {
//             let mut req = ClusterUser_Token_Req::new();
//             req.username = String::from(username);
//             req
//         }
//
//         pub(crate) fn delete_req(username: &str) -> ClusterUser_Delete_Req {
//             let mut req = ClusterUser_Delete_Req::new();
//             req.username = String::from(username);
//             req
//         }
//     }
//
//     pub(crate) mod database_manager {
//         use typedb_protocol_backup::cluster_database::{ClusterDatabaseManager_All_Req, ClusterDatabaseManager_Get_Req};
//
//         pub(crate) fn get_req(name: &str) -> ClusterDatabaseManager_Get_Req {
//             let mut req = ClusterDatabaseManager_Get_Req::new();
//             req.name = String::from(name);
//             req
//         }
//
//         pub(crate) fn all_req() -> ClusterDatabaseManager_All_Req {
//             ClusterDatabaseManager_All_Req::new()
//         }
//     }
// }

pub(crate) mod session {
    use typedb_protocol::{Options, session};
    use typedb_protocol::session::{close, open};

    pub(crate) fn close_req(session_id: Vec<u8>) -> close::Req {
        close::Req { session_id }
    }

    pub(crate) fn open_req(database: &str, session_type: session::Type, options: Option<Options>) -> open::Req {
        open::Req {
            database: database.into(),
            r#type: session_type.into(),
            options
        }
    }
}

pub(crate) mod transaction {
    use typedb_protocol::transaction;
    use typedb_protocol::transaction::{commit, open, rollback, stream};
    use uuid::Uuid;

    pub(crate) fn client_msg(reqs: Vec<transaction::Req>) -> transaction::Client {
        transaction::Client { reqs }
    }

    pub(crate) fn stream_req(req_id: Vec<u8>) -> transaction::Req {
        req_with_id(transaction::req::Req::StreamReq(stream::Req {}), req_id)
    }

    pub(crate) fn open_req(session_id: Vec<u8>, transaction_type: transaction::Type, network_latency_millis: i32) -> transaction::Req {
        req(transaction::req::Req::OpenReq(open::Req {
            session_id,
            r#type: transaction_type.into(),
            options: None,
            network_latency_millis
        }))
    }

    pub(crate) fn commit_req() -> transaction::Req {
        req(transaction::req::Req::CommitReq(commit::Req {}))
    }

    pub(crate) fn rollback_req() -> transaction::Req {
        req(transaction::req::Req::RollbackReq(rollback::Req {}))
    }

    pub(super) fn req(req: transaction::req::Req) -> transaction::Req {
        transaction::Req {
            req_id: Uuid::new_v4().as_bytes().to_vec(),
            metadata: Default::default(),
            req: req.into()
        }
    }

    pub(super) fn req_with_id(req: transaction::req::Req, req_id: Vec<u8>) -> transaction::Req {
        transaction::Req {
            req_id,
            metadata: Default::default(),
            req: req.into()
        }
    }
}

// #[allow(dead_code)]
// pub(crate) mod query_manager {
//     use typedb_protocol_backup::query::{QueryManager_Define_Req, QueryManager_Delete_Req, QueryManager_Explain_Req, QueryManager_Insert_Req, QueryManager_Match_Req, QueryManager_MatchAggregate_Req, QueryManager_MatchGroup_Req, QueryManager_MatchGroupAggregate_Req, QueryManager_Req, QueryManager_Undefine_Req, QueryManager_Update_Req};
//     use typedb_protocol_backup::transaction::Transaction_Req;
//
//     fn query_manager_req(req: QueryManager_Req) -> Transaction_Req {
//         let mut tx_req = Transaction_Req::new();
//         tx_req.set_query_manager_req(req);
//         tx_req
//     }
//
//     pub(crate) fn define_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut define_req = QueryManager_Define_Req::new();
//         define_req.query = String::from(query);
//         req.set_define_req(define_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn undefine_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut undefine_req = QueryManager_Undefine_Req::new();
//         undefine_req.query = String::from(query);
//         req.set_undefine_req(undefine_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn match_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut match_req = QueryManager_Match_Req::new();
//         match_req.query = String::from(query);
//         req.set_match_req(match_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn match_aggregate_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut match_aggregate_req = QueryManager_MatchAggregate_Req::new();
//         match_aggregate_req.query = String::from(query);
//         req.set_match_aggregate_req(match_aggregate_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn match_group_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut match_group_req = QueryManager_MatchGroup_Req::new();
//         match_group_req.query = String::from(query);
//         req.set_match_group_req(match_group_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn match_group_aggregate_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut match_group_aggregate_req = QueryManager_MatchGroupAggregate_Req::new();
//         match_group_aggregate_req.query = String::from(query);
//         req.set_match_group_aggregate_req(match_group_aggregate_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn insert_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut insert_req = QueryManager_Insert_Req::new();
//         insert_req.query = String::from(query);
//         req.set_insert_req(insert_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn delete_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut delete_req = QueryManager_Delete_Req::new();
//         delete_req.query = String::from(query);
//         req.set_delete_req(delete_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn update_req(query: &str) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut update_req = QueryManager_Update_Req::new();
//         update_req.query = String::from(query);
//         req.set_update_req(update_req);
//         query_manager_req(req)
//     }
//
//     pub(crate) fn explain_req(id: i64) -> Transaction_Req {
//         let mut req = QueryManager_Req::new();
//         let mut explain_req = QueryManager_Explain_Req::new();
//         explain_req.explainable_id = id;
//         req.set_explain_req(explain_req);
//         query_manager_req(req)
//     }
// }
//
// #[allow(dead_code)]
// pub(crate) mod thing {
//     use typedb_protocol_backup::concept::{Attribute_GetOwners_Req, Thing_Req};
//     use typedb_protocol_backup::transaction::Transaction_Req;
//
//     fn thing_req(req: Thing_Req) -> Transaction_Req {
//         let mut tx_req = Transaction_Req::new();
//         tx_req.set_thing_req(req);
//         tx_req
//     }
//
//     pub(crate) fn attribute_get_owners_req(iid: &Vec<u8>) -> Transaction_Req {
//         let mut req = Thing_Req::new();
//         req.iid = iid.clone();
//         req.set_attribute_get_owners_req(Attribute_GetOwners_Req::new());
//         thing_req(req)
//     }
// }
