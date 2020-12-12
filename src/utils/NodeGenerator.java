package utils;

import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import patient.PatientAgent;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.gui.RectNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.awt.*;
import java.util.List;

public class NodeGenerator {
    public static void generateNodes(java.util.List<HospitalAgent> hospitalAgents, java.util.List<HelicopterAgent> helicopterAgents, List<PatientAgent> patientAgents) {
        System.out.println(hospitalAgents);
        System.out.println(helicopterAgents);
        System.out.println(patientAgents);

        for (HospitalAgent hospital: hospitalAgents) {

            generateHospitalNode(hospital);
        }
        for (HelicopterAgent helicopter: helicopterAgents) {
            generateHelicopterNode(helicopter);
        }
        for (PatientAgent patient: patientAgents) {
            generatePatientNode(patient);
        }
    }

    private static DefaultDrawableNode generatePatientNode(PatientAgent patient) {
        OvalNetworkItem oval = new OvalNetworkItem(patient.getPosition().getX()*10, patient.getPosition().getY()*10);
        oval.allowResizing(false);
        oval.setHeight(50);
        oval.setWidth(50);

        DefaultDrawableNode node = new DefaultDrawableNode(patient.getLocalName(), oval);
        node.setColor(new Color(0,0,255));

        patient.setNode(node);

        return node;
    }

    private static DefaultDrawableNode generateHelicopterNode(HelicopterAgent helicopter) {
        RectNetworkItem rect = new RectNetworkItem(helicopter.getLocation().getX()*10, helicopter.getLocation().getY()*10);
        rect.allowResizing(false);
        rect.setHeight(50);
        rect.setWidth(50);

        DefaultDrawableNode node = new DefaultDrawableNode(helicopter.getLocalName(), rect);
        node.setColor(new Color(0,255,0));

        helicopter.setNode(node);

        return node;
    }

    private static DefaultDrawableNode generateHospitalNode(HospitalAgent hospital) {
        OvalNetworkItem oval = new OvalNetworkItem(hospital.getLocation().getX()*10, hospital.getLocation().getY()*10);
        oval.allowResizing(false);
        oval.setHeight(50);
        oval.setWidth(50);

        DefaultDrawableNode node = new DefaultDrawableNode(hospital.getLocalName(), oval);
        node.setColor(new Color(255,0,0));

        hospital.setNode(node);

        return node;
    }
}
