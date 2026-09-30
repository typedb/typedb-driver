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

/**
 * Parses JSON, returning a `bigint` for any integer a `number` cannot hold
 * exactly. Otherwise behaves as `JSON.parse`, which reads every number as a
 * double and so rounds integers beyond 2^53.
 *
 * The type of an integer therefore depends on its magnitude: a `number` below
 * 2^53 and a `bigint` from there up, so compare with `==` rather than `===` if
 * either is possible.
 */
export function parseJson(text: string): any {
    if (!UNSAFE_INTEGER_CANDIDATE.test(text)) return JSON.parse(text);
    const parser = new JsonParser(text);
    const value = parser.parseValue();
    parser.skipWhitespace();
    if (!parser.atEnd()) parser.fail("Unexpected trailing content");
    return value;
}

/**
 * 2^53 - 1 has 16 digits, so fewer than that is always exact. A number can only
 * follow a structural character or begin the text, which keeps IIDs and other
 * long digit runs inside strings on the fast path.
 */
const UNSAFE_INTEGER_CANDIDATE = /(^|[\[,:\s-])\d{16}/;

const JSON_NUMBER = /^-?(0|[1-9]\d*)(\.\d+)?([eE][+-]?\d+)?$/;

/**
 * Serializes to JSON, writing a `bigint` as a number. `JSON.stringify` throws on
 * one, so a value taken from an answer could not otherwise be sent back.
 */
export function stringifyJson(value: unknown): string {
    const out: string[] = [];
    writeValue(value, out);
    return out.join("");
}

function writeValue(value: unknown, out: string[]): void {
    if (value !== null && typeof value === "object" && typeof (value as any).toJSON === "function") {
        value = (value as any).toJSON();
    }
    if (value === null) {
        out.push("null");
    } else if (typeof value === "bigint") {
        out.push(value.toString());
    } else if (typeof value === "number") {
        out.push(Number.isFinite(value) ? JSON.stringify(value) : "null");
    } else if (typeof value === "boolean" || typeof value === "string") {
        out.push(JSON.stringify(value));
    } else if (Array.isArray(value)) {
        out.push("[");
        value.forEach((item, i) => {
            if (i > 0) out.push(",");
            if (item === undefined || typeof item === "function" || typeof item === "symbol") out.push("null");
            else writeValue(item, out);
        });
        out.push("]");
    } else if (typeof value === "object") {
        out.push("{");
        let first = true;
        for (const [key, member] of Object.entries(value as Record<string, unknown>)) {
            if (member === undefined || typeof member === "function" || typeof member === "symbol") continue;
            if (!first) out.push(",");
            first = false;
            out.push(JSON.stringify(key), ":");
            writeValue(member, out);
        }
        out.push("}");
    } else {
        // undefined, function and symbol are not representable
        out.push("null");
    }
}

const ESCAPES: Record<string, string> = {
    '"': '"', "\\": "\\", "/": "/", b: "\b", f: "\f", n: "\n", r: "\r", t: "\t",
};

class JsonParser {
    private readonly text: string;
    private at = 0;

    constructor(text: string) {
        this.text = text;
    }

    atEnd(): boolean {
        return this.at >= this.text.length;
    }

    fail(message: string): never {
        throw new SyntaxError(`${message} at position ${this.at}`);
    }

    skipWhitespace(): void {
        while (this.at < this.text.length) {
            const c = this.text[this.at];
            if (c === " " || c === "\n" || c === "\t" || c === "\r") this.at++;
            else break;
        }
    }

    parseValue(): any {
        this.skipWhitespace();
        if (this.atEnd()) this.fail("Unexpected end of input");
        const c = this.text[this.at];
        switch (c) {
            case "{": return this.parseObject();
            case "[": return this.parseArray();
            case '"': return this.parseString();
            case "t": return this.parseLiteral("true", true);
            case "f": return this.parseLiteral("false", false);
            case "n": return this.parseLiteral("null", null);
            default: return this.parseNumber();
        }
    }

    private parseLiteral<T>(word: string, value: T): T {
        if (this.text.startsWith(word, this.at)) {
            this.at += word.length;
            return value;
        }
        this.fail(`Expected ${word}`);
    }

    private parseObject(): Record<string, any> {
        this.at++; // {
        const result: Record<string, any> = {};
        this.skipWhitespace();
        if (this.text[this.at] === "}") {
            this.at++;
            return result;
        }
        for (;;) {
            this.skipWhitespace();
            if (this.text[this.at] !== '"') this.fail("Expected a property name");
            const key = this.parseString();
            this.skipWhitespace();
            if (this.text[this.at] !== ":") this.fail("Expected ':'");
            this.at++;
            // Plain assignment of "__proto__" would go through the setter rather
            // than create an own property, which is not what JSON.parse does.
            Object.defineProperty(result, key, {
                value: this.parseValue(),
                writable: true,
                enumerable: true,
                configurable: true,
            });
            this.skipWhitespace();
            const c = this.text[this.at];
            if (c === ",") {
                this.at++;
                continue;
            }
            if (c === "}") {
                this.at++;
                return result;
            }
            this.fail("Expected ',' or '}'");
        }
    }

    private parseArray(): any[] {
        this.at++; // [
        const result: any[] = [];
        this.skipWhitespace();
        if (this.text[this.at] === "]") {
            this.at++;
            return result;
        }
        for (;;) {
            result.push(this.parseValue());
            this.skipWhitespace();
            const c = this.text[this.at];
            if (c === ",") {
                this.at++;
                continue;
            }
            if (c === "]") {
                this.at++;
                return result;
            }
            this.fail("Expected ',' or ']'");
        }
    }

    private parseString(): string {
        this.at++; // opening quote
        let out = "";
        let chunkStart = this.at;
        for (;;) {
            if (this.atEnd()) this.fail("Unterminated string");
            const c = this.text[this.at];
            if (c === '"') {
                out += this.text.slice(chunkStart, this.at);
                this.at++;
                return out;
            }
            if (c === "\\") {
                out += this.text.slice(chunkStart, this.at);
                this.at++;
                const escape = this.text[this.at];
                if (escape === "u") {
                    const hex = this.text.slice(this.at + 1, this.at + 5);
                    if (!/^[0-9a-fA-F]{4}$/.test(hex)) this.fail("Invalid unicode escape");
                    out += String.fromCharCode(parseInt(hex, 16));
                    this.at += 5;
                } else {
                    const replacement = ESCAPES[escape];
                    if (replacement === undefined) this.fail("Invalid escape sequence");
                    out += replacement;
                    this.at++;
                }
                chunkStart = this.at;
                continue;
            }
            this.at++;
        }
    }

    private parseNumber(): number | bigint {
        const start = this.at;
        if (this.text[this.at] === "-") this.at++;
        while (isDigit(this.text[this.at])) this.at++;

        let isInteger = true;
        if (this.text[this.at] === ".") {
            isInteger = false;
            this.at++;
            while (isDigit(this.text[this.at])) this.at++;
        }
        if (this.text[this.at] === "e" || this.text[this.at] === "E") {
            isInteger = false;
            this.at++;
            if (this.text[this.at] === "+" || this.text[this.at] === "-") this.at++;
            while (isDigit(this.text[this.at])) this.at++;
        }

        const literal = this.text.slice(start, this.at);
        if (!JSON_NUMBER.test(literal)) this.fail(`Invalid number '${literal}'`);
        const value = Number(literal);
        if (isInteger && !Number.isSafeInteger(value)) return BigInt(literal);
        return value;
    }
}

function isDigit(c: string | undefined): boolean {
    return c !== undefined && c >= "0" && c <= "9";
}
