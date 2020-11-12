package patient;

import injury.Injury;
import jade.core.Agent;
import utils.Location;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;

public class PatientAgent extends Agent {

    private String id;
    private Injury injury;
    private Location position;
    private Object[] responders;

    public String getId() {
        return id;
    }

    public Location getPosition() {
        return position;
    }

    public Injury getInjury() {
        return injury;
    }

    public Object[] getResponders(){
        return responders;
    }

    public void setup() {

        Object[] objArgs = getArguments();
        String[] args = Arrays.copyOf(objArgs, objArgs.length, String[].class);

        String x = args[0], y = args[1], injuryType = args[2], injurySeverity = args[3];
        this.injury = new Injury(injuryType, Integer.parseInt(injurySeverity));
        this.position = new Location(Integer.parseInt(x), Integer.parseInt(y));
        /*
          TODO:
          - sends message to all helicopters
          - receives reply from helicopters (X time after sending)
          - sends confirmation message to the chosen helicopter
         */

        responders = getArguments();
        if (responders != null && responders.length > 0) {
            int nResponders = responders.length;
            System.out.println("Trying to delegate pick-me-up action to one out of " + nResponders + " helicopters.");
            addBehaviour(new PatientSendRequestHelicopters(this, nResponders, new ACLMessage(ACLMessage.CFP)));
        }
        else {
            System.out.println("No responder specified.");
        }

    }
    
}
