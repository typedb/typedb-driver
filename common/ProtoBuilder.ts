/*
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

import { GraknOptions } from "../dependencies_internal";
import options_pb from "graknlabs-protocol/protobuf/options_pb";
import Options = options_pb.Options;

export namespace ProtoBuilder {
    export function options(options?: GraknOptions): Options {
        const optionsProto = new Options();
        if (options) {
            if (options.infer() != null) optionsProto.setInfer(options.infer() as boolean);
            if (options.explain() != null) optionsProto.setExplain(options.explain() as boolean);
            if (options.batchSize() != null) optionsProto.setBatchSize(options.batchSize() as number);
        }
        return optionsProto;
    }
}
