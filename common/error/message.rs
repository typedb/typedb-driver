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

pub(super) struct Template<'a> {
    code_prefix: &'a str,
    msg_prefix: &'a str,
}

impl Template<'_> {
    pub(super) const fn new<'a>(code_prefix: &'a str, msg_prefix: &'a str) -> Template<'a> {
        Template {
            code_prefix,
            msg_prefix
        }
    }
}

pub struct Message<'a> {
    code_prefix: &'a str,
    code_number: u8,
    msg_prefix: &'a str,
    msg_body: &'a str
}

impl Message<'_> {
    pub(super) const fn new<'a>(template: Template<'a>, code_number: u8, msg_body: &'a str) -> Message<'a> {
        Message {
            code_prefix: template.code_prefix,
            code_number,
            msg_prefix: template.msg_prefix,
            msg_body
        }
    }

    pub(crate) fn format(&self, args: Vec<&str>) -> String {
        let expected_arg_count = self.msg_body.matches("{}").count();
        assert_eq!(expected_arg_count, args.len());
        format!("[{}{:0>2}] {}: {}", self.code_prefix, self.code_number, self.msg_prefix, self.expand_msg(args))
    }

    fn expand_msg(&self, args: Vec<&str>) -> String {
        let arg_count = args.len();
        let msg_split_indexed = self.msg_body.split("{}").enumerate();
        let mut formatted_msg = String::new();
        for (idx, fragment) in msg_split_indexed {
            formatted_msg.push_str(fragment);
            if idx < arg_count { formatted_msg.push_str(args[idx]) }
        }
        formatted_msg
    }
}

impl From<Message<'_>> for String {
    fn from(msg: Message) -> Self {
        assert!(!msg.msg_body.contains("{}"));
        String::from(msg.msg_body)
    }
}
