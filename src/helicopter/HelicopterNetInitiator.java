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

        for (AID responder : helicopter.getResponders()) {
            cfp.addReceiver(responder);

            String logMessage = helicopter.getLocalName() + ": " +
                    "sending CFP [ " + helicopter.getPatientInjuryType() + " ] " +
                    "to agent [ " + responder.getLocalName() + " ]";
            Logger.writeLog(logMessage, "Helicopter");
        }
        v.add(cfp);

        return v;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        String logMessage;
        try {
            logMessage = helicopter.getLocalName() + ": " +
                    "received proposal [ " + propose.getContentObject() + " ] " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        } catch (UnreadableException e) {
            logMessage = helicopter.getLocalName() + ": " +
                    "sending UNREADABLE proposal " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
            e.printStackTrace();
        }
        Logger.writeLog(logMessage, "Helicopter");
    }

    protected void handleRefuse(ACLMessage refuse) {
        String logMessage = helicopter.getLocalName() + ": " +
                "proposal was refused " +
                "by agent [ " + refuse.getSender().getLocalName() + " ] , " +
                "for reason [ " + refuse.getContent() + " ]";
        Logger.writeLog(logMessage, "Helicopter");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            String logMessage = helicopter.getLocalName() + ": " +
                    "JADE runtime error [ " + failure.getSender().getLocalName() + " ] - " +
                    "receiver does not exist";
            Logger.writeLog(logMessage, "Helicopter");
        }
        else {
            String logMessage = helicopter.getLocalName() + ": " +
                    "received failure " +
                    "from [ " + failure.getSender().getLocalName() + " ]";
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
                    Integer suitability = proposal.getSuitability();
                    int hospitalValue = helicopter.hospitalEvaluation(distance, suitability);
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
                    " ] from responder [ " + bestProposer.getLocalName() + " ]";
            Logger.writeLog(logMessage, "Helicopter");
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        } else {
            this.helicopter.setBusy(false);
        }
    }

    protected void handleInform(ACLMessage inform) {
        String logMessage = helicopter.getLocalName() + ": " +
                "requested action successfully performed " +
                "by [ " + inform.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, "Helicopter");

        this.helicopter.setBusy(false);
    }
}
