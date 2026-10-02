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

const construction = Symbol("TypeQL");

/**
 * A piece of TypeQL query text that can only come from code: the literal parts of a <code>typeql</code> template,
 * labels checked by <code>TypeQL.label()</code>, variables checked by <code>TypeQL.variable()</code>, and other
 * fragments built the same way. Values are never part of it: they are passed to the query as given rows.
 */
export class TypeQL {
    /** The query text of this fragment. */
    readonly text: string;

    /** @hidden */
    constructor(text: string, key: symbol) {
        if (key !== construction) {
            throw new TypeError("TypeQL fragments are built with typeql, TypeQL.label(), TypeQL.variable() or TypeQL.join()");
        }
        this.text = text;
    }

    /**
     * Makes a fragment from a type label (<code>person</code>), a scoped role label (<code>friendship:friend</code>),
     * or the name of a function, struct or struct field. Throws a <code>TypeError</code> if <code>name</code> isn't
     * a valid TypeQL identifier, or is a reserved keyword.
     *
     * @param name - The label
     *
     * @example
     * ```ts
     * typeql`match $x isa person, has ${TypeQL.label(attributeName)} $value;`
     * ```
     */
    static label(name: string): TypeQL {
        const parts = typeof name === "string" ? name.split(":") : [];
        if (parts.length < 1 || parts.length > 2 || !parts.every(isLabelIdentifier)) {
            throw new TypeError(`Not a TypeQL label: ${JSON.stringify(name)}`);
        }
        return new TypeQL(name, construction);
    }

    /**
     * Makes a variable fragment, <code>$name</code>, from a variable name given without its <code>$</code>. Throws a
     * <code>TypeError</code> if <code>name</code> isn't a valid TypeQL variable name.
     *
     * @param name - The variable name, without its <code>$</code>
     *
     * @example
     * ```ts
     * typeql`match ${TypeQL.variable(variableName)} isa person;`
     * ```
     */
    static variable(name: string): TypeQL {
        if (typeof name !== "string" || !VARIABLE_NAME.test(name)) {
            throw new TypeError(`Not a TypeQL variable name: ${JSON.stringify(name)}`);
        }
        return new TypeQL(`$${name}`, construction);
    }

    /**
     * Joins fragments with a separator that is itself a fragment, e.g. to assemble a list of fetch entries.
     *
     * @param fragments - The fragments to join
     * @param separator - The fragment put between each of them
     *
     * @example
     * ```ts
     * TypeQL.join([typeql`"name": $x.name`, typeql`"age": $x.age`], typeql`, `)
     * ```
     */
    static join(fragments: TypeQL[], separator: TypeQL): TypeQL {
        const texts = fragments.map(fragment => requireFragment(fragment).text);
        return new TypeQL(texts.join(requireFragment(separator).text), construction);
    }
}

/**
 * Builds a <code>TypeQL</code> fragment from a template literal. Its literal parts are kept as is, and each
 * interpolated value must itself be a <code>TypeQL</code> fragment: anything else, such as a string or a number,
 * throws a <code>TypeError</code>. Values go into given rows instead.
 *
 * @example
 * ```ts
 * const query = typeql`given $name: string; match $x isa person, has name == $name;`;
 * await driver.query(transactionId, query.text, undefined, [{ name: userInput }]);
 * ```
 */
export function typeql(strings: TemplateStringsArray, ...fragments: TypeQL[]): TypeQL {
    if (!isTemplateStrings(strings)) throw new TypeError("typeql is a template tag: call it as typeql`...`");
    let text = strings[0];
    fragments.forEach((fragment, index) => {
        text += requireFragment(fragment).text + strings[index + 1];
    });
    return new TypeQL(text, construction);
}

const LABEL_IDENTIFIER = /^[_\p{XID_Start}][-\p{XID_Continue}]*$/u;
const VARIABLE_NAME = /^[0-9\p{XID_Start}][-\p{XID_Continue}]*$/u;

const RESERVED_KEYWORDS = new Set([
    "with", "given", "match", "fetch", "update", "define", "undefine", "redefine", "insert", "put", "delete", "end",
    "entity", "relation", "attribute", "role",
    "asc", "desc",
    "struct", "fun", "return",
    "alias", "sub", "owns", "as", "plays", "relates",
    "iid", "isa", "links", "has",
    "is", "or", "not", "try", "in",
    "true", "false",
    "of", "from",
    "first", "last",
]);

function isLabelIdentifier(part: string): boolean {
    return LABEL_IDENTIFIER.test(part) && !RESERVED_KEYWORDS.has(part);
}

function isTemplateStrings(strings: unknown): strings is TemplateStringsArray {
    return Array.isArray(strings) && Object.isFrozen(strings) && Array.isArray((strings as unknown as TemplateStringsArray).raw);
}

function requireFragment(value: unknown): TypeQL {
    if (!(value instanceof TypeQL)) {
        throw new TypeError(`typeql only interpolates TypeQL fragments, not ${value === null ? "null" : `a ${typeof value}`}: pass values as given rows`);
    }
    return value;
}
