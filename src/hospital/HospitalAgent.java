package hospital;

import sajas.core.Agent;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.AgentType;
import utils.Location;
import utils.Logger;
import injury.InjuryType;
import utils.NodeGenerator;

import java.util.Arrays;
import java.util.EnumMap;

public class HospitalAgent extends Agent {

    private Location location;
    private int numberOfPatients;
    private int capacity;
    private EnumMap<InjuryType, Integer> levelOfCompetence;
    private DefaultDrawableNode node;

    public HospitalAgent() {}

    public HospitalAgent(Object[] args) {
        this.setArguments(args);
        String[] argsString = Arrays.copyOf(args, args.length, String[].class);
        String x = argsString[0], y = argsString[1];
        this.location = new Location(Integer.parseInt(x), Integer.parseInt(y));
    }

    public Location getLocation() {
        return location;
    }

    public boolean isFull() {
        return this.numberOfPatients == this.capacity;
    }

    public Integer getLevelOfCompetenceForInjuryType(InjuryType injuryType){
        if (!levelOfCompetence.containsKey(injuryType))
            return 0;
        return levelOfCompetence.get(injuryType);
    }

    public DefaultDrawableNode getNode() {
        return node;
    }

    public void setNode(DefaultDrawableNode node) {
        this.node = node;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        this.numberOfPatients = Integer.parseInt(args[2]);
        this.capacity = Integer.parseInt(args[3]);
        this.levelOfCompetence = new EnumMap<>(InjuryType.class);
        for (int i = 4; i + 1 < args.length; i += 2) {
            String arg = args[i];
            InjuryType type = InjuryType.valueOf(arg);
            Integer level = Integer.parseInt(args[i+1]);
            this.levelOfCompetence.put(type, level);
        }

        if (!this.dfRegister()) {
            String logMessage = getLocalName() + ": " +
                    " unsuccessful DFRegister";
            Logger.writeLog(logMessage, Logger.HOSPITAL);
        }

        addBehaviour(new HospitalNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));

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
        String logMessage;
        try {
            DFService.deregister(this);
            logMessage = getLocalName() + ": shutting down";
        } catch(FIPAException e) {
            e.printStackTrace();
            logMessage = getLocalName() + ": " +
                    "tried to shut down but DFService did not reply";
        }
        Logger.writeLog(logMessage, Logger.HOSPITAL);
    }

    protected boolean performAction() {
        if (this.numberOfPatients == this.capacity)
            return false;

        this.numberOfPatients++;
        return true;
    }

    public int patientSuitability(InjuryType injuryType) { // Lower is better
        int suitability;
        if (this.numberOfPatients >= this.capacity || this.getLevelOfCompetenceForInjuryType(injuryType) == 0) {
            suitability = 100;
        } else {
            // Calculate suitability estimation
            double patientsOverCapacity = (double) numberOfPatients / (double) capacity;
            double fineRatio = 0.75;
            double factor = Math.min(1.0, fineRatio / patientsOverCapacity);

            suitability = (int) (this.getLevelOfCompetenceForInjuryType(injuryType) * factor);
        }
        return 100 - suitability;
    }
}
