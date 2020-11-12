package helicopter;

import hospital.HospitalAgent;
import injury.InjuryType;
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

//TODO assign id and responders
public class HelicopterAgent extends Agent {

    private String id;
    private Location location;
    private Object[] responders;
    private InjuryType patientInjuryType;

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public Object[] getResponders(){
        return responders;
    }

    public InjuryType getPatientInjuryType(){
        return patientInjuryType;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        String logMessage = getLocalName() + ": " +
                " waiting for CFP ...";
        Logger.writeLog(logMessage, "Helicopter");

        if (!this.dfRegister()) {
            //  log unsuccessful dfregister
            logMessage = getLocalName() + ": " +
                    " unsuccessful dfregister!!";
            Logger.writeLog(logMessage, "Helicopter");
        }

        addBehaviour(new HelicopterNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));

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


    protected boolean performAction() {
        //TODO  - change this accordingly
        patientInjuryType = InjuryType.HEART;
        addBehaviour(new HelicopterNetInitiator(this, responders.length, new ACLMessage(ACLMessage.CFP)));

        return true;
    }

    //TODO decent utility function
    protected int hospitalEvaluation(double distance, Integer levelOfCompetence){
        return 1;
    }

}
