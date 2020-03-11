nailgun
=======

[![Build status](https://circleci.com/gh/facebook/nailgun.svg?style=svg)](https://circleci.com/gh/facebook/nailgun)

---
**Note:**  Nailgun is based on original code developed by <a href="http://martylamb.com/">Marty Lamb</a>.
In October, 2017, Marty transferred the repository to Facebook, where it is currently
maintained by <a href="https://buckbuild.com/">Buck team</a>. Nailgun will remain available under the Apache license, version 2.0.
---

Nailgun is a client, protocol, and server for running Java programs from
the command line without incurring the JVM startup overhead.

Programs run in the server (which is implemented in Java), and are 
triggered by the client (written in C), which handles all I/O.

The server and examples are built using maven.  From the project directory,
"mvn clean install" will do it.

The client is built using make.  From the project directory, 
"make && sudo make install" will do it.  To create the windows client
you will additionally need to "make ng.exe".

A ruby client is available through the [railgun](https://github.com/timuralp/railgun) project.

For more information, see [the nailgun website](https://github.com/facebook/nailgun).


