package distributed.snapshotakka;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Portion of a snapshot recorded by a single node; it will be sent to the observer node at completion
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class SnapshotContent implements Serializable {

    private int state; // Current state of the node (balance)
    private HashMap<ActorRef, List<Integer>> linkState; // State of the links
    // Records whether the node has received the token from the incoming links
    private HashMap<ActorRef, Boolean> tokenReceived;
    private final ActorRef observer; // Who initiated the snapshot
    private final ActorRef currentNode; // Who is recording this data
    private final long snapshotId; // Id of the snapshot

    public SnapshotContent(List<ActorRef> linkList, ActorRef observer, ActorRef currentNode, long snapshotId) {
        initializeLinkState(linkList);
        initializeTokenReceived(linkList);
        this.observer = observer;
        this.currentNode = currentNode;
        this.snapshotId = snapshotId;
    }

    private void initializeLinkState(List<ActorRef> linkList) {
        linkState = new HashMap<ActorRef, List<Integer>>();
        for (ActorRef actor : linkList) {
            linkState.put(actor, new ArrayList<Integer>());
        }
    }

    private void initializeTokenReceived(List<ActorRef> linkList) {
        tokenReceived = new HashMap<ActorRef, Boolean>();
        for (ActorRef actor : linkList) {
            tokenReceived.put(actor, Boolean.FALSE);
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public HashMap<ActorRef, List<Integer>> getLinkState() {
        return linkState;
    }

    public void setLinkState(HashMap<ActorRef, List<Integer>> linkState) {
        this.linkState = linkState;
    }

    public HashMap<ActorRef, Boolean> getTokenReceived() {
        return tokenReceived;
    }

    public void setTokenReceived(HashMap<ActorRef, Boolean> tokenReceived) {
        this.tokenReceived = tokenReceived;
    }

    public ActorRef getObserver() {
        return observer;
    }

    public ActorRef getCurrentNode() {
        return currentNode;
    }

    public long getSnapshotId() {
        return snapshotId;
    }
}