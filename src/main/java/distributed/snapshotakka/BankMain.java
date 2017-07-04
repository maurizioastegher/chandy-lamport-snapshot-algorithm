package distributed.snapshotakka;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Main program that initializes some banks on the host
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class BankMain {

    public static void main(String[] args) {
        final int numberOfBanks;
        if (args.length > 0) {
            try {
                numberOfBanks = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument type.\nUsage: java distributed.snapshotakka.BankMain numberOfBanks bankNodeName");
                return;
            }
        } else {
            numberOfBanks = 10;
        }
        final String namePrefix = args.length > 1 ? "bank-"+args[1] : "bank";

        // Initialize Akka
        final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").
                withFallback(ConfigFactory.parseString("akka.cluster.roles = [bank]")).
                withFallback(ConfigFactory.load());
        ActorSystem system = ActorSystem.create("ClusterSystem", config);

        // Initialize each bank and modify its name
        String nodeName = namePrefix + "-";
        for (int i = 0; i < numberOfBanks; i++) {
            system.actorOf(Props.create(Bank.class), nodeName + i);

        }
    }
}