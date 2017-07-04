package distributed.snapshotakka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import distributed.snapshotakka.Messages.OutputComplete;
import distributed.snapshotakka.Messages.SnapshotToOutput;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Logs the cluster events to standard output and prints the content of a snapshot to file
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class OutputManager extends UntypedActor {

    Cluster cluster = Cluster.get(getContext().system());

    // Subscribe to cluster changes
    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), MemberUp.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof CurrentClusterState) {
            CurrentClusterState state = (CurrentClusterState) message;
            System.out.println("Current cluster state");
            for (Member member : state.getMembers()) {
                if (member.status().equals(MemberStatus.up())) {
                    System.out.println(member.address().toString());
                }
            }
        } else if (message instanceof MemberUp) {
            MemberUp mUp = (MemberUp) message;
            System.out.print("A new member joined the cluster: ");
            if (mUp.member().hasRole("bank")) {
                System.out.print("bank");
            } else if (mUp.member().hasRole("customer")) {
                System.out.print("customer");
            } else if (mUp.member().hasRole("master")) {
                System.out.print("master");
            }
            System.out.print(" " + mUp.member().address().toString());
        } else if (message instanceof SnapshotToOutput) { // Print the content of a snapshot to file
            SnapshotToOutput snapshotToOutput = (SnapshotToOutput) message;
            System.out.println("\nSnapshot " + snapshotToOutput.getSnapshotContentList().get(0).getSnapshotId() + " completed");
            ArrayList<SnapshotContent> snapshotContentList = snapshotToOutput.getSnapshotContentList();
            Writer writer;
            long total = 0;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("Snapshot" + snapshotContentList.get(0).getSnapshotId() + ".txt"), "utf-8"));
                writer.write("Snapshot initiated by " + snapshotContentList.get(0).getObserver().path().name() + "\n");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                writer.write("Started on: " + sdf.format(snapshotToOutput.getStartTime()) + "\n");
                writer.write("Ended on:\t" + sdf.format(snapshotToOutput.getEndTime()) + "\n");
                writer.write("Duration:\t" + (snapshotToOutput.getEndTime().getTime() - snapshotToOutput.getStartTime().getTime()) + "ms\n\n");
                Collections.sort(snapshotContentList, new Comparator<SnapshotContent>() {
                    @Override
                    public int compare(SnapshotContent s1, SnapshotContent s2) {
                        String[] s1NameArray = s1.getCurrentNode().path().name().split("-");
                        String[] s2NameArray = s2.getCurrentNode().path().name().split("-");
                        if (Integer.parseInt(s1NameArray[s2NameArray.length - 1]) > Integer.parseInt(s2NameArray[s2NameArray.length - 1])) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                for (SnapshotContent content : snapshotContentList) {
                    writer.write(content.getCurrentNode().path().name() + "\n" + "State: " + content.getState() + "\n");
                    total += content.getState();
                    HashMap<ActorRef, List<Integer>> linkState = content.getLinkState();
                    for (Map.Entry<ActorRef, List<Integer>> entry : linkState.entrySet()) {
                        writer.write("From " + entry.getKey().path().name() + ":\t{");
                        boolean first = true;
                        for (int item : entry.getValue()) {
                            if (!first) {
                                writer.write(", ");
                            } else {
                                first = false;
                            }
                            writer.write(String.valueOf(item));
                            total += item;
                        }
                        writer.write("}\n");
                    }
                    writer.write("\n");
                }
                writer.write("Total: " + total + "\n");
                writer.close();
                getSender().tell(new OutputComplete(snapshotContentList.get(0).getSnapshotId()), null);
            } catch (IOException ex) {
            }
        } else {
            unhandled(message);
        }
    }
}