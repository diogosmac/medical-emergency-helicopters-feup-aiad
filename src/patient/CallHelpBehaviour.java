package patient;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.Location;

import java.util.Enumeration;
import java.util.Vector;

public class CallHelpBehaviour extends ContractNetInitiator {
    public CallHelpBehaviour(Agent agent, ACLMessage cfp) {
        super(agent, cfp);
    }

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
        ((PatientAgent) this.getAgent()).setnResponders((((PatientAgent) this.getAgent()).getnResponders() - 1));
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < ((PatientAgent) this.getAgent()).getnResponders()) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing "+(((PatientAgent) this.getAgent()).getnResponders() - responses.size())+" responses");
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
                    double distance = proposal.getDistance(((PatientAgent) this.getAgent()).getPosition());
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
}
