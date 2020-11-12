package helicopter;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.HospitalProposal;
import utils.Logger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class HelicopterNetInitiator extends ContractNetInitiator {

    private final HelicopterAgent helicopter;
    private int nResponders;

    public HelicopterNetInitiator(HelicopterAgent helicopter, int nResponders, ACLMessage cfp) {
        super(helicopter, cfp);
        this.helicopter = helicopter;
        this.nResponders = nResponders;
    }

    protected Vector prepareCfps(ACLMessage cfp) {
        Vector v = new Vector();
        //TODO - decent try catch
        try {
            cfp.setContentObject(helicopter.getPatientInjuryType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < nResponders; i++) {
            //TODO change this implementation
            cfp.addReceiver(new AID(helicopter.getName(), false));

            String logMessage = helicopter.getLocalName() + ": sending CFP to [ " +
                helicopter.getResponders()[i] + " ]";
            Logger.writeLog(logMessage, "Helicopter");
        }
        v.add(cfp);

        return v;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        String logMessage = propose.getSender().getName() + ": " +
                "sending proposal [ " + propose.getContent() + " ]";
        Logger.writeLog(logMessage, "Helicopter");
    }

    protected void handleRefuse(ACLMessage refuse) {
        String logMessage = refuse.getSender().getName() + ": " +
                "refused proposal";
        Logger.writeLog(logMessage, "Helicopter");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            String logMessage = failure.getSender().getName() + ": " +
                    "responder does not exist";
            Logger.writeLog(logMessage, "Helicopter");
        }
        else {
            String logMessage = failure.getSender().getName() + ": failed";
            Logger.writeLog(logMessage, "Helicopter");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            String logMessage = helicopter.getLocalName() + ": " +
                    "timeout expired, missing " +
                    (nResponders - responses.size()) + "responses";
            Logger.writeLog(logMessage, "Helicopter");
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
                HospitalProposal proposal = null;

                //TODO decent try catch
                try {
                    proposal = (HospitalProposal) (msg.getContentObject());
                    double distance = proposal.getLocation().getDistance(helicopter.getLocation());
                    Integer levelOfCompetence = proposal.getLevelOfCompetence();
                    int hospitalValue = helicopter.hospitalEvaluation(distance, levelOfCompetence);
                    if (hospitalValue < bestProposal) {
                        bestProposal = hospitalValue;
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
            String logMessage = helicopter.getLocalName() + ": " +
                    "accepting proposal [ " + bestProposal +
                    " ] from responder [ " + bestProposer.getName() + " ]";
            Logger.writeLog(logMessage, "Helicopter");
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

    protected void handleInform(ACLMessage inform) {
        String logMessage = inform.getSender().getName() + ": " +
                "successfully performed the requested action";
        Logger.writeLog(logMessage, "Helicopter");
    }
}
