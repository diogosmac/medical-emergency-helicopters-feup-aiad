package patient;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.Location;
import utils.Logger;

import java.util.Enumeration;
import java.util.Vector;

public class PatientNetInitiator extends ContractNetInitiator {

    private final PatientAgent patient;
    private int nResponders;

    public PatientNetInitiator(PatientAgent patient, int nResponders, ACLMessage cfp) {
        super(patient, cfp);
        this.patient = patient;
        this.nResponders = nResponders;
    }

    protected Vector prepareCfps(ACLMessage cfp) {
        Vector v = new Vector();
        cfp.setContent("What is your location?");
        for (int i = 0; i < nResponders; i++) {
            //TODO change this implementation
            cfp.addReceiver(new AID(patient.getName(), false));

            String logMessage = patient.getLocalName() + ": sending CFP to [ " +
                    patient.getResponders()[i] + " ]";
            Logger.writeLog(logMessage, "Patient");
        }
        v.add(cfp);

        return v;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        String logMessage = propose.getSender().getName() + ": " +
                "sending proposal [ " + propose.getContent() + " ]";
        Logger.writeLog(logMessage, "Patient");
    }

    protected void handleRefuse(ACLMessage refuse) {
        String logMessage = refuse.getSender().getName() + ": " +
                "refused proposal";
        Logger.writeLog(logMessage, "Patient");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            String logMessage = failure.getSender().getName() + ": " +
                    "responder does not exist";
            Logger.writeLog(logMessage, "Patient");
        }
        else {
            String logMessage = failure.getSender().getName() + ": failed";
            Logger.writeLog(logMessage, "Patient");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            String logMessage = patient.getLocalName() + ": " +
                    "timeout expired, missing " +
                    (nResponders - responses.size()) + "responses";
            Logger.writeLog(logMessage, "Patient");
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
                Location proposal = null;
                try {
                    proposal = (Location)(msg.getContentObject());
                    double distance = proposal.getDistance(patient.getPosition());
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
            String logMessage = patient.getLocalName() + ": " +
                    "accepting proposal [ " + bestProposal +
                    " ] from responder [ " + bestProposer.getName() + " ]";
            Logger.writeLog(logMessage, "Patient");
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

    protected void handleInform(ACLMessage inform) {
        String logMessage = inform.getSender().getName() + ": " +
                "successfully performed the requested action";
        Logger.writeLog(logMessage, "Patient");
    }
}
