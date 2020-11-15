package helicopter;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import utils.Location;
import utils.Logger;
import injury.Injury;

import java.io.IOException;

public class HelicopterNetResponder  extends ContractNetResponder {

    private final HelicopterAgent helicopter;

    public HelicopterNetResponder(HelicopterAgent helicopter, MessageTemplate mt) {
        super(helicopter, mt);
        this.helicopter = helicopter;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        String logMessage;
        try {
            logMessage = helicopter.getLocalName() + ": " +
                    "CFP received from [ " + cfp.getSender().getLocalName() + " ] , " +
                    "Location is [ " + cfp.getContentObject() + " ]";
        } catch (UnreadableException e) {
            logMessage = helicopter.getLocalName() + ": " +
                    "CFP received from [ " + cfp.getSender().getLocalName() + " ] , " +
                    "UNREADABLE Location";
        }
        Logger.writeLog(logMessage, "Helicopter");
        Location patientLocation;
        try {
             patientLocation = (Location) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            throw new NotUnderstoodException("Couldn't understand location!");
        }

        if (!this.helicopter.isInArea(patientLocation)) { // Switch this
            throw new RefuseException("Out of my area!");
        } else if (this.helicopter.isBusy()) {
            throw new RefuseException("Busy!");
        }

        // We provide a proposal
        Location proposal = helicopter.getLocation();

        logMessage = helicopter.getLocalName() + ": proposing [ " + proposal + " ]";
        Logger.writeLog(logMessage, "Helicopter");

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);

        //TODO - decent try catch
        try {
            propose.setContentObject(proposal);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propose;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        String logMessage;
        try {
            logMessage = helicopter.getLocalName() + ": " +
                    "accepted proposal [ " + propose.getContentObject() + " ] " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        } catch (UnreadableException e) {
            logMessage = helicopter.getLocalName() + ": " +
                    "accepted UNREADABLE proposal " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        }
        Logger.writeLog(logMessage, "Helicopter");

        //TODO decent try catch
        Injury injury = null;
        try {
            injury = (Injury) accept.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (helicopter.performAction(injury)) {
            logMessage = helicopter.getLocalName() + ": action successfully performed";
            Logger.writeLog(logMessage, "Helicopter");

            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            logMessage = helicopter.getLocalName() + ": action execution failed";
            Logger.writeLog(logMessage, "Helicopter");
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        String logMessage = helicopter.getLocalName() + ": " +
                "proposal rejected " +
                "by agent [ " + reject.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, "Helicopter");
    }
}
