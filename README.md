nailgun

=======


This repository contains implementations of a nailgun client in Python and in C.

[![Build status](https://circleci.com/gh/facebook/nailgun.svg?style=svg)](https://circleci.com/gh/facebook/nailgun)

  
## Table of Contents
[What is nailgun?](#what-is-nailgun)<br>
[Benefits of nailgun?](#benefits-of-nailgun)<br>
[MacOS Build and Installation](#macos-build-and-installation)<br>
[Linux Build and Installation](#linux-build-and-installation)<br>
[How to run a java program](#how-to-run-a-java-program)<br>
[Other implementations](#other-implementations)<br> 
[Legal](#legal)<br>


---
What is nailgun?
----------------
Nailgun is a client, protocol, and server for running Java programs from
the command line without incurring the JVM startup overhead.Programs run in the server (which is implemented in Java), and are 
triggered by the client (written in C), which handles all I/O.

**Note:**  Nailgun is based on original code developed by Marty Lamb, you can find his original website <a href="http://www.martiansoftware.com/nailgun/">here</a>.
As of October 2017, the repository has been transferred to facebook and is being maintained and used by <a href="https://buckbuild.com/">Buck team</a>. Nailgun will remain available under the Apache license, version 2.0. 

Benefits of nailgun?
--------------------
Elimanate JVM startup time completely and run all of your java apps in the same JVM called the nailgun server which only starts once. This gets rid of the slow startup times of the JVM everytime you want to run a program. 

---
MacOS Build and Installation 
----------------------
To install nailgun on Mac:

```scala
brew install nailgun
```

To start a background server:
```scala
$ brew install nailgun
$ java -jar /usr/local/Cellar/nailgun/0.9.1/libexec/nailgun-server-0.9.1.jar 
NGServer 0.9.1 started on all interfaces, port 2113.
```
Add the required jars 
```scala
$ ng ng-cp /usr/local/Cellar/scala/2.12.2/libexec/lib/scala-library.jar
$ ng ng-cp s.jar
```

Linux Build and Installation 
----------------------
To install nailgun on Linux:

```scala
sudo apt update
sudo apt install nailgun
```

Using maven, from the project directory:
```scala
mvn clean install
```

The client is built using make.  From the project directory, '
```scala
make && sudo make install
```
To create the windows client
you will additionally need to 
```scala
make ng.exe
```
---
How to run a java program
----------------------
By default Nailgun server runs on port **2113** 

Assume you have a file HelloWorld.class in
```scala
/home/28041/ng-sample/com/testrun/HelloWorld.class
```
run 
```scala
ng ng-cp /home/28041/ng-sample
ng com.testrun.HelloWorld
```

For a more in depth guide go <a href="http://martiansoftware.com/nailgun/quickstart.html">here</a>

Other implementations
----------------------

- [snailgun](https://github.com/jvican/snailgun), a client implementation written in Scala that compiles to native.
- [railgun](https://github.com/timuralp/railgun), a client implementation written in Ruby.


License
-------
Apache License 2.0

Legal
-----
- [Privacy](https://opensource.facebook.com/legal/privacy)
- [Terms](https://opensource.facebook.com/legal/terms)
