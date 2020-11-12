package helicopter;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import utils.Location;

import java.io.IOException;

public class EmergencyCallBehaviour extends ContractNetResponder {
    public EmergencyCallBehaviour(Agent agent, MessageTemplate messageTemplate) {
        super(agent, messageTemplate);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        System.out.println("Agent " + this.getAgent().getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());
        // We provide a proposal
        Location proposal = ((HelicopterAgent) this.getAgent()).getLocation();
        System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposing " + proposal);
        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        //TO DO - decent try catch
        try {
            propose.setContentObject(proposal);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propose;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        System.out.println("Agent "+this.getAgent().getLocalName()+": Proposal accepted");
        if (((HelicopterAgent)this.getAgent()).performAction()) {
            System.out.println("Agent "+this.getAgent().getLocalName()+": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            System.out.println("Agent "+this.getAgent().getLocalName()+": Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent "+this.getAgent().getLocalName()+": Proposal rejected");
    }
}
