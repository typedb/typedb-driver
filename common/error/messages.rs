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

use crate::common::error::message::{Message, Template};

struct Templates<'a> {
    client: Template<'a>,
    concept: Template<'a>
}

impl Templates<'_> {
    const fn new() -> Templates<'static> {
        Templates {
            client: Template::new("CLI", "Client Error"),
            concept: Template::new("CON", "Concept Error")
        }
    }
}

const TEMPLATES: Templates = Templates::new();

pub struct ClientMessages<'a> {
    pub transaction_closed_with_errors: Message<'a>,
    pub unable_to_connect: Message<'a>,
    pub cluster_replica_not_primary: Message<'a>,
    pub cluster_token_credential_invalid: Message<'a>,
}

pub struct ConceptMessages<'a> {
    pub invalid_concept_casting: Message<'a>,
}

pub struct Messages<'a> {
    pub client: ClientMessages<'a>,
    pub concept: ConceptMessages<'a>,
}

impl Messages<'_> {
    const fn new() -> Messages<'static> {
        Messages {
            client: ClientMessages {
                transaction_closed_with_errors: Message::new(TEMPLATES.client, 4, "The transaction has been closed with error(s): \n{}"),
                unable_to_connect: Message::new(TEMPLATES.client, 5, "Unable to connect to TypeDB server."),
                cluster_replica_not_primary: Message::new(TEMPLATES.client, 13, "The replica is not the primary replica."),
                cluster_token_credential_invalid: Message::new(TEMPLATES.client, 16, "Invalid token credential."),
            },
            concept: ConceptMessages {
                invalid_concept_casting: Message::new(TEMPLATES.concept, 1, "Invalid concept conversion from '{}' to '{}'"),
            }
        }
    }
}

pub const ERRORS: Messages = Messages::new();
