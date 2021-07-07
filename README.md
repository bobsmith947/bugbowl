# BugBOWL

This is a hackathon competition website for the RISC-V assembly language. Participants compete to debug code as fast as possible.

The easiest way to get started with development is using Gradle. After synchronizing with Gradle, you can use the following tasks:
* `classes` builds the Java class files for the sources under `jvmMain` and `commonMain`
* `jsBrowserDistribution` builds a minified JavaScript file for the sources under `jsMain` and `commonMain`
* `tomcatRun` deploys the application to a test server running under `http://localhost:8080/orgabowl/` (this was the original name and I didn't bother to change the context path)

You also need a PostgreSQL server to store persistent data. The database configuration goes under `web/META-INF/context.xml`.
