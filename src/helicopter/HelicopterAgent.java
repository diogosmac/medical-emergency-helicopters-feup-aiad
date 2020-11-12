package helicopter;

import hospital.HospitalAgent;
import injury.InjuryType;
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

        //TODO change logger accordingly
        System.out.println("Agent "+getLocalName()+" waiting for CFP...");

        addBehaviour(new HelicopterNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
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
