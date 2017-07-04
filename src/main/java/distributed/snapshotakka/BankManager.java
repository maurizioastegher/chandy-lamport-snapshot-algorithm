package distributed.snapshotakka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitors the cluster for new banks or customers, provides all the required information for the snapshot
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class BankManager extends UntypedActor {

    private final Cluster cluster = Cluster.get(getContext().system());
    private List<ActorRef> bankList = new ArrayList<ActorRef>();
    private long lastSnapshotId = 0;

    // Subscribe to cluster changes
    @Override
    public void preStart() {
        System.out.println("BankManager: " + getSelf().toString());
        cluster.subscribe(getSelf(), ClusterEvent.MemberUp.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ClusterEvent.MemberUp) {
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            if (mUp.member().hasRole("bank")) {  // A member with role 'bank' joined the system
                // Send a registration message to all the banks in that host
                getContext().actorSelection(mUp.member().address() + "/user/bank*").tell(Messages.REGISTRATION, getSelf());
            }
            if (mUp.member().hasRole("customer")) { // A member with role 'customer' joined the system
                // Send the list of banks to all the customers in that host
                getContext().actorSelection(mUp.member().address() + "/user/customer*").tell(new Messages.BankList(bankList), getSelf());
            }
        } else if (message.equals(Messages.REGISTRATION)) {
            bankList.add(getSender()); // Add the bank who replied to the registration message to the bank list
        } else if (message.equals(Messages.SNAPSHOTSTART)) {
            if (!bankList.isEmpty()) {
                // Select a random bank that will act as the observer for the snapshot
                bankList.get((int) (Math.random() * bankList.size())).tell(new Messages.BankListForSnapshot(bankList, lastSnapshotId++), ActorRef.noSender());
            } else {
                System.out.println("\nNo banks in the system, snapshot aborted!");
            }
        } else {
            unhandled(message);
        }
    }
}