PyPI package: https://pypi.org/project/typedb-client
Documentation: https://docs.vaticle.com/docs/client-api/python

## Distribution

Available through https://pypi.org

```
pip install typedb-client=={version}
```


## New Features


## Bugs Fixed
- **Safer cluster channel initialisation & better handling of invalid token errors**
  When using CA in the authorisation flow, we fix a bug where the client tries to access client state that may not exist yet.
  
  
## Code Refactors


## Other Improvements
- **Update VERSION to 2.18.2**

- **Bump TypeDB artifacts to 2.19.0**
