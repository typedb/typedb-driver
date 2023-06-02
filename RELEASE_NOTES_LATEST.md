Documentation: http://docs.vaticle.com/docs/client-api/java

## Distribution

Available through https://repo.vaticle.com

```xml
<repositories>
    <repository>
        <id>repo.vaticle.com</id>
        <url>https://repo.vaticle.com/repository/maven/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupid>com.vaticle.typedb</groupid>
        <artifactid>typedb-client</artifactid>
        <version>{version}</version>
    </dependency>
</dependencies>
```


## New Features


## Bugs Fixed
- **Split client construction and connection validation**
  
  To prevent null pointer errors during failover tasks being executed, we go back to the previous model of first creating a client, then opening and validating the connection.
  
  
  

## Code Refactors


## Other Improvements

    

