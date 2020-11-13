package patient;

import injury.Injury;
import injury.InjuryType;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.AgentType;
import utils.Location;
import jade.lang.acl.ACLMessage;
import utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;

public class PatientAgent extends Agent {

    private Injury injury;
    private Location position;
    private ArrayList<AID> responders = new ArrayList<>();

    public InjuryType getInjuryType() {
        return injury.getType();
    }

    public ArrayList<AID> getResponders(){
        return responders;
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

        this.dfSearch();

        if (responders != null && responders.size() > 0) {
            int nResponders = responders.size();
            String logMessage = getAID().getName() + ": " +
                    " trying to delegate action [ pick-me-up ]" +
                    " to one of " + nResponders + " helicopters";
            Logger.writeLog(logMessage, "Patient");
            addBehaviour(new PatientNetInitiator(this, nResponders, new ACLMessage(ACLMessage.CFP)));
        }
        else {
            String logMessage = getLocalName() + ": " +
                    " no responder specified";
            Logger.writeLog(logMessage, "Patient");
        }

    }

    private boolean dfSearch() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(AgentType.HELICOPTER);
        template.addServices(serviceDescription);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription dfAgentDescription : result) {
                String logMessage = getLocalName() + ": " +
                        "found [ " + dfAgentDescription.getName() + " ]";
                Logger.writeLog(logMessage, "Patient");
                // Add to list and/to initiate ContractNet to each one of them
                responders.add(dfAgentDescription.getName());
            }
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }

        return true;
    }
    
}
