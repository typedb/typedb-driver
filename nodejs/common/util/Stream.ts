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

import {TypeDBDriverError} from "../errors/TypeDBDriverError";

/**
 * A stream of elements offering a functional interface to manipulate elements.
 * Typically the elements are generated/retrieved lazily.
 */
export abstract class Stream<T> implements AsyncIterable<T> {

    // TODO why can't this be abstract?
    // eslint-disable-next-line require-yield
    async* [Symbol.asyncIterator](): AsyncIterator<T, any, undefined> {
        throw new TypeDBDriverError("ILLEGAL STATE");
    }

    // TODO: TODO DOCS: Add docs
    iterator(): AsyncIterator<T, any, undefined> {
        return this[Symbol.asyncIterator]();
    }

    /**
     * Collects all the answers from this stream into an array
     * ### Examples
     *
     * ```ts
     * results = transaction.query.match(query).collect().await;
     * ```
     */
    async collect(): Promise<T[]> {
        const answers: T[] = [];
        for await (const answer of this) {
            answers.push(answer);
        }
        return answers;
    }

    /**
     * Checks whether a condition is satisfied by ALL answers in this stream.
     * @param callbackFn: The condition to evaluate.
     * ### Examples
     *
     * ```ts
     * // For a query "match $a isa age;", check if all results for $a are positive.
     * results = transaction.query.match(query).every(cm => cm.get("a").value > 0).await;
     * ```
     */
    async every(callbackFn: (value: T) => unknown): Promise<boolean> {
        for await (const item of this) {
            if (!callbackFn(item)) return false;
        }
        return true;
    }

    /**
     * Checks whether a condition is satisfied by ANY answer in this stream.
     * @param callbackFn: The condition to evaluate.
     * ### Examples
     *
     * ```ts
     * // For a query "match $a isa age;", check if any results for $a are negative.
     * results = transaction.query.match(query).some(cm => cm.get("a").value < 0).await;
     * ```
     */
    async some(callbackFn: (value: T) => unknown): Promise<boolean> {
        for await (const item of this) {
            if (callbackFn(item)) return true;
        }
        return false;
    }

    /**
     * Returns a new stream from this stream consisting only of elements which satisfy a given condition.
     * @param filter - The condition to evaluate.
     * ### Examples
     *
     * ```ts
     * // For a query "match $p isa person, has age $a;", only retrieve results having $a >= 60.
     * results = transaction.query.match(query).filter(cm => cm.get("a").value > 60).collect();
     * ```
     */
    filter(filter: (value: T) => boolean): Stream<T> {
        return new Stream.Filtered<T>(this, filter);
    }

    /**
     * @param mapper - The mapping function to apply.
     * Returns a new stream from this stream by applying the <code>mapper</code> function to each element.
     */
    map<U>(mapper: (value: T) => U): Stream<U> {
        return new Stream.Mapped<T, U>(this, mapper);
    }

    /**
     * Given a function which accepts a single element of this stream and returns a new stream,
     * This function returns a new stream obtained by applying the function to each element in the stream,
     * and concatenating each result thus obtained
     * @param mapper - The mapping function to apply. Must return a stream.
     */
    flatMap<U>(mapper: (value: T) => Stream<U>): Stream<U> {
        return new Stream.FlatMapped<T, U, Stream<U>>(this, mapper);
    }

    /**
     * Executes the given function for each element in the stream.
     * @param fn - The function to evaluate for each element.
     */
    async forEach(fn: (value: T) => void): Promise<void> {
        for await (const val of this) {
            fn(val);
        }
    }

    /**
     * Returns the first element in the stream.
     */
    async first(): Promise<T | null> {
        for await (const val of this) {
            return val;
        }
        return null;
    }
}

/** @ignore */
export namespace Stream {

    export function iterable<T>(iterable: AsyncIterable<T>) {
        return new Iterable<T>(iterable);
    }

    export function promises<T>(items: Promise<T>[]) {
        return new PromiseArray<T>(items);
    }

    export function array<T>(items: T[]) {
        return new Array<T>(items);
    }

    class Iterable<T> extends Stream<T> {

        private _provider: AsyncIterable<T>;

        public constructor(provider: AsyncIterable<T>) {
            super();
            this._provider = provider;
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async* [Symbol.asyncIterator](): AsyncIterator<T, any, undefined> {
            for await (const val of this._provider) {
                yield val;
            }
        }
    }

    class Array<T> extends Stream<T> {
        private readonly _array: T[];

        public constructor(array: T[]) {
            super();
            this._array = array;
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async* [Symbol.asyncIterator](): AsyncIterator<T, any, undefined> {
            for await (const val of this._array) {
                yield val;
            }
        }
    }

    class PromiseArray<T> extends Stream<T> {
        private readonly _array: Promise<T>[];

        public constructor(array: Promise<T>[]) {
            super();
            this._array = array;
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async* [Symbol.asyncIterator](): AsyncIterator<T, any, undefined> {
            for await (const val of this._array) {
                yield val;
            }
        }
    }

    export class Filtered<T> extends Stream<T> {

        private _filter: (value: T) => boolean;
        private _source: Stream<T>;

        constructor(source: Stream<T>, filter: (value: T) => boolean) {
            super();
            this._source = source;
            this._filter = filter;

        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async* [Symbol.asyncIterator](): AsyncIterator<T, any, undefined> {
            for await (const val of this._source) {
                if (this._filter(val)) yield val;
            }
        }
    }

    export class Mapped<T, U> extends Stream<U> {

        private _source: Stream<T>;
        private _mapper: (value: T) => U;

        constructor(source: Stream<T>, mapper: (value: T) => U) {
            super();
            this._source = source;
            this._mapper = mapper;
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async* [Symbol.asyncIterator](): AsyncIterator<U, any, undefined> {
            for await (const val of this._source) {
                yield this._mapper(val);
            }
        }
    }

    export class FlatMapped<T, U, S extends Stream<U>> extends Stream<U> {
        private _source: Stream<T>;
        private _flatMapper: (value: T) => Stream<U>;

        constructor(source: Stream<T>, flatMapper: (value: T) => Stream<U>) {
            super();
            this._source = source;
            this._flatMapper = flatMapper;
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        async* [Symbol.asyncIterator](): AsyncIterator<U, any, undefined> {
            for await (const val of this._source) {
                for await (const flatMapped of this._flatMapper(val)) {
                    yield flatMapped;
                }
            }
        }
    }

}
