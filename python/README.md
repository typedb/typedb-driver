# TypeDB Driver for Python

## Driver Architecture
To learn about the mechanism that a TypeDB Driver uses to set up communication with databases running on the TypeDB Server, refer to the [Driver Overview](https://typedb.com/docs/clients/2.x/clients).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Driver Python, refer to the [API Reference](https://typedb.com/docs/clients/2.x/python/python-api-ref).

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
