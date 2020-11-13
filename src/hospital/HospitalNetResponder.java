package hospital;

import injury.InjuryType;
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

import java.io.IOException;

public class HospitalNetResponder extends ContractNetResponder {

    private final HospitalAgent hospital;

    public HospitalNetResponder(HospitalAgent hospital, MessageTemplate mt) {
        super(hospital, mt);
        this.hospital = hospital;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        String logMessage = hospital.getLocalName() + ": CFP received from [ " +
                cfp.getSender().getName() + " ] , Action is [ " +
                cfp.getContent() + " ]";
        Logger.writeLog(logMessage, "Hospital");

        //TODO - decent try catch
        InjuryType injuryType = null;
        try {
            injuryType = (InjuryType) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            throw new NotUnderstoodException("Cound't get injury type");
        }

        int suitability = this.hospital.patientSuitability(injuryType);
        if (suitability == 0)
            throw new RefuseException("Currently full");

        // We provide a proposal -> Add suitability
        Location location = hospital.getLocation();
        Integer levelOfCompetence = hospital.getLevelOfCompetenceForInjuryType(injuryType);
        HospitalProposal proposal = new HospitalProposal(location, levelOfCompetence);

        logMessage = hospital.getLocalName() + ": proposing [ " + proposal + " ]";
        Logger.writeLog(logMessage, "Hospital");

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
        String logMessage = hospital.getLocalName() + ": Proposal accepted";
        Logger.writeLog(logMessage, "Hospital");

        if (hospital.performAction()) {
            logMessage = hospital.getLocalName() + ": Action successfully performed";
            Logger.writeLog(logMessage, "Hospital");

            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            logMessage = hospital.getLocalName() + ": Action execution failed";
            Logger.writeLog(logMessage, "Hospital");
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        String logMessage = hospital.getLocalName() + ": Proposal rejected";
        Logger.writeLog(logMessage, "Hospital");
    }
}
