# TypeDB HTTP Typescript Driver

## Driver Architecture

To learn about the mechanism that TypeDB drivers use set up communication with databases running on the TypeDB Server,
refer to the [Drivers Overview](https://typedb.com/docs/drivers/overview).

## API Reference

To learn about the methods available for executing queries and retrieving their answers using Rust, refer to
the [API Reference](https://typedb.com/docs/drivers/http/api-reference).

## Install TypeDB HTTP Typescript Driver through NPM

1. Install `typedb-http-driver` through npm:

```bash
npm install typedb-http-driver
```

2. Make sure the [TypeDB Server](https://docs.typedb.com/docs/running-typedb/install-and-run#start-the-typedb-server) is
   running.
3. Use TypeDB Driver in your program:

```ts
import { TypeDBHttpDriver, isApiErrorResponse } from "typedb-http-driver";

const driver = new TypeDBHttpDriver({
    username: "admin",
    password: "password",
    addresses: [ "localhost:1729" ],
});

const readTx = await driver.openTransaction("database-name", "read");
if (isApiErrorResponse(readTx)) throw readTx;

const readAnswer = await driver.query(newReadTx.ok.transactionId, "match entity $x;");
if (isApiErrorResponse(readAnswer)) throw readAnswer;

if (readAnswer.ok.answerType === "conceptRows") {
    readAnswer.ok.answers.forEach((row) => {
        console.log(row.data)
    })
}
```
