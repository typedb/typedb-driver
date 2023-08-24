# TypeDB Client for Python

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
