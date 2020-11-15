package helicopter;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.HospitalProposal;
import utils.Location;
import utils.Logger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class HelicopterNetInitiator extends ContractNetInitiator {

    private final HelicopterAgent helicopter;
    private int numberOfResponders;
    private final Location patientLocation;
    private Location hospitalLocation;

    public HelicopterNetInitiator(HelicopterAgent helicopter, int numberOfResponders, Location patientLocation, ACLMessage cfp) {
        super(helicopter, cfp);
        this.helicopter = helicopter;
        this.numberOfResponders = numberOfResponders;
        this.patientLocation = patientLocation;
    }

    protected Vector prepareCfps(ACLMessage cfp) {
        Vector v = new Vector();

        boolean validCFPContent = true;
        try {
            cfp.setContentObject(helicopter.getPatientInjuryType());
        } catch (IOException e) {
            // TODO: log the exception as we do right now? or add no receivers?
            validCFPContent = false;
        }

        for (AID responder : helicopter.getResponders()) {
            cfp.addReceiver(responder);

            String logMessage;
            if (validCFPContent) {
                logMessage = helicopter.getLocalName() + ": " +
                        "sending CFP [ " + helicopter.getPatientInjuryType() + " ] " +
                        "to agent [ " + responder.getLocalName() + " ]";
            } else {
                logMessage = helicopter.getLocalName() + ": " +
                        "sending CFP with UNREADABLE content (IOException) " +
                        "to agent [ " + responder.getLocalName() + " ]";
            }
            Logger.writeLog(logMessage, Logger.HELICOPTER);
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
        Logger.writeLog(logMessage, Logger.HELICOPTER);
    }

    protected void handleRefuse(ACLMessage refuse) {
        String logMessage = helicopter.getLocalName() + ": " +
                "proposal was refused " +
                "by agent [ " + refuse.getSender().getLocalName() + " ] , " +
                "for reason [ " + refuse.getContent() + " ]";
        Logger.writeLog(logMessage, Logger.HELICOPTER);
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            String logMessage = helicopter.getLocalName() + ": " +
                    "JADE runtime error [ " + failure.getSender().getLocalName() + " ] - " +
                    "receiver does not exist";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
        }
        else {
            String logMessage = helicopter.getLocalName() + ": " +
                    "received failure " +
                    "from [ " + failure.getSender().getLocalName() + " ]";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
        }
        // Immediate failure --> we will not receive a response from this agent
        numberOfResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < numberOfResponders) {
            // Some responder didn't reply within the specified timeout
            String logMessage = helicopter.getLocalName() + ": " +
                    "timeout expired, missing " +
                    (numberOfResponders - responses.size()) + "responses";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
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
                    String logMessage = helicopter.getLocalName() + ": " +
                            "ignoring UNREADABLE proposal " +
                            "from agent [ " + msg.getSender().getLocalName() + " ]";
                    Logger.writeLog(logMessage, Logger.HELICOPTER);
                }
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            String logMessage = helicopter.getLocalName() + ": " +
                    "accepting proposal [ " + bestProposal + " ] " +
                    "from responder [ " + bestProposer.getLocalName() + " ]";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        } else {
            this.helicopter.setBusy(false);
        }
    }

    protected void handleInform(ACLMessage inform) {
        String logMessage = helicopter.getLocalName() + ": " +
                "requested action successfully performed " +
                "by [ " + inform.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, Logger.HELICOPTER);

        double distanceToPatient = helicopter.getLocation().getDistance(patientLocation);
        double distanceToHospital = patientLocation.getDistance(hospitalLocation);
        double distanceToReturn = hospitalLocation.getDistance(helicopter.getLocation());
        double totalDistance = distanceToPatient + distanceToHospital + distanceToReturn;
        double travelTime = totalDistance / helicopter.getSpeed();
        logMessage = helicopter.getLocalName() + ": " +
                "starting voyage, " +
                "at our speed of [ " + helicopter.getSpeed() + " ] , " +
                "this will take about [ " + travelTime + " ] seconds";
        Logger.writeLog(logMessage, Logger.HELICOPTER);
        int travelTimeMillis = (int) travelTime * 1000;
        helicopter.addBehaviour(
                new HelicopterTravelingBehaviour(helicopter, travelTimeMillis));
    }
}
