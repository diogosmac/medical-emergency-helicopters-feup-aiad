package patient;

import injury.Injury;
import jade.core.Agent;
import utils.Location;

public class PatientAgent extends Agent {

    private Injury injury;
    private Location position;

    public void setup() {

        /*
          TODO:
          - read private fields from file
          - sends message to all helicopters
          - receives reply from helicopters (X time after sending)
          - sends confirmation message to the chosen helicopter
         */
        System.out.println("Yes I am a patient");

    }
    
}
