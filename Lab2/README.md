# Lab2 - UDP Multicast with Java

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

[Guide](https://web.fe.up.pt/~pfs/aulas/sd2017/labs/lab2.html)

###Documentation
*[MulticastSocket (Java Platform SE 8)](http://docs.oracle.com/javase/8/docs/api/java/net/MulticastSocket.html)
*[A simple example on java.net.MulticastSocket](https://examples.javacodegeeks.com/core-java/net/multicastsocket-net/java-net-multicastsocket-example/)
*[Oracle's MulticastSocket's Tutorial](http://docs.oracle.com/javase/tutorial/networking/datagrams/broadcasting.html)
