[![Codacy Badge](https://api.codacy.com/project/badge/Grade/21ce2aa7aae34058aee8fccb95069e39)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=pedro-c/FEUP-SDIS&amp;utm_campaign=Badge_Grade)

To start rmi run the following command:
```
rmiregistry -J-Djava.rmi.server.codese=file:///home/pedroc/Documents/FEUP-SDIS/ServerlessDBS/out/production/ServerlessDBS/
```
To start the server run the following command:
```
java Peer.InitPeer 1.0 1 224.0.0.0 4445 224.0.0.1 4446 224.0.0.2 4447
```
To start the server run the following command:
```
java TestingClientApplication.TCA 1 BACKUP lbaw.pdf 3
```

To run multicastsnooper:
```
java -jar McastSnooper.jar 224.0.0.0:4445 224.0.0.1:4446 224.0.0.2:4447
```

		
