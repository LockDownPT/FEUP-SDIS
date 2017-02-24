# Lab1

Open two terminals inside the src folder.
On the first terminal run:
```
$javac MulticastServer.java
$java MulticastServer 4445 224.0.0.1 4446
```

On the second terminal run:
```
$javac Client.java
$java Client 224.0.0.1 4446 REGISTER 11-AA-11 Condutor
$java Client 224.0.0.1 4446 LOOKUP 11-AA-11
```