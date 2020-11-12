package helicopter;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import utils.AgentType;
import utils.Location;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import utils.Logger;

import java.io.IOException;
import java.util.Arrays;

public class HelicopterAgent extends Agent {

    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        String logMessage = getLocalName() + ": " +
                " waiting for CFP ...";
        Logger.writeLog(logMessage, "Helicopter");

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );

        if (!this.dfRegister()) {
            //  log unsuccessful dfregister
        }

        // Maybe use EmergencyCallBehaviour class?
        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                String logMessage = getLocalName() + ": " +
                        " received CFP [" + cfp.getContent() +
                        "] from " + cfp.getSender().getName();
                Logger.writeLog(logMessage, "Helicopter");

                // We provide a proposal
                Location proposal = location;
                logMessage = getLocalName() + ": " +
                        " sending proposal [" + proposal + "]";
                Logger.writeLog(logMessage, "Helicopter");

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
                String logMessage = getLocalName() + ": " +
                        " accepted proposal";
                Logger.writeLog(logMessage, "Helicopter");
                if (performAction()) {
                    logMessage = getLocalName() + ": " +
                            " performed action successfully";
                    Logger.writeLog(logMessage, "Helicopter");

                    ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    return inform;
                }
                else {
                    logMessage = getLocalName() + ": " +
                        " failed action execution";
                    Logger.writeLog(logMessage, "Helicopter");
                    throw new FailureException("unexpected-error");
                }
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                String logMessage = getLocalName() + ": " +
                        " proposal rejected";
                Logger.writeLog(logMessage, "Helicopter");
            }
        } );
    }

    // TO DO  - wtf goes in here? -> this should trigger the "call to hospitals" behaviour
    public boolean performAction() {
        return true;
    }

    private boolean dfRegister() {
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(AgentType.HELICOPTER);
        serviceDescription.setName(getLocalName());
        dfAgentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, dfAgentDescription);
        } catch(FIPAException fe) {
            fe.printStackTrace();
            return false;
        }

        return true;
    }

    // Add a subscription?? (be notified when there's a new hospital)
    private boolean dfSearch() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(AgentType.HOSPITAL);
        template.addServices(serviceDescription);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for(int i=0; i<result.length; ++i) {
                System.out.println("Found " + result[i].getName());
                // Add to list and/to initiate ContractNet to each one of them
            }
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }

        return true;
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
        // Log end of service
    }

}
