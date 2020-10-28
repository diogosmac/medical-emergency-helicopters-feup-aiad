package hospital;

import injury.InjuryType;
import jade.core.Agent;
import utils.Location;

import java.util.EnumMap;

public class HospitalAgent extends Agent {

    private Location location;
    private EnumMap<InjuryType, Integer> levelOfCompetence;

    public void setup(String[] args) {
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
        System.out.println();
    }

}
