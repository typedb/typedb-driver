# TypeDB HTTP Typescript Driver

## Driver Architecture

To learn about how the TypeDB HTTP driver communicates with the TypeDB Server,
refer to the [HTTP API Reference](https://typedb.com/docs/reference/http-api).

## API Reference

To learn about the methods available for executing queries and retrieving their answers using Typescript, refer to
the [API Reference](https://typedb.com/docs/reference/http-drivers/typescript).

## Install TypeDB HTTP Typescript Driver through NPM

1. Install `@typedb/driver-http` through npm:

```bash
npm install @typedb/driver-http
```

2. Make sure a [TypeDB Server](https://typedb.com/docs/home/install/) is
   running.
3. Use TypeDB Driver in your program:

```ts
import { TypeDBHttpDriver, isApiErrorResponse } from "@typedb/driver-http";

const driver = new TypeDBHttpDriver({
    username: "admin",
    password: "password",
    addresses: [ "localhost:1729" ],
});

const transactionResponse = await driver.openTransaction("database-name", "read");
if (isApiErrorResponse(transactionResponse)) throw transactionResponse.err;
const transactionId = transactionResponse.ok.transactionId;

const answerResponse = await driver.query(transactionId, "match entity $x;");
if (isApiErrorResponse(answerResponse)) throw answerResponse.err;
const answer = answerResponse.ok;

if (answer.answerType === "conceptRows") {
   answer.answers.forEach((row) => {
        console.log(row.data)
    })
}
```

## Build queries with the `typeql` template tag

`typeql` builds query text from code only, so values can't be injected into it: an interpolated value must itself be a
fragment built by `typeql`, `TypeQL.label()`, `TypeQL.variable()` or `TypeQL.join()`, and anything else throws. Values
are passed to the query as given rows.

```ts
import { typeql, TypeQL } from "@typedb/driver-http";

const attribute = TypeQL.label(attributeName); // throws unless attributeName is a valid TypeQL label
const query = typeql`given $value: string; match $x isa person, has ${attribute} == $value;`;

const answerResponse = await driver.query(transactionId, query, undefined, [{ value: userInput }]);
```
