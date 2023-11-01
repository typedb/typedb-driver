/*
 * Copyright (C) 2022 Vaticle
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

/**
  * A <code>Label</code> holds the uniquely identifying name of a type.
  *
  * It consists of an optional 'scope', and a 'name', represented "scope:name".
  * The scope is used only used to distinguish between role-types of the same name declared in different relation types.
  */
export class Label {
    private readonly _scope: string;
    private readonly _name: string;

    /** @ignore */
    constructor(scope: string, label: string) {
        this._scope = scope;
        this._name = label;
    }

    /** Returns the (possibly null) scope part of the label. */
    get scope(): string {
        return this._scope;
    }

    /** Returns the name part of the label. */
    get name(): string {
        return this._name;
    }

    /** Returns the string representation of the scoped name. */
    get scopedName(): string {
        return this._scope == null ? this._name : `${this._scope}:${this._name}`;
    }

    /** Printable string */
    toString() {
        return this.scopedName;
    }

    /** Compares this label to <code>that</code> label.
     * @param that - The label to compare to.
     */
    equals(that: Label) {
        return this._scope === that._scope && this._name === that._name;
    }
}

export namespace Label {

    /** Creates an unscoped scoped label from the name
     * @param name - The type name.
     */
    export function of(name: string) {
        return new Label(null, name);
    }

    /** Creates an unscoped scoped label from the name
     * @param scope - The scope part of the type name.
     * @param name - The type name.
     */
    export function scoped(scope: string, name: string) {
        return new Label(scope, name);
    }
}
