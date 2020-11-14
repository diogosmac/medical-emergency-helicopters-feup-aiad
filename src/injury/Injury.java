package injury;

public class Injury implements java.io.Serializable {

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

    @Override
    public String toString() {
        return "Injury { " +
                "type=" + type +
                ", severity=" + severity +
                " }";
    }
}
