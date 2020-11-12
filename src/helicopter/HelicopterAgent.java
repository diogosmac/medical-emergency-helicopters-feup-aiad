package helicopter;

import injury.Injury;
import injury.InjuryType;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import utils.AgentType;
import utils.Location;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class HelicopterAgent extends Agent {

    private Location location;
    private ArrayList<AID> responders = new ArrayList<>();
    private InjuryType patientInjuryType;

    public Location getLocation() {
        return location;
    }

    public ArrayList<AID> getResponders(){
        return responders;
    }

    //TODO get corrent patientInjuryType
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
                responders.add(result[i].getName());
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

    protected boolean performAction(InjuryType injuryType) {
        patientInjuryType = injuryType;

        this.dfSearch();

        if (responders != null && responders.size() > 0) {
            int nResponders = responders.size();
            String logMessage = getAID().getName() + ": " +
                    " trying to delegate action [ treat-my-patient ]" +
                    " to one of " + nResponders + " hospitals";
            Logger.writeLog(logMessage, "Patient");
            addBehaviour(new HelicopterNetInitiator(this, nResponders, new ACLMessage(ACLMessage.CFP)));
        }
        else {
            System.out.println("No responder specified.");
        }

        return true;
    }

    //TODO decent utility function
    protected int hospitalEvaluation(double distance, Integer levelOfCompetence){
        return 1;
    }

}
