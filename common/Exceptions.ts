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

import {RemoteConcept} from "../dependencies_internal";

export function CreateGraknError(error: string, additionalInfo = "") {
    return new Error(additionalInfo ? error + "/nAdditional information: " + additionalInfo : error)
}

export function ConnectionClosedError (additionalInformation = "") : Error {
    return CreateGraknError("The connection to the database is closed.", additionalInformation);
}

export function ExplanationNotPresentError (additionalInformation = "") : Error {
    return CreateGraknError("No explanation found.", additionalInformation);
}

export function UnknownBaseTypeError (concept: RemoteConcept, additionalInformation = "") : Error {
    return CreateGraknError("No known base type for concept " + concept.toString(), additionalInformation);
}

export function ResultNotPresentError (additionalInformation = "") : Error {
    return CreateGraknError("Result not present.", additionalInformation);
}