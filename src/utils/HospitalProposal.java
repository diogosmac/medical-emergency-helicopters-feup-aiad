package utils;

public class HospitalProposal implements java.io.Serializable {

    private final Location location;
    private final Integer suitability;

    public HospitalProposal(Location location, Integer suitability){
        this.location = location;
        this.suitability = suitability;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getSuitability() {
        return suitability;
    }

    @Override
    public String toString() {
        return "HospitalProposal { " +
                "location=" + location.toString() +
                ", suitability=" + suitability.toString() +
                " }";
    }
}
