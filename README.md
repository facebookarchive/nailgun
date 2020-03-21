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

This repository contains implementations of a nailgun client in Python and in C.

For additional client implementations in other languages, see:

- [snailgun](https://github.com/jvican/snailgun), a client implementation written in Scala that compiles to native.
- [railgun](https://github.com/timuralp/railgun), a client implementation written in Ruby.

For more information, see [the nailgun website](https://github.com/facebook/nailgun).

## Additional information

### Example usage

```bash
$ ./nailgun-client/target/ng com.facebook.nailgun.examples.ThreadTest
```

### Logging

To enable logging, start the server with `-Djava.util.logging.config.file="logging.properties"`. A relative file path is resolved in respect to the working directory
(i.e. system property `user.dir`). An example file is at `nailgun-server/src/main/resources/logging.properties` but will only be used if (soft-) linked appropriate. 

### Aliases

* Aliases are loaded from `com/facebook/nailgun/builtins/builtins.properties`
* Adding an alias example:
  ```bash
  $ ./nailgun-client/target/ng ng-alias test com.facebook.nailgun.examples.ThreadTest
  $ ./nailgun-client/target/ng test
  ```

#### Built-in aliases

```bash
$ ./nailgun-client/target/ng ng-alias
ng-alias        com.facebook.nailgun.builtins.NGAlias      
                Displays and manages command aliases

ng-cp           com.facebook.nailgun.builtins.NGClasspath  
                Displays and manages the current system classpath

ng-stats        com.facebook.nailgun.builtins.NGServerStats
                Displays nail statistics

ng-stop         com.facebook.nailgun.builtins.NGStop       
                Shuts down the nailgun server

ng-version      com.facebook.nailgun.builtins.NGVersion    
                Displays the server version number.
```

The alias `ng-cp` is _very_ dangerous and YOU are encouraged to set 
`com.facebook.nailgun.builtins.NGClasspath.ALLOW_CLASSPATH_MODIFICATION` to 
`false`.

### Links

* [original homepage](http://www.martiansoftware.com/nailgun/)
