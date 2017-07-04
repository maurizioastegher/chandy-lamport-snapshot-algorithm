package distributed.snapshotakka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Main program that initializes some customers on the host
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class CustomerMain {

    public static void main(String[] args) {
        final int numberOfCustomers;
        final int numberOfOperations;
        if (args.length > 0) {
            try {
                numberOfCustomers = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument type.\nUsage: java distributed.snapshotakka.CustomerMain numberOfCustomers numberOfOperations");
                return;
            }
        } else {
            numberOfCustomers = 5;
        }
        if (args.length > 1) {
            try {
                numberOfOperations = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument type.\nUsage: java distributed.snapshotakka.CustomerMain numberOfCustomers numberOfOperations");
                return;
            }
        } else {
            numberOfOperations = 5000;
        }

        // Initialize Akka
        final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=0").
                withFallback(ConfigFactory.parseString("akka.cluster.roles = [customer]")).
                withFallback(ConfigFactory.load());
        ActorSystem system = ActorSystem.create("ClusterSystem", config);
        final Inbox inbox = Inbox.create(system);

        // Initialize each customer
        List<ActorRef> customers = new ArrayList();
        for (int i = 0; i < numberOfCustomers; i++) {
            customers.add(system.actorOf(Props.create(Customer.class), "customer-" + i));
        }

        // Tell each customer the number of operations to perform
        for (ActorRef customer : customers) {
            inbox.send(customer, new Messages.DoOperations(numberOfOperations));
        }
    }
}