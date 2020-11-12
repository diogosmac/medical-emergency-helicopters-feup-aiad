package utils;

import injury.Injury;

public class HospitalProposal implements java.io.Serializable{

    private Location location;
    private Integer levelOfCompetence;

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
