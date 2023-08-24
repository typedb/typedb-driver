# TypeDB Client for Python

[![Factory](https://factory.vaticle.com/api/status/vaticle/typedb-client-python/badge.svg)](https://factory.vaticle.com/vaticle/typedb-client-python)
[![GitHub release](https://img.shields.io/github/release/vaticle/typedb-client-python.svg)](https://github.com/vaticle/typedb/releases/latest)
[![Discord](https://img.shields.io/discord/665254494820368395?color=7389D8&label=chat&logo=discord&logoColor=ffffff)](https://vaticle.com/discord)
[![Discussion Forum](https://img.shields.io/discourse/https/forum.vaticle.com/topics.svg)](https://forum.vaticle.com)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typedb-796de3.svg)](https://stackoverflow.com/questions/tagged/typedb)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typeql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/typeql)

## Client Architecture
To learn about the mechanism that a TypeDB Client uses to set up communication with databases running on the TypeDB Server, refer to [TypeDB > Client API > Overview](http://docs.vaticle.com/docs/client-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Client Python, refer to [TypeDB > Client API > Python > API Reference](http://docs.vaticle.com/docs/client-api/python#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to TypeQL queries, refer to [TypeDB > Concept API > Overview](http://docs.vaticle.com/docs/concept-api/overview)

## Install TypeDB Client for Python through Pip
```
pip install typedb-client
```
If multiple Python versions are available, you may wish to use
```
pip3 install typedb-client
```

In your python program, import from typedb.client:
```py
from typedb.client import *

client = TypeDB.core_client(address=TypeDB.DEFAULT_ADDRESS)
```
