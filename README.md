# SDIS - T1G01

## Index

1. [Lab1 - UDP] (https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab1)
2. [Lab2 - UDP Multicast with Java] (https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab2)
3. [Lab3 - TCP] (https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab3)

### [Lab1 - UDP](https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab1)

Open two terminals inside the src folder.
On the first terminal run:
```
$javac Server.java
$java Server 4445
```

On the second terminal run:
```
$javac Client.java
$java Client localhost 4445 REGISTER 11-AA-11 Condutor
$java Client localhost 4445 LOOKUP 11-AA-11
```
#### Documentation
* [Sun's tutorial about the Java API for UDP](http://docs.oracle.com/javase/tutorial/networking/datagrams/index.html)
* [Java API for UDP communication](https://moodle.up.pt/pluginfile.php/103736/mod_page/content/23/udp_java.pdf)
* [User Datagram Protocol](https://tools.ietf.org/html/rfc768)

### [Lab2 - UDP Multicast with Java](https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab2)

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
* [MulticastSocket (Java Platform SE 8)](http://docs.oracle.com/javase/8/docs/api/java/net/MulticastSocket.html)
* [A simple example on java.net.MulticastSocket](https://examples.javacodegeeks.com/core-java/net/multicastsocket-net/java-net-multicastsocket-example/)
* [Oracle's MulticastSocket's Tutorial](http://docs.oracle.com/javase/tutorial/networking/datagrams/broadcasting.html)

### [Lab3 - TCP](https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab3)

This server is able to serve **concurrent** client requests.

Open two terminals inside the src folder.
On the first terminal run:
```
$javac Server.java
$java Server 4445
```

On the second terminal run:
```
$javac Client.java
$java Client localhost 4445 REGISTER 11-AA-11 Condutor
$java Client localhost 4445 LOOKUP 11-AA-11
```
#### Documentation
* [GUIDE](https://web.fe.up.pt/~pfs/aulas/sd2016/labs/lab3.html)
* [Java (and C) APIs for TCP communication](https://web.fe.up.pt/~pfs/aulas/sd2016/labs/7tcp.pdf)
* [Oracle's (TCP) Socket's Tutorial](http://docs.oracle.com/javase/tutorial/networking/sockets/index.html)
* [User Datagram Protocol](https://tools.ietf.org/html/rfc768)
