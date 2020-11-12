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
import java.util.Arrays;

public class HelicopterAgent extends Agent {

    private String id;
    private Location location;

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        this.location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        //TODO change logger accordingly
        System.out.println("Agent "+getLocalName()+" waiting for CFP...");

        addBehaviour(new HelicopterReceivePatientRequest(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }

    // TODO  - wtf goes in here?
    protected boolean performAction() {
        return true;
    }
}
