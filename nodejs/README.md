# TypeDB Driver for Node.js

[![Factory](https://factory.vaticle.com/api/status/vaticle/typedb-driver-nodejs/badge.svg)](https://factory.vaticle.com/vaticle/typedb-driver-nodejs)
[![Discord](https://img.shields.io/discord/665254494820368395?color=7389D8&label=chat&logo=discord&logoColor=ffffff)](https://vaticle.com/discord)
[![Discussion Forum](https://img.shields.io/discourse/https/forum.vaticle.com/topics.svg)](https://forum.vaticle.com)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typedb-796de3.svg)](https://stackoverflow.com/questions/tagged/typedb)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typeql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/typeql)

## Driver Architecture
To learn about the mechanism that a TypeDB Driver uses to set up communication with databases running on the TypeDB Server, refer to [TypeDB > Driver API > Overview](http://docs.vaticle.com/docs/driver-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Driver Node.js, refer to [TypeDB > Driver API > Node.js > API Reference](http://docs.vaticle.com/docs/driver-api/nodejs#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to TypeQL queries, refer to [TypeDB > Concept API > Overview](http://docs.vaticle.com/docs/concept-api/overview).

## Installation

[Node.js](https://nodejs.org/) version 14 or above is recommended.

```shell script
npm install typedb-driver
```
Further documentation: https://docs.vaticle.com/docs/driver-api/nodejs

## Using TypeScript
`typedb-driver` is a TypeScript project and provides its own type definitions out of the box - for example:

```ts
import { EntityType } from 'typedb-driver';
```

## Build TypeDB Driver for Node.js from Source

> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"Import TypeDB Driver for Node.js"_ section above.
>
> We recommend using `yarn`, see https://yarnpkg.com/ for information about `yarn`.

1. Make sure you have the following installed: Node.js (version 14 or above); `npm` package manager; `yarn` package manager.
1. Clone the project and run `yarn` at the root directory (containing `package.json`)
1. Run `yarn run build`
1. The JavaScript files, and their matching TypeScript type definitions are compiled to the `dist` directory.

> Note: TypeDB Driver can be run without TypeScript, however its type assertions may make development smoother.
