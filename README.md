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
```

### How to run server?

```
java -jar keystore.jar -server                   -> defaults to port 1099
java -jar keystore.jar -server -port 15000
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
post <key> <value>
put <key> <value>
get <key>
delete <key>
Application also pre-populates 10 post entries, 5 put entries, 5 gets and 5 deletes from key_value.txt file
```