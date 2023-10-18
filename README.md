nailgun
=======

[![Build status](https://circleci.com/gh/facebook/nailgun.svg?style=svg)](https://circleci.com/gh/facebook/nailgun)

---

**Note:**  Nailgun is based on original code developed by <a href="http://martylamb.com/">Marty Lamb</a>.
In October, 2017, Marty transferred the repository to Facebook, where it is was previously
maintained by <a href="https://buck.build/">the Buck1 team</a>. In April, 2023, Buck1 was deprecated in
favor of <a href="https://buck2.build/">Buck2</a>, which does not use Nailgun.
As a result this repository is now unmaintained.

Nailgun remains available under the Apache license, version 2.0.

---

Build and Installation
----------------------

Nailgun is a client, protocol, and server for running Java programs from
the command line without incurring the JVM startup overhead.

Programs run in the server (which is implemented in Java), and are 
triggered by the client (written in C), which handles all I/O.

The server and examples are built using maven.  From the project directory,
"mvn clean install" will do it.

The client is built using make.  From the project directory, 
"make && sudo make install" will do it.  To create the windows client
you will additionally need to "make ng.exe".

This repository contains implementations of a nailgun client in Python and in C.

For additional client implementations in other languages, see:

- [snailgun](https://github.com/jvican/snailgun), a client implementation written in Scala that compiles to native.
- [railgun](https://github.com/timuralp/railgun), a client implementation written in Ruby.

A PHP client is also available through the [nailgun-php](https://github.com/alirezameskin/nailgun-php) project.

For more information, see [the nailgun website](https://github.com/facebook/nailgun).

License
-------
Apache License 2.0

Legal
-----
- [Privacy](https://opensource.facebook.com/legal/privacy)
- [Terms](https://opensource.facebook.com/legal/terms)
