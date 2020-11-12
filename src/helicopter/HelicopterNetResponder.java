package helicopter;

import injury.InjuryType;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import utils.Location;

import java.io.IOException;

public class HelicopterNetResponder  extends ContractNetResponder {

    private HelicopterAgent helicopter;

    public HelicopterNetResponder(HelicopterAgent helicopter, MessageTemplate mt) {
        super(helicopter, mt);
        this.helicopter = helicopter;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        //TODO change logger accordingly
        System.out.println("Agent " + helicopter.getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

        // We provide a proposal
        Location proposal = helicopter.getLocation();
        //TODO change logger accordingly
        System.out.println("Agent " + helicopter.getLocalName() + ": Proposing " + proposal);
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
        System.out.println("Agent " + helicopter.getLocalName() + ": Proposal accepted");

        //TODO decent try catch
        InjuryType injuryType = null;
        try {
            injuryType = (InjuryType) accept.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (helicopter.performAction(injuryType)) {
            //TODO change logger accordingly
            System.out.println("Agent " + helicopter.getLocalName() + ": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            //TODO change logger accordingly
            System.out.println("Agent " + helicopter.getLocalName() + ": Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        //TODO change logger accordingly
        System.out.println("Agent " + helicopter.getLocalName() + ": Proposal rejected");
    }
}
