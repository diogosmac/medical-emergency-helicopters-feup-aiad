package hospital;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import utils.HospitalProposal;
import utils.Location;
import utils.Logger;
import injury.InjuryType;

import java.io.IOException;

public class HospitalNetResponder extends ContractNetResponder {

    private final HospitalAgent hospital;

    public HospitalNetResponder(HospitalAgent hospital, MessageTemplate mt) {
        super(hospital, mt);
        this.hospital = hospital;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        InjuryType injuryType;
        try {
            injuryType = (InjuryType) cfp.getContentObject();
        } catch (UnreadableException e) {
            String logMessage = hospital.getLocalName() + ": " +
                    "CFP received from [ " + cfp.getSender().getLocalName() + " ] , " +
                    "Injury Type is UNREADABLE";
            Logger.writeLog(logMessage, Logger.HOSPITAL);
            throw new NotUnderstoodException("Couldn't get injury type");
        }

        String logMessage = hospital.getLocalName() + ": " +
                "CFP received from [ " + cfp.getSender().getLocalName() + " ] , " +
                "Injury Type is [ " + injuryType + " ]";
        Logger.writeLog(logMessage, Logger.HOSPITAL);

        int suitability = this.hospital.patientSuitability(injuryType);
        if (this.hospital.isFull())
            throw new RefuseException("Currently full");
        else if (suitability == 100) {
            throw new RefuseException("Unavailable Specialty");
        }

        // We provide a proposal -> Add suitability
        Location location = hospital.getLocation();
        HospitalProposal proposal = new HospitalProposal(location, suitability);

        logMessage = hospital.getLocalName() + ": " +
                "proposing [ " + proposal + " ] " +
                "to agent [ " + cfp.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, Logger.HOSPITAL);

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);

        try {
            propose.setContentObject(proposal);
        } catch (IOException e) {
            logMessage = hospital.getLocalName() + ": " +
                    "could not respond with proposal to CFP " +
                    "from [ " + cfp.getSender().getLocalName() + " ]";
            Logger.writeLog(logMessage, Logger.HOSPITAL);
            e.printStackTrace();
        }
        return propose;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        String logMessage;
        try {
            logMessage = hospital.getLocalName() + ": " +
                    "accepted proposal [ " + propose.getContentObject() + " ] " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        } catch (UnreadableException e) {
            logMessage = hospital.getLocalName() + ": " +
                    "accepted UNREADABLE proposal " +
                    "from agent [ " + propose.getSender().getLocalName() + " ]";
        }
        Logger.writeLog(logMessage, Logger.HOSPITAL);

        if (hospital.performAction()) {
            logMessage = hospital.getLocalName() + ": patient will be treated (action successful)";
            Logger.writeLog(logMessage, Logger.HOSPITAL);

            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            logMessage = hospital.getLocalName() + ": no capacity for patient (action failed)";
            Logger.writeLog(logMessage, Logger.HOSPITAL);
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        String logMessage = hospital.getLocalName() + ": " +
                "proposal rejected " +
                "by agent [ " + reject.getSender().getLocalName() + " ]";
        Logger.writeLog(logMessage, Logger.HOSPITAL);
    }
}
