# [Lab3 - TCP](https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab3)

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
