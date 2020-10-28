package injury;

public class Injury {

    private final InjuryType type;
    private final int severity;

    public Injury(String type, int severity) {
        this.type = InjuryType.valueOf(type);
        this.severity = severity;
    }

    public InjuryType getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public String toString() {
        return type.toString() + " - " + severity;
    }

}
