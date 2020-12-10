package utils;

import jade.core.AID;

import java.io.Serializable;

public class PatientAccepted implements Serializable {
    private AID patient;

    public PatientAccepted(AID patient){
        this.patient = patient;
    };

    public AID getPatient() {
        return patient;
    }
}
