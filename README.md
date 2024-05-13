# Trading Service
Hi, here's the POC trading system service I've implemented. I have tried to fulfill the requirements and
write what I deemed most necessary (basic structure, simple api level with serialization and validation, DTO, basic exception handling
and so on). Of course it is not possible to write a real production ready application so these are the things I either left
out or simplified:
- Documentation (just wrote basic Java doc and some comments)
- Security (endpoints are all accessible and there are no users with registrations and logins)
- No real setup for deployments (Docker, CI files, maven build configs)
- Money management, kept everything in Float, without Money specific classes or currency handling
- Caching, the database is reached all the times, while some things could be cached (like last element for any given symbol)

Also the potential high-concurrency was mainly handled by the database itself (kdb).

### How to run
Needed:
* Java 17
* Maven

A running instance of the database KDB is should be running on port 7001 before starting the application.
Generally for databases running locally a Docker setup is easier, but KDB does not provide a default image
because a personal LICENCE is needed. This complicates the local setup because each dev would need to download
and add the licence.

#### Run
```
mvn install:install-file -Dfile=lib/jdbc.jar -DgroupId=com.lucarospocher -DartifactId=jdbc-kdb -Dversion=1 -Dpackaging=jar
mvn compile
mvn spring-boot:run
```
### Design
Given the requisites, my main focus was on chosing the datasource.
It is impossible to know the best datasource, it depends if this system would be more READ or WRITE heavy.

* A relational database would be easy to setup and very flexible to changes
but lacks on real time aggregation performed on-demand (also no relationships needed)

* Something like MongoDB would be the clear choice because of simplicity and great performances for
this kind of tasks (simple inserts and aggregations).

* However I wanted to push for performance and testout some in-memory databases specialised in
trading/financial data where performance is critical. I've chosen KDB, integrated through Spring
JDBCTemplate, but with manually written queries (in Q) to get max performance.

#### Result
KDB has great performance at least for simple tests that I've made locally,
but the support for Java is really limited, the JDBC driver is present but buggy
so I had to write queries manually mostly (no type-checking, potential "sql" injection) and so on.

On the other side inserting a batch of data or getting all the stats, **took around 5-10ms (on my machine)**

### Possible Improvements

* If insert volume is estimates to be to big, creating events on some queue/ledger (e.g. Kafka), would be more safe in cases of burst of inserts
