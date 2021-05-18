# TypeDB Client for Node.js

[![Grabl](https://grabl.io/api/status/vaticle/typedb-client-nodejs/badge.svg)](https://grabl.io/vaticle/typedb-client-nodejs)
[![Discord](https://img.shields.io/discord/665254494820368395?color=7389D8&label=chat&logo=discord&logoColor=ffffff)](https://vaticle.com/discord)
[![Discussion Forum](https://img.shields.io/discourse/https/forum.vaticle.com/topics.svg)](https://forum.vaticle.com)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typedb-796de3.svg)](https://stackoverflow.com/questions/tagged/typedb)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typeql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/typeql)

## Client Architecture
To learn about the mechanism that a TypeDB Client uses to set up communication with databases running on the TypeDB Server, refer to [TypeDB > Client API > Overview](http://docs.vaticle.com/docs/client-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Client Node.js, refer to [TypeDB > Client API > Node.js > API Reference](http://docs.vaticle.com/docs/client-api/nodejs#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to TypeQL queries, refer to [TypeDB > Concept API > Overview](http://docs.vaticle.com/docs/concept-api/overview).

## Import TypeDB Client for Node.js through `npm`

```shell script
npm install typedb-client
```
Further documentation: https://docs.vaticle.com/docs/client-api/nodejs

## Build TypeDB Client for Node.js from Source

> Note: You don't need to compile TypeDB Client from source if you just want to use it in your code. See the _"Import TypeDB Client for Node.js"_ section above.

1. Make sure you have [Node.js](https://nodejs.org/) (version 14 or above) and its `npm` package manager installed on your machine
1. Clone the project and run `npm install` at its root directory (containing `package.json`)
1. Run `npm run build`
1. The JavaScript files, and their matching TypeScript type definitions are compiled to the `dist` directory.

> Note: TypeScript is not required in order to run TypeDB Client, however its type assertions may make development smoother.
