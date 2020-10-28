package utils;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import patient.PatientAgent;

import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static void readPatients(AgentContainer container, JSONObject obj) {
        JSONArray patients = (JSONArray) obj.get("patients");
        AtomicInteger patientID = new AtomicInteger(1);
        patients.iterator().forEachRemaining(element -> {
            JSONObject patient = (JSONObject) element;
            String x, y, injuryType, injurySeverity;
            JSONObject location = (JSONObject) patient.get("location");
            x = location.get("x").toString();
            y = location.get("y").toString();
            JSONObject injury = (JSONObject) patient.get("injury");
            injuryType = injury.get("type").toString();
            injurySeverity = injury.get("severity").toString();
            String[] args = { x, y, injuryType, injurySeverity };
            try {
                PatientAgent agent = new PatientAgent();
                agent.setup(args);
                AgentController pat = container.acceptNewAgent(
                        "patient" + patientID,
                        agent
                );
//                AgentController pat = container.createNewAgent(
//                        "patient" + patientID,
//                        PatientAgent.class.getName(),
//                        args
//                );
                pat.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
            patientID.getAndIncrement();
        });
    }

}
