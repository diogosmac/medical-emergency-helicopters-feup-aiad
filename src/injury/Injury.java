package injury;

public class Injury {

    private final InjuryType type;
    private final int severity;

    Injury(InjuryType type, int severity) {
        this.type = type;
        this.severity = severity;
    }

    public InjuryType getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

}
