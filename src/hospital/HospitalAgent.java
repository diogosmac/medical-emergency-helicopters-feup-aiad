package hospital;

import injury.InjuryType;
import jade.core.Agent;
import utils.Location;

import java.util.EnumMap;

public class HospitalAgent extends Agent {

    private Location location;
    private EnumMap<InjuryType, Integer> levelOfCompetence;

    public void setup(String[] args) {
        // TODO - read private fields from file
        System.out.println("Yes I am a hospital");
    }

}
