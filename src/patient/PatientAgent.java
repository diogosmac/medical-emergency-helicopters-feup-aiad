package patient;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.AgentType;
import utils.Location;
import utils.Logger;
import injury.Injury;

import java.util.ArrayList;
import java.util.Arrays;

public class PatientAgent extends Agent {

    private Injury injury;
    private Location position;
    private int waitPeriod = 0;
    private ArrayList<AID> responders = new ArrayList<>();
    private int numberOfResponders;

    public Injury getInjury() { return injury; }

    public ArrayList<AID> getResponders(){
        return responders;
    }

    public int getNumberOfResponders() {
        return numberOfResponders;
    }

    public Location getPosition() {
        return position;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        String x = args[0], y = args[1], injuryType = args[2], injurySeverity = args[3];
        this.injury = new Injury(injuryType, Integer.parseInt(injurySeverity));
        this.position = new Location(Integer.parseInt(x), Integer.parseInt(y));

        if (args.length > 4) {
            this.waitPeriod = Integer.parseInt(args[4]);
        }

        if (this.dfSearch()) {
            numberOfResponders = responders.size();

            String logMessage = getAID().getLocalName() + ": " +
                    "trying to delegate action [ pick-me-up ]" +
                    " to one of " + numberOfResponders + " helicopters";
            Logger.writeLog(logMessage, "Patient");

            if (this.waitPeriod == 0) {
                addBehaviour(new PatientNetInitiator(this, numberOfResponders, new ACLMessage(ACLMessage.CFP)));
            }
            else {
                logMessage = getLocalName() + ": " +
                        "waiting [ " + waitPeriod + " ] seconds before sending out request";
                Logger.writeLog(logMessage, "Patient");
                addBehaviour(new PatientWaitBehaviour(this, this.waitPeriod * 1000));
            }
        }
        else {
            String logMessage = getLocalName() + ": " +
                    "no responder specified";
            Logger.writeLog(logMessage, Logger.PATIENT);
        }

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
        Logger.writeLog(logMessage, Logger.PATIENT);
    }

    private boolean dfSearch() {
        responders = new ArrayList<>();
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(AgentType.HELICOPTER);
        template.addServices(serviceDescription);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription dfAgentDescription : result) {
                String logMessage = getLocalName() + ": " +
                        "found [ " + dfAgentDescription.getName().getLocalName() + " ]";
                Logger.writeLog(logMessage, Logger.PATIENT);
                // Add to list and/to initiate ContractNet to each one of them
                responders.add(dfAgentDescription.getName());
            }
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }

        return responders != null && responders.size() > 0;
    }
    
}
