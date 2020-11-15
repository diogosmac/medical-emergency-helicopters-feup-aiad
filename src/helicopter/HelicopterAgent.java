package helicopter;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;
import utils.AgentType;
import utils.Location;
import injury.Injury;
import injury.InjuryType;

import java.util.ArrayList;
import java.util.Arrays;

public class HelicopterAgent extends Agent {

    private Location location;
    private int radius;
    private ArrayList<AID> responders = new ArrayList<>();
    private Injury patientInjury;
    private boolean busy;

    public Location getLocation() {
        return location;
    }

    public ArrayList<AID> getResponders(){
        return responders;
    }

    public InjuryType getPatientInjuryType() {
        return patientInjury.getType();
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        this.radius = Integer.parseInt(args[2]);

        String logMessage = getLocalName() + ": " +
                "waiting for CFP ...";
        Logger.writeLog(logMessage, Logger.HELICOPTER);

        if (!this.dfRegister()) {
            logMessage = getLocalName() + ": " +
                    " unsuccessful DFRegister";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
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
            for (DFAgentDescription dfAgentDescription : result) {
                String logMessage = getLocalName() + ": " +
                        "found [ " + dfAgentDescription.getName().getLocalName() + " ]";
                Logger.writeLog(logMessage, Logger.HELICOPTER);
                // Add to list and/to initiate ContractNet to each one of them
                responders.add(dfAgentDescription.getName());
            }
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }

        return true;
    }

    protected void takeDown() {
        String logMessage;
        try {
            DFService.deregister(this);
            logMessage = getLocalName() + ": shutting down";
        } catch(FIPAException e) {
            e.printStackTrace();
            logMessage = getLocalName() + ": " +
                    "tried to shut down but DFService did not reply";
        }
        Logger.writeLog(logMessage, Logger.HELICOPTER);
    }

    protected boolean performAction(Injury injury) {
        if (injury == null)
            return false;

        this.patientInjury = injury;
        this.busy = true;

        this.dfSearch();

        if (responders != null && responders.size() > 0) {
            int nResponders = responders.size();
            String logMessage = getAID().getLocalName() + ": " +
                    "trying to delegate action [ treat-my-patient ]" +
                    " to one of " + nResponders + " hospitals";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
            addBehaviour(new HelicopterNetInitiator(this, nResponders, new ACLMessage(ACLMessage.CFP)));
        }
        else {
            String logMessage = getLocalName() + ": " +
                    "no responder specified";
            Logger.writeLog(logMessage, Logger.HELICOPTER);
        }

        return true;
    }

    public boolean isInArea(Location patientLocation) {
        double euclideanDistance = this.getLocation().getDistance(patientLocation);
        return (euclideanDistance <= (double) radius);
    }

    protected int hospitalEvaluation(double distance, Integer levelOfCompetence){
        int distanceWeight = patientInjury.getSeverity();
        int competenceWeight = 100 - distanceWeight;

        double eval = distanceWeight * distance + competenceWeight * levelOfCompetence.doubleValue();
        return (int) Math.ceil(eval / 100);
    }

}
