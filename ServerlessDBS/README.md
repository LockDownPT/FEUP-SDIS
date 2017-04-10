[![CodeFactor](https://www.codefactor.io/repository/github/pedro-c/feup-sdis/badge)](https://www.codefactor.io/repository/github/pedro-c/feup-sdis)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/21ce2aa7aae34058aee8fccb95069e39)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=pedro-c/FEUP-SDIS&amp;utm_campaign=Badge_Grade)

To compile, run the rmi, run the snooper and launhc peers run:
```
bash peers.sh <Number of Peers> <Version> <MCip> <MCport> <MDBip> <MDBport> <MDRip> <MDRport>
```

To start the test client application run:
```
bash tca.sh <Access Point> <Protocol> [ <Number of Bytes> | <File> | <File>, <Replication Degree>]
```

OR RUN MANUALLY:

To start rmi run the following command:
```
rmiregistry -J-Djava.rmi.server.codese=file:///home/pedroc/Documents/FEUP-SDIS/ServerlessDBS/out/production/ServerlessDBS/
```

To start the peer run the following command(advised to run at least 3 peers):
```
java Peer.InitPeer 1.0 1 224.0.0.0 4445 224.0.0.1 4446 224.0.0.2 4447
```

To test the BACKUP protocol run the following command:
```
java TestingClientApplication.TCA 1 BACKUP 300kb.pdf 2
```
To test the RESTORE protocol run the following command:
```
java TestingClientApplication.TCA 1 RESTORE lbaw.pdf
```
To test the STATE protocol run the following command:
```
java TestingClientApplication.TCA 1 STATE
```

To run multicastsnooper:
```
java -jar McastSnooper.jar 224.0.0.0:4445 224.0.0.1:4446 224.0.0.2:4447
```
