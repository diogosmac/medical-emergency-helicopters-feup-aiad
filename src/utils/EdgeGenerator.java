package utils;

import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import patient.PatientAgent;
import uchicago.src.sim.network.EdgeFactory;

import java.util.List;

public class EdgeGenerator {

    public static void generateEdges(
            List<HospitalAgent> hospitalAgents, List<HelicopterAgent> helicopterAgents, List<PatientAgent> patientAgents)
    {
        for (HelicopterAgent helicopter: helicopterAgents) {
            for (HospitalAgent hospital: hospitalAgents) {
                EdgeFactory.createDrawableEdge(helicopter.getNode(), hospital.getNode());
            }
        }

        for (PatientAgent patient: patientAgents) {
            for (HelicopterAgent helicopter: helicopterAgents) {
                EdgeFactory.createDrawableEdge(patient.getNode(), helicopter.getNode());
            }
        }
    }
}
