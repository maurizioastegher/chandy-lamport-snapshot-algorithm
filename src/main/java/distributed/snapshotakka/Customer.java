package distributed.snapshotakka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import distributed.snapshotakka.Messages.BankList;
import distributed.snapshotakka.Messages.DoOperations;
import distributed.snapshotakka.Messages.SendMoney;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simulates a user that transfers money between banks
 *
 * @author Astegher Maurizio, Gambi Enrico, Guarato Enrico
 */
public class Customer extends UntypedActor {
    private List<ActorRef> bankList;
    public ActorRef customer;

    public Customer() {
        System.out.println("Customer created: " + getSelf().toString());
        customer = getSelf();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof BankList) { // Update the list of banks on which we operate
            System.out.println("Bank list received");
            bankList = ((BankList) message).getBankList();
        } else if (message instanceof DoOperations) { // Perform transactions between random banks
            DoOperations doOperations = (DoOperations) message;
            if (bankList == null) { // Bank list not received yet; retry after two seconds
                System.out.println("Customer not ready, retrying...");
                new RetryThread(doOperations).start();
            } else {
                if (bankList.isEmpty()) {
                    System.out.println("No banks in the system!");
                    System.exit(0);
                }
                System.out.println("Doing " + doOperations.getNumberOfOperations() + " operations...");
                for (int i = 0; i < doOperations.getNumberOfOperations(); i++) {
                    // Select two random banks and move money from one to the other
                    int numberOfBanks = bankList.size();
                    int sender, receiver;

                    do {
                        sender = (int) (Math.random() * numberOfBanks);
                        receiver = (int) (Math.random() * numberOfBanks);
                    } while (sender == receiver);
                    bankList.get(sender).tell(new SendMoney(bankList.get(receiver), 10), ActorRef.noSender());
                    if (i % 100 == 0) { // Wait every 100 operations; avoids network congestion
                        Thread.sleep(500);
                    }
                }
                System.out.println("DONE!");
            }
        } else {
            unhandled(message);
        }
    }

    // The customer was not ready; send again a DoOperations message after two seconds
    private class RetryThread extends Thread {
        private final DoOperations doOperations;

        public RetryThread(DoOperations doOperations) {
            super("retryThread");
            this.doOperations = doOperations;
        }

        @Override
        public void start() {
            try {
                Thread.sleep(2000);
                customer.tell(doOperations, ActorRef.noSender());
            } catch (InterruptedException ex) {
                Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}