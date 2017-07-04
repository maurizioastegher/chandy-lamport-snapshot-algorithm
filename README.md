# Chandy-Lamport Distributed Snapshot Algorithm for a Bank Application

A Java implementation (built with Akka: http://akka.io) of the distributed snapshot algorithm of K. Mani Chandy and Leslie Lamport [1].

[1] Chandy, K. M., and Lamport, L. (1985). Distributed snapshots: Determining global states of distributed systems. ACM Transactions on Computer Systems (TOCS), 3(1), 63-75.

## How to Run

* Build the project: 'mvn install' from the root directory;
* Configure the application.conf file located in src/main/resources: 'hostname = "localhost"' and 'seed-nodes = ["akka.tcp://ClusterSystem@localhost:2551"]' to run the application locally, or 'hostname = "[machine's IP address]"' to run the application on different machines;
* Run the MasterMain class: 'java -cp SnapshotAkka-1.0-allinone.jar distributed.snapshotakka.MasterMain' from the target directory;
* Run one or multiple instances of the BankMain class: 'java -cp SnapshotAkka-1.0-allinone.jar distributed.snapshotakka.BankMain numberOfBanks bankNodeName';
* Run one or multiple instance of the CustomerMain class: 'java -cp SnapshotAkka-1.0-allinone.jar distributed.snapshotakka.CustomerMain numberOfCustomers numberOfOperations';
* Start a snapshot by pressing enter while running the MasterMain class.

## Authors

* Astegher Maurizio
* Gambi Enrico
* Guarato Enrico
