package utils;

import jade.core.AID;

import java.io.Serializable;

public class PatientFinished implements Serializable {
    private AID patient;
    private Integer hospitalSuitability;

    public PatientFinished(AID patient, Integer hospitalSuitability){
        this.patient = patient;
        this.hospitalSuitability = hospitalSuitability;
    }

    public AID getPatient() {
        return patient;
    }

    public Integer getHospitalSuitability() {
        return hospitalSuitability;
    }
}
