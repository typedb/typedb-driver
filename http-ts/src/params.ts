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

export interface DriverParamsBasic {
    username: string;
    password: string;
    addresses: string[];
}

export interface DriverParamsTranslated {
    username: string;
    password: string;
    translatedAddresses: TranslatedAddress[];
}

export interface TranslatedAddress {
    external: string;
    internal: string;
}

export type DriverParams = DriverParamsBasic | DriverParamsTranslated;

export function isBasicParams(params: DriverParams): params is DriverParamsBasic {
    return `addresses` in params;
}

export function isTranslatedParams(params: DriverParams): params is DriverParamsTranslated {
    return `translatedAddresses` in params;
}

export function remoteOrigin(params: DriverParams) {
    if (isBasicParams(params)) return `${params.addresses[0]}`;
    else return `${params.translatedAddresses[0].external}`;
}

/** Extract host:port from a URL string. "https://127.0.0.1:18000" → "127.0.0.1:18000" */
export function hostPortFromOrigin(origin: string): string {
    try {
        const url = new URL(origin);
        return url.host;
    } catch {
        return origin;
    }
}

/** Resolve a bare primaryAddress (host:port) to a full URL using configured params. */
export function resolveOrigin(params: DriverParams, primaryAddress: string): string {
    if (isBasicParams(params)) {
        for (const addr of params.addresses) {
            if (hostPortFromOrigin(addr) === primaryAddress) return addr;
        }
        return deriveOriginWithScheme(params.addresses[0], primaryAddress);
    } else {
        for (const ta of params.translatedAddresses) {
            if (hostPortFromOrigin(ta.internal) === primaryAddress
                || hostPortFromOrigin(ta.external) === primaryAddress) {
                return ta.external;
            }
        }
        return deriveOriginWithScheme(params.translatedAddresses[0].external, primaryAddress);
    }
}

function deriveOriginWithScheme(referenceUrl: string, hostPort: string): string {
    try {
        const url = new URL(referenceUrl);
        return `${url.protocol}//${hostPort}`;
    } catch {
        return `https://${hostPort}`;
    }
}

/** Return all configured origins for connection error fallback. */
export function allOrigins(params: DriverParams): string[] {
    if (isBasicParams(params)) return params.addresses;
    else return params.translatedAddresses.map(ta => ta.external);
}
