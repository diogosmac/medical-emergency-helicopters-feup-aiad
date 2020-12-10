package helicopter;

import sajas.proto.ContractNetResponder;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utils.Location;
import utils.Logger;
import injury.Injury;
import utils.PatientAccepted;
import utils.PatientFinished;

import java.io.IOException;

public class HelicopterNetResponder extends ContractNetResponder {

    private final HelicopterAgent helicopter;

    public HelicopterNetResponder(HelicopterAgent helicopter, MessageTemplate mt) {
        super(helicopter, mt);
        this.helicopter = helicopter;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        Location patientLocation;
        helicopter.setPatient(cfp.getSender());

        try {
            patientLocation = (Location) cfp.getContentObject();
            helicopter.setPatientLocation(patientLocation);
        } catch (UnreadableException e) {
            String logMessage = helicopter.getLocalName() + ": " +
                    "CFP received from [ " + cfp.getSender().getLocalName() + " ] , " +
                    "Location is UNREADABLE";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
            throw new NotUnderstoodException("Couldn't understand location");
        }

        String logMessage = helicopter.getLocalName() + ": " +
                    "CFP received from [ " + cfp.getSender().getLocalName() + " ] , " +
                    "Location is [ " + patientLocation + " ]";
        Logger.writeLog(logMessage, Logger.HELICOPTER);

        if (this.helicopter.isBusy()) {
            throw new RefuseException("Busy!");
        } else if (!this.helicopter.isInArea(patientLocation)) {
            throw new RefuseException("Out of my area!");
        }

        // We provide a proposal
        Location proposal = helicopter.getLocation();

        logMessage = helicopter.getLocalName() + ": proposing [ " + proposal + " ]";
        Logger.writeLog(logMessage, Logger.HELICOPTER);

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);

        try {
            propose.setContentObject(proposal);
        } catch (IOException e) {
            logMessage = helicopter.getLocalName() + ": " +
                    "could not respond with proposal to CFP " +
                    "from [ " + cfp.getSender().getLocalName() + " ]";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
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
        Logger.writeLog(logMessage, Logger.HELICOPTER);

        Injury injury;
        try {
            injury = (Injury) accept.getContentObject();
        } catch (UnreadableException e) {
            // error will be handled by making the helicopter fail
            // to perform the action, as there is no valid injury
            injury = null;
        }

        if (helicopter.performAction(injury)) {
            logMessage = helicopter.getLocalName() + ": action successfully performed";
            Logger.writeLog(logMessage, Logger.HELICOPTER);

            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            //inform Results Collector
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            try {
                msg.setContentObject(new PatientAccepted(helicopter.getPatient()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            msg.addReceiver(helicopter.getResultsCollector());
            helicopter.send(msg);

            return inform;
        }
        else {
            logMessage = helicopter.getLocalName() + ": action execution failed";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        String logMessage = helicopter.getLocalName() + ": " +
                "proposal rejected " +
                "by agent [ " + reject.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, Logger.HELICOPTER);
    }
}
