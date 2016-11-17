# corfukv 

CorfuKV is a key-value store implemented using [CorfuDB](https://github.com/CorfuDB/CorfuDB). It is meant mostly as simple example of how to use CorfuDB. 

CorfuKV consists of a daemon and a client. Each Daemon keeps an in-memory map of keys to values which is synchronized across instances using a CorfuDB log. Clients can perform `get` and `set` operations against any instance of the daemon and their changes will be replicated across all instances. 

CorfuKV uses [GRPC](http://www.grpc.io/) for communication. The daemon implements the server side of a simple key value interface (`get(string)->string` and `set(string,string)->bool`). This is exposed as a GRPC endpoint. The client implements the clientside of the GRPC interface and implements a set of CLI commands allowing the user to get and set keys in the daemon.  

# Quickstart 

1. Build corfukv 

```
mvn install 
``` 

2. Run a CorfuDB instance 

```
$ corfu_server -ms 9000 
```

3. Start a CorfuKV daemon 
```
$ bin/corfukvd 
```

4. Use the CorfuKV client to run commands on the daemon 
```
$ bin/corfukv set -key foo -value bar 
true 
$ bin/corfukv get -key foo 
bar 
``` 

