package distributed.snapshotakka;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Defines the messages exchanged between actors of the system
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public interface Messages {
    String REGISTRATION = "registration";

    // Sent by MasterMain to the BankManager when the user starts a snapshot
    String SNAPSHOTSTART = "snapshotStart";

    // Sent by the BankManager to the observer of a snapshot; provides the list of banks and a snapshot id
    class BankListForSnapshot implements Serializable {
        private final List<ActorRef> bankList;
        private final long snapshotId;

        public BankListForSnapshot(List<ActorRef> bankList, long snapshotId) {
            this.bankList = bankList;
            this.snapshotId = snapshotId;
        }

        public List<ActorRef> getBankList() {
            return bankList;
        }

        public long getSnapshotId() {
            return snapshotId;
        }
    }

    // Sent by the BankManager to each customer; provides the list of banks
    class BankList implements Serializable {
        private final List<ActorRef> bankList;

        public BankList(List<ActorRef> bankList) {
            this.bankList = bankList;
        }

        public List<ActorRef> getBankList() {
            return bankList;
        }
    }

    // Sent by CustomerMain to each customer; provides the number of operations to perform
    class DoOperations implements Serializable {
        private final int numberOfOperations;

        public DoOperations(int numberOfOperations) {
            this.numberOfOperations = numberOfOperations;
        }

        public int getNumberOfOperations() {
            return numberOfOperations;
        }
    }

    // Sent by a customer to a bank; specifies the amount of money to transfer and the destination bank
    class SendMoney implements Serializable {
        private final ActorRef destination;
        private final int money;

        public SendMoney(ActorRef destination, int money) {
            this.destination = destination;
            this.money = money;
        }

        public ActorRef getDestination() {
            return destination;
        }

        public int getMoney() {
            return money;
        }
    }

    // Sent by a bank to another; specifies the amount of money transferred
    class Transaction implements Serializable {

        private final int money;

        public Transaction(int money) {
            this.money = money;
        }

        int getMoney() {
            return money;
        }
    }

    // Snapshot token containing the snapshot id, the observer node and the list of participating banks
    class SnapshotToken implements Serializable {

        private final ActorRef observer;
        private final long snapshotId;
        private final List<ActorRef> nodeList;

        public SnapshotToken(ActorRef observer, long snapshotId, List<ActorRef> nodeList) {
            this.observer = observer;
            this.snapshotId = snapshotId;
            this.nodeList = nodeList;
        }

        public ActorRef getObserver() {
            return observer;
        }

        public long getSnapshotId() {
            return snapshotId;
        }

        public List<ActorRef> getNodeList() {
            return nodeList;
        }
    }

    // Sent by the observer node to the OutputManager; contains the snapshot data
    class SnapshotToOutput implements Serializable {
        private final ArrayList<SnapshotContent> snapshotContentList;
        private final Date startTime;
        private final Date endTime;

        public SnapshotToOutput(ArrayList<SnapshotContent> snapshotContentList, Date startTime, Date endTime) {
            this.snapshotContentList = snapshotContentList;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public ArrayList<SnapshotContent> getSnapshotContentList() {
            return snapshotContentList;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }
    }

    // Sent by the OutputManager to the observer node; cleanup purposes
    class OutputComplete implements Serializable {

        private final long snapshotId;

        public long getSnapshotId() {
            return snapshotId;
        }

        public OutputComplete(long snapshotId) {
            this.snapshotId = snapshotId;
        }
    }
}