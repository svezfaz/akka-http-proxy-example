Example application to reproduce a problem with `http.server.max-connections` setting: 

to run:

```
sbt app/run
```

to test:

```
sbt sim/gatling:test
```

At the end of the test (i.e. when the test terminates!) it should be able to see much more open connections than expected.