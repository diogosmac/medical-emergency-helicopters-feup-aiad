package hospital;

import injury.InjuryType;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.AgentType;
import utils.Location;
import utils.Logger;

import java.util.Arrays;
import java.util.EnumMap;

public class HospitalAgent extends Agent {

    private Location location;
    private EnumMap<InjuryType, Integer> levelOfCompetence;

    public Location getLocation() {
        return location;
    }

    public Integer getLevelOfCompetenceForInjuryType(InjuryType injuryType){
        return levelOfCompetence.get(injuryType);
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        this.levelOfCompetence = new EnumMap<>(InjuryType.class);
        for (InjuryType type : InjuryType.values()) {
            this.levelOfCompetence.put(type, 50);
        }
        for (int i = 2; i + 1 < args.length; i += 2) {
            String arg = args[i];
            InjuryType type = InjuryType.valueOf(arg);
            Integer level = Integer.parseInt(args[i+1]);
            this.levelOfCompetence.put(type, level);
        }

        if (!this.dfRegister()) {
            String logMessage = getLocalName() + ": " +
                    " unsuccessful DFRegister";
            Logger.writeLog(logMessage, "Hospital");
        }
    }

    private boolean dfRegister() {
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(AgentType.HOSPITAL);
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

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
        // Log end of service
    }

    protected boolean performAction() {
        //TODO  - do something here
        return true;
    }
}
