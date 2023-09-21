# TypeDB Driver for Python

## Driver Architecture
To learn about the mechanism that a TypeDB Driver uses to set up communication with databases running on the TypeDB Server, refer to [TypeDB > Driver API > Overview](http://docs.vaticle.com/docs/driver-api/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Driver Python, refer to [TypeDB > Driver API > Python > API Reference](http://docs.vaticle.com/docs/driver-api/python#api-reference).

## Concept API
To learn about the methods available on the concepts retrieved as the answers to TypeQL queries, refer to [TypeDB > Concept API > Overview](http://docs.vaticle.com/docs/concept-api/overview)

## Install TypeDB Driver for Python through Pip
```
pip install typedb-driver
```
If multiple Python versions are available, you may wish to use
```
pip3 install typedb-driver
```

In your python program, import from typedb.driver:
```py
from typedb.driver import *

driver = TypeDB.core_driver(address=TypeDB.DEFAULT_ADDRESS)
```
