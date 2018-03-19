# exception-examples

#### To test the disclosure:

In a console, do:
```
$ sbt clean docker:publishLocal
$ ./exception-examples-impl/target/docker/stage/opt/docker/bin/exception-examples-impl
$ curl http://localhost:9000/api/disclose
```