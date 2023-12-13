# key-value-store

### Executable

```
keystore.jar
```

### Program Arguments

```
-server    -> run as server
-client    -> run as client
-port      -> server rmi port   
-f         -> failure
```

### How to run server?

```
java -jar keystore.jar -server                   // defaults to port 1099
java -jar keystore.jar -server -port 15000
java -jar keystore.jar -server -port 15000 -f    // -f adds failure rate of 0.25 at servers
```

### How to run client?

```
java -jar keystore.jar -client                   -> defaults to server rmi port 1099
java -jar keystore.jar -client -port 15000
```

### Log files location

```
user home dir
%h/client%u.log     -> client logs
%h/server%u.log     -> server logs
```

### Commands

```
PUT <key> <value>
GET <key>
DELETE <key>
Application also pre-populates 5 put entries, and then performs 5 put, 5 get and 5 delete operations from key_value.txt file
```