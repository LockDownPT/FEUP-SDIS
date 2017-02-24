# Lab1 - UDP

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
###Documentation
* [Sun's tutorial about the Java API for UDP](http://docs.oracle.com/javase/tutorial/networking/datagrams/index.html)
* [Java API for UDP communication](https://moodle.up.pt/pluginfile.php/103736/mod_page/content/23/udp_java.pdf)
* [User Datagram Protocol](https://tools.ietf.org/html/rfc768)
