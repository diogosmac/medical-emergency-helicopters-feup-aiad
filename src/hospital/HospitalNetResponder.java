package hospital;

import helicopter.HelicopterAgent;
import injury.Injury;
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

import java.io.IOException;

public class HospitalNetResponder extends ContractNetResponder {

    private HospitalAgent hospital;

    public HospitalNetResponder(HospitalAgent hospital, MessageTemplate mt) {
        super(hospital, mt);
        this.hospital = hospital;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        //TODO change logger accordingly
        System.out.println("Agent " + hospital.getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

        //TODO - decent try catch
        InjuryType injuryType = null;
        try {
            injuryType = (InjuryType) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        // We provide a proposal
        Location location = hospital.getLocation();
        Integer levelOfCompetence = hospital.getLevelOfCompetenceForInjuryType(injuryType);
        HospitalProposal proposal = new HospitalProposal(location, levelOfCompetence);

        //TODO change logger accordingly
        System.out.println("Agent " + hospital.getLocalName() + ": Proposing " + proposal);
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
        //TODO change logger accordingly
        System.out.println("Agent " + hospital.getLocalName() + ": Proposal accepted");
        if (hospital.performAction()) {
            //TODO change logger accordingly
            System.out.println("Agent " + hospital.getLocalName() + ": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            //TODO change logger accordingly
            System.out.println("Agent " + hospital.getLocalName() + ": Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        //TODO change logger accordingly
        System.out.println("Agent " + hospital.getLocalName() + ": Proposal rejected");
    }
}
