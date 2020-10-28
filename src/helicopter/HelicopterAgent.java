package helicopter;

import jade.core.Agent;
import utils.Location;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import java.io.IOException;

public class HelicopterAgent extends Agent {

    private Location location;

    public void setup(String[] args) {
        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        System.out.println("Agent "+getLocalName()+" waiting for CFP...");
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );

        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());
                // We provide a proposal
                Location proposal = location;
                System.out.println("Agent " + getLocalName() + ": Proposing " + proposal);
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
                System.out.println("Agent "+getLocalName()+": Proposal accepted");
                if (performAction()) {
                    System.out.println("Agent "+getLocalName()+": Action successfully performed");
                    ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    return inform;
                }
                else {
                    System.out.println("Agent "+getLocalName()+": Action execution failed");
                    throw new FailureException("unexpected-error");
                }
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                System.out.println("Agent "+getLocalName()+": Proposal rejected");
            }
        } );
    }

    // TO DO  - wtf goes in here?
    private boolean performAction() {
        return true;
    }
}
