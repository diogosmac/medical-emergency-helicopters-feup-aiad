package patient;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.Location;
import utils.Logger;

import java.io.IOException;
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

        boolean validCFPContent = true;
        try {
            cfp.setContentObject(this.patient.getPosition());
        } catch (IOException e) {
            // TODO: log the exception as we do right now? or add no receivers?
            validCFPContent = false;
        }
        for (AID responder : patient.getResponders()) {
            cfp.addReceiver(responder);

            String logMessage;
            if (validCFPContent) {
                logMessage = patient.getLocalName() + ": " +
                        "sending CFP [ " + patient.getPosition() + " ] " +
                        "to agent [ " + responder.getLocalName() + " ]";
            } else {
                logMessage = patient.getLocalName() + ": " +
                        "sending CFP with UNREADABLE content (IOException) " +
                        "to agent [ " + responder.getLocalName() + " ]";
            }
            Logger.writeLog(logMessage, Logger.PATIENT);
        }
        v.add(cfp);

        return v;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        String logMessage;
        try {
            logMessage = patient.getLocalName() + ": " +
                    "received proposal [ " + propose.getContentObject() + " ] " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        } catch (UnreadableException e) {
            logMessage = patient.getLocalName() + ": " +
                    "received UNREADABLE proposal " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        }
        Logger.writeLog(logMessage, Logger.PATIENT);
    }

    protected void handleRefuse(ACLMessage refuse) {
        String logMessage = patient.getLocalName() + ": " +
                "received proposal refusal " +
                "from agent [ " + refuse.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, Logger.PATIENT);
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            String logMessage = patient.getLocalName() + ": " +
                    "JADE runtime error [ " + failure.getSender().getLocalName() + " ] - " +
                    "receiver does not exist";
            Logger.writeLog(logMessage, Logger.PATIENT);
        }
        else {
            String logMessage = patient.getLocalName() + ": " +
                    "received failure " +
                    "from [ " + failure.getSender().getLocalName() + " ]";
            Logger.writeLog(logMessage, Logger.PATIENT);
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
            Logger.writeLog(logMessage, Logger.PATIENT);
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
                    String logMessage = patient.getLocalName() + ": " +
                            "ignoring UNREADABLE proposal " +
                            "from agent [ " + msg.getSender().getLocalName() + " ]";
                    Logger.writeLog(logMessage, Logger.PATIENT);
                }
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            String logMessage = patient.getLocalName() + ": " +
                    "accepting proposal [ " + bestProposal + " ] " +
                    "from responder [ " + bestProposer.getLocalName() + " ]";
            Logger.writeLog(logMessage, Logger.PATIENT);

            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

            try {
                accept.setContentObject(patient.getInjury());
            } catch (IOException ioException) {
                logMessage = patient.getLocalName() + ": " +
                        "couldn't send injury " +
                        "to responder [ " + bestProposer.getLocalName() + " ] , " +
                        "and it will fail to perform the action";
                Logger.writeLog(logMessage, Logger.PATIENT);
            }
        }
    }

    protected void handleInform(ACLMessage inform) {
        String logMessage = patient.getLocalName() + ": " +
                "requested action successfully performed " +
                "by [ " + inform.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, Logger.PATIENT);
    }
}
