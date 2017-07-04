package distributed.snapshotakka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static distributed.snapshotakka.Messages.SNAPSHOTSTART;

/**
 * Provides a seed node for the cluster, initializes BankManager and OutputManager, waits for the user to start a
 * snapshot (by pressing the enter key)
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class MasterMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize Akka
        final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=2551").
                withFallback(ConfigFactory.parseString("akka.cluster.roles = [master]")).
                withFallback(ConfigFactory.load());
        ActorSystem system = ActorSystem.create("ClusterSystem", config); // Create an actor system
        final Inbox inbox = Inbox.create(system);

        // Initialize BankManager and OutputManager
        ActorRef bankManager = system.actorOf(Props.create(BankManager.class), "bankManager");
        ActorRef outputManager = system.actorOf(Props.create(OutputManager.class), "outputManager");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Thread.sleep(2000); // Wait for the the BankManager and OutputManager to be initialized
        while (true) {
            System.out.println("\n*\n* PRESS ENTER TO START A SNAPSHOT\n*");
            String s = br.readLine();
            System.out.println("Snapshot started!");
            inbox.send(bankManager, SNAPSHOTSTART); // Tell the BankManager to start a snapshot
        }
    }
}