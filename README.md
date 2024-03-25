## Compilar

`javac -cp src -d bin src/server/Server.java src/client/GoodClient.java src/client/BadClient.java`

## Executar

### Servidor

`java -cp bin server.Server`

### Cliente legítimo

`java -cp bin client.GoodClient`

### Cliente atacante

`java -cp bin client.BadClient`
