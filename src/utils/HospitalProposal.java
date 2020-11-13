package utils;

import injury.Injury;

public class HospitalProposal implements java.io.Serializable{

    private final Location location;
    private final Integer levelOfCompetence;

    public HospitalProposal(Location location, Integer levelOfCompetence){
        this.location = location;
        this.levelOfCompetence = levelOfCompetence;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getLevelOfCompetence() {
        return levelOfCompetence;
    }
}
