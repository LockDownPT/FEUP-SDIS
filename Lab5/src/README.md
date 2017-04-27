# [Lab3 - TCP](https://github.com/pedro-c/FEUP-SDIS/tree/master/Lab3)

This SSLServer is able to serve **concurrent** SSLClient requests.

Open two terminals inside the src folder.
On the first terminal run:
```
$javac SSLServer.java
$java -Djavax.net.ssl.trustStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/truststore -Djavax.net.ssl.trustStorePassword=123456 -Djavax.net.ssl.keyStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/server.keys -Djavax.net.ssl.keyStorePassword=123456 SSLServer 4445 TLS_RSA_WITH_AES_128_CBC_SHA```


```
On the second terminal run:
```
$javac SSLClient.java
$java -Djavax.net.ssl.keyStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/truststore -Djavax.net.ssl.trustStorePassword=123456 SSLClient localhost 4445 REGISTER 11-AA-11 Condutor TLS_RSA_WITH_AES_128_CBC_SHA
$java -Djavax.net.ssl.trustStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/truststore -Djavax.net.ssl.trustStorePassword=123456 SSLClient localhost 4445 LOOKUP 11-AA-11 TLS_RSA_WITH_AES_128_CBC_SHA
```
#### Documentation
* [GUIDE](https://web.fe.up.pt/~pfs/aulas/sd2016/labs/lab3.html)
* [Java (and C) APIs for TCP communication](https://web.fe.up.pt/~pfs/aulas/sd2016/labs/7tcp.pdf)
* [Oracle's (TCP) Socket's Tutorial](http://docs.oracle.com/javase/tutorial/networking/sockets/index.html)
* [User Datagram Protocol](https://tools.ietf.org/html/rfc768)


java -D-Djavax.net.ssl.trustStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/truststore -Djavax.net.ssl.trustStorePassword=123456 javax.net.ssl.keyStore=/home/pedroc/Documents/FEUP-SDIS/Lab5/src/server.keys -Djavax.net.ssl.keyStorePassword=123456 SSLServer 4445 TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA