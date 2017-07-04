package distributed.snapshotakka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import distributed.snapshotakka.Messages.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static distributed.snapshotakka.Messages.*;

/**
 * Represents a bank that can send and receive money; implements the Chandy-Lamport snapshot algorithm
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class Bank extends UntypedActor {

    private int balance = 1000; // State of the node
    // Associates to each snapshot id, the content of the snapshot recorded by this bank
    private final HashMap<Long, SnapshotContent> snapshotContentMap;
    // When we act as the observer, stores the SnapshotContent of each participating bank
    private final HashMap<Long, ArrayList<SnapshotContent>> observerSnapshotContentMap;
    private final HashMap<Long, Date> startTimeMap;
    private ActorRef bankManager;

    public Bank() {
        snapshotContentMap = new HashMap<Long, SnapshotContent>();
        observerSnapshotContentMap = new HashMap<Long, ArrayList<SnapshotContent>>();
        startTimeMap = new HashMap<Long, Date>();
        System.out.println("Bank created: " + getSelf().toString());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message.equals(REGISTRATION)) { // Reply to registration message
            System.out.println("Registration received: " + getSelf().toString());
            bankManager = getSender();
            getSender().tell(REGISTRATION, getSelf());
        } else if (message instanceof SendMoney) { // Update balance and send money to the destination bank
            SendMoney sendMoney = (SendMoney) message;
            balance = balance - sendMoney.getMoney();
            sendMoney.getDestination().tell(new Transaction(sendMoney.getMoney()), getSelf());
        } else if (message instanceof Transaction) { // Update balance
            balance = balance + ((Transaction) message).getMoney();
            if (!snapshotContentMap.isEmpty()) { // If we are doing a snapshot...
                for (SnapshotContent content : snapshotContentMap.values()) {
                    if (!content.getTokenReceived().get(getSender())) {  // If we didn't receive the token from the sender of the message
                        // Add the message to the link state
                        content.getLinkState().get(getSender()).add(((Transaction) message).getMoney());
                    }
                }
            }
        } else if (message instanceof BankListForSnapshot) {
            // This bank has been selected to start a snapshot; start the snapshot by sending a token to myself
            startTimeMap.put(((BankListForSnapshot) message).getSnapshotId(), new Date());
            getSelf().tell(new SnapshotToken(getSelf(),
                            ((BankListForSnapshot) message).getSnapshotId(),
                            ((BankListForSnapshot) message).getBankList()),
                    getSelf());
        } else if (message instanceof SnapshotToken) { // Snapshot token received
            SnapshotToken token = ((SnapshotToken) message);
            if (!snapshotContentMap.containsKey(token.getSnapshotId())) {
                // This is the first token we have received with this snapshot id; save the current state and
                // broadcast the snapshot token
                List<ActorRef> nodeList = new ArrayList<ActorRef>();
                nodeList.addAll(token.getNodeList());
                nodeList.remove(getSelf());
                SnapshotContent snapshotContent = new SnapshotContent(nodeList, token.getObserver(), getSelf(), token.getSnapshotId());
                snapshotContent.setState(balance);
                if (!getSender().equals(getSelf())) {
                    snapshotContent.getTokenReceived().put(getSender(), true);
                }
                snapshotContentMap.put(token.getSnapshotId(), snapshotContent);
                // Broadcast the token
                for (ActorRef actor : nodeList) {
                    actor.tell(new SnapshotToken(snapshotContent.getObserver(), snapshotContent.getSnapshotId(), token.getNodeList()), getSelf());
                }
            } else {
                // We have already received a snapshot token with this snapshot id; register the sender of the token
                SnapshotContent snapshotContent = snapshotContentMap.get(token.getSnapshotId());
                snapshotContent.getTokenReceived().put(getSender(), true);
                if (!snapshotContent.getTokenReceived().containsValue(false)) {
                    // We have received a token from all the incoming links; send the snapshot content to the observer node
                    snapshotContent.getObserver().tell(snapshotContent, getSelf());
                    snapshotContentMap.remove(token.getSnapshotId());
                }
            }
        } else if (message instanceof SnapshotContent) {
            // As the observer, we receive the snapshot content from the banks that have completed the snapshot
            SnapshotContent content = (SnapshotContent) message;
            ArrayList<SnapshotContent> snapshotContentList = observerSnapshotContentMap.get(content.getSnapshotId());
            if (snapshotContentList == null) {
                snapshotContentList = new ArrayList<SnapshotContent>();
                snapshotContentList.add(content);
                observerSnapshotContentMap.put(content.getSnapshotId(), snapshotContentList);
            } else {
                observerSnapshotContentMap.get(content.getSnapshotId()).add(content);
                if (observerSnapshotContentMap.get(content.getSnapshotId()).size() == (content.getLinkState().size()) + 1) {
                    // We have received the snapshot content from everybody; send the collected data to the OutputManager
                    getContext().actorSelection(bankManager.path().parent().child("outputManager"))
                            .tell(new Messages.SnapshotToOutput(snapshotContentList, startTimeMap.get(content.getSnapshotId()), new Date()), getSelf());
                }
            }
        } else if (message instanceof OutputComplete) { // The OutputManager finished processing data; cleanup
            OutputComplete outputComplete = (OutputComplete) message;
            observerSnapshotContentMap.remove(outputComplete.getSnapshotId());
            startTimeMap.remove(outputComplete.getSnapshotId());
        } else {
            unhandled(message);
        }
    }
}