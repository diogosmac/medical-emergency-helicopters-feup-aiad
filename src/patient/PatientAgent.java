package patient;

import injury.Injury;
import jade.core.Agent;
import utils.Location;

public class PatientAgent extends Agent {

    private Injury injury;
    private Location position;

    public void setup(String[] args) {

        String x = args[0], y = args[1], injuryType = args[2], injurySeverity = args[3];
        this.injury = new Injury(injuryType, Integer.parseInt(injurySeverity));
        this.position = new Location(Integer.parseInt(x), Integer.parseInt(y));
        /*
          TODO:
          - sends message to all helicopters
          - receives reply from helicopters (X time after sending)
          - sends confirmation message to the chosen helicopter
         */
        System.out.println("Yes I am a patient");
        System.out.println("I am at location " + this.position.toString());
        System.out.println("My injury is: " + this.injury.toString() + "\n");

    }
    
}
