package patient;

import injury.Injury;
import jade.core.Agent;
import jade.lang.acl.UnreadableException;
import utils.Location;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

public class PatientAgent extends Agent {

    private Injury injury;
    private Location position;
    private int nResponders;

    public void setup(String[] args) {

        String x = args[0], y = args[1], injuryType = args[2], injurySeverity = args[3];
        this.injury = new Injury(injuryType, Integer.parseInt(injurySeverity));
        this.position = new Location(Integer.parseInt(x), Integer.parseInt(y));
        /*
          TODO:
          - sends message to all helicopters
          - receives reply from helicopters (X time after sending)
          - sends confirmation message to the chosen helicopter
         */
        System.out.println("Yes I am a patient");
        System.out.println("I am at location " + this.position.toString());
        System.out.println("My injury is: " + this.injury.toString() + "\n");

        //copied from basic ContractNetInitiator
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            nResponders = args.length;
            System.out.println("Trying to delegate pick-me-up action to one out of " + nResponders + " helicopters.");

            // Fill the CFP message
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < args.length; ++i) {
                msg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 10 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            msg.setContent("Send me yor location.");

            addBehaviour(new ContractNetInitiator(this, msg) {

                protected void handlePropose(ACLMessage propose, Vector v) {
                    System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
                }

                protected void handleRefuse(ACLMessage refuse) {
                    System.out.println("Agent "+refuse.getSender().getName()+" refused");
                }

                protected void handleFailure(ACLMessage failure) {
                    if (failure.getSender().equals(myAgent.getAMS())) {
                        // FAILURE notification from the JADE runtime: the receiver
                        // does not exist
                        System.out.println("Responder does not exist");
                    }
                    else {
                        System.out.println("Agent "+failure.getSender().getName()+" failed");
                    }
                    // Immediate failure --> we will not receive a response from this agent
                    nResponders--;
                }

                protected void handleAllResponses(Vector responses, Vector acceptances) {
                    if (responses.size() < nResponders) {
                        // Some responder didn't reply within the specified timeout
                        System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
                    }
                    // Evaluate proposals.
                    double bestProposal = Double.POSITIVE_INFINITY;
                    AID bestProposer = null;
                    ACLMessage accept = null;
                    Enumeration e = responses.elements();
                    while (e.hasMoreElements()) {
                        ACLMessage msg = (ACLMessage) e.nextElement();
                        if (msg.getPerformative() == ACLMessage.PROPOSE) {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                            acceptances.addElement(reply);
                            //changes start here!!!!
                            Location proposal = null;
                            try {
                                proposal = (Location)(msg.getContentObject());
                                double distance = proposal.getDistance(position);
                                if (distance < bestProposal) {
                                    bestProposal = distance;
                                    bestProposer = msg.getSender();
                                    accept = reply;
                                }
                            } catch (UnreadableException unreadableException) {
                                unreadableException.printStackTrace();
                            }
                        }
                    }
                    // Accept the proposal of the best proposer
                    if (accept != null) {
                        System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
                        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    }
                }

                protected void handleInform(ACLMessage inform) {
                    System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
                }
            } );
        }
        else {
            System.out.println("No responder specified.");
        }

    }
    
}
