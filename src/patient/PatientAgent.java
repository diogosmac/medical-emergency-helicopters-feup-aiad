package patient;

import injury.Injury;
import jade.core.Agent;
import jade.lang.acl.UnreadableException;
import utils.Location;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;
import utils.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

public class PatientAgent extends Agent {

    private Injury injury;
    private Location position;
    private int nResponders;

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        String x = args[0], y = args[1], injuryType = args[2], injurySeverity = args[3];
        this.injury = new Injury(injuryType, Integer.parseInt(injurySeverity));
        this.position = new Location(Integer.parseInt(x), Integer.parseInt(y));
        /*
          TODO:
          - sends message to all helicopters
          - receives reply from helicopters (X time after sending)
          - sends confirmation message to the chosen helicopter
         */

        //copied from basic ContractNetInitiator
        Object[] cniArguments = getArguments();
        if (cniArguments != null && cniArguments.length > 0) {
            nResponders = cniArguments.length;
            String logMessage = getAID().getName() + ": " +
                    " trying to delegate action [ pick-me-up ]" +
                    " to one of " + nResponders + " helicopters";
            Logger.writeLog(logMessage, "Patient");

            // Fill the CFP message
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < cniArguments.length; ++i) {
                msg.addReceiver(new AID((String) cniArguments[i], AID.ISLOCALNAME));
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 10 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            msg.setContent("Send me your location.");

            addBehaviour(new ContractNetInitiator(this, msg) {

                protected void handlePropose(ACLMessage propose, Vector v) {
                    String logMessage = myAgent.getAID().getName() + ": " +
                            " received proposal [" + propose.getContent() +
                            "] from " + propose.getSender().getName();
                    Logger.writeLog(logMessage, "Patient");
                }

                protected void handleRefuse(ACLMessage refuse) {
                    String logMessage = myAgent.getAID().getName() + ": " +
                            " received refusal [" + refuse.getContent() +
                            "] from " + refuse.getSender().getName();
                    Logger.writeLog(logMessage, "Patient");
                }

                protected void handleFailure(ACLMessage failure) {
                    if (failure.getSender().equals(myAgent.getAMS())) {
                        // FAILURE notification from the JADE runtime: the receiver
                        // does not exist
                        String logMessage = myAgent.getAID().getName() + ": " +
                                " responder does not exist";
                        Logger.writeLog(logMessage, "Patient");
                    }
                    else {
                        String logMessage = myAgent.getAID().getName() + ": " +
                                " received failure [" + failure.getContent() +
                                "] from " + failure.getSender().getName();
                        Logger.writeLog(logMessage, "Patient");
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
                        String logMessage = myAgent.getAID().getName() + ": " +
                                " accepting proposal [" + bestProposal +
                                "] from " + bestProposer.getName();
                        Logger.writeLog(logMessage, "Patient");
                        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    }
                }

                protected void handleInform(ACLMessage inform) {
                    String logMessage = myAgent.getAID().getName() + ": " +
                            "Agent " + inform.getSender().getName() + " successfully performed the requested action";
                    Logger.writeLog(logMessage, "Patient");
                }
            } );
        }
        else {
            System.out.println("No responder specified.");
        }

    }
    
}
