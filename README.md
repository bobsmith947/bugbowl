# BugBOWL

This is a hackathon competition website for the RISC-V assembly language. Participants form groups and compete to debug code as fast as they can.

## Getting Started

The website is written entirely in Kotlin, using the Kotlin Multiplatform libraries. The easiest way to start with development is using Gradle. After synchronizing with Gradle, you can use the following tasks to build and run code:
* `classes` builds the Java class files for the sources under `jvmMain` and `commonMain`
* `jsBrowserDistribution` builds a minified JavaScript file for the sources under `jsMain` and `commonMain`
* `tomcatRun` deploys the application to a test server running under `http://localhost:8080/orgabowl/` (this was the original name and I didn't bother to change the context path)

You also need a PostgreSQL server to store persistent data. The database configuration goes under `web/META-INF/context.xml`. Once configured, you can run the SQL statements in `schema.sql` to set up the needed tables and indexes. The data is stored in `jsonb` format, which is extremely flexible for adding and removing fields as necessary.

### Logging In

The `REMOTE_USER` environment variable is used to determine the identity of the logged in user. In production, this is set by a SSO service provider. However in development, you will instead want to hardcode an admin user to login as in `LoginServlet.kt`.

~~`val user = req.remoteUser?.let { Database.getUser(it.substringBefore('@')) }`~~

`val user = User(0, "admin", true)`

## Deploying

If you wish to deploy the app to your own server, you may want to consider serving it behind a proxy, so as not to expose your Tomcat instance to the public. This will also allow you to configure a SSO service provider as previously mentioned. For example, in `httpd.conf` you would put:

```
ProxyPass / ajp://localhost:8009/bugbowl/
ProxyPassReverse / https://example.com/bugbowl/
```

The AJP connector will need to be separately configured for Tomcat. If you do not wish to use AJP, you could instead use the standard HTTP connector, however AJP is preferred. Do note that any paths you wish to exclude from the proxy will need to be included before the root.

## Acknowledgements

BugBOWL was developed for the Computer Science department at the Colorado School of Mines for use in computer organization classes.

There is some influence taken from the [AlgoBOWL website](https://github.com/jackrosenthal/algobowl) used in algorithms classes at the Colorado School of Mines.

[RARS](https://github.com/TheThirdOne/rars) is used to assemble and run the RISC-V code submitted to the website.
