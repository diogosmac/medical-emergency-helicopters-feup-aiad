package utils;

import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class ScenarioReader {

    public static void readScenario(AgentContainer container, String filename) throws StaleProxyException {
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(new FileReader(filename));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        readHospitals(container, obj);
        readHelicopters(container, obj);
        readPatients(container, obj);
    }

    private static void readHospitals(AgentContainer container, JSONObject obj) throws StaleProxyException {
        JSONArray hospitals = (JSONArray) obj.get("hospitals");
//        AgentController hos = container.createNewAgent("hospital", "HospitalAgent", null);
//        hos.start();
    }

    private static void readHelicopters(AgentContainer container, JSONObject obj) throws StaleProxyException {
        JSONArray helicopters = (JSONArray) obj.get("helicopters");
//        AgentController hel = container.createNewAgent("helicopter", "PatientAgent", null);
//        hel.start();
    }

    private static void readPatients(AgentContainer container, JSONObject obj) throws StaleProxyException {
        JSONArray patients = (JSONArray) obj.get("patients");
//        AgentController pat = container.createNewAgent("patient", "PatientAgent", null);
//        pat.start();
    }

}
