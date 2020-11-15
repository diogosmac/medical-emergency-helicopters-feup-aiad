package utils;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import patient.PatientAgent;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ScenarioReader {

    private static int hospitalID = 1;
    private static int helicopterID = 1;
    private static int patientID = 1;

    public static void readScenario(AgentContainer hospitalContainer, AgentContainer helicopterContainer,
                                    AgentContainer patientContainer, String filename) throws StaleProxyException {
        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            obj = (JSONObject) parser.parse(new FileReader(filename));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        readHospitals(hospitalContainer, obj);

        readHelicopters(helicopterContainer, obj);

        readPatients(patientContainer, obj);
    }

    private static void readHospitals(AgentContainer container, JSONObject obj) throws StaleProxyException {
        JSONArray hospitals = (JSONArray) obj.get("hospitals");
        for (Object o : hospitals) {
            JSONObject hospital = (JSONObject) o;
            String x, y, patients, capacity;
            JSONObject location = (JSONObject) hospital.get("location");
            x = location.get("x").toString();
            y = location.get("y").toString();
            patients = hospital.get("patients").toString();
            capacity = hospital.get("capacity").toString();
            List<String> argList = new ArrayList<>();
            argList.add(x);
            argList.add(y);
            argList.add(patients);
            argList.add(capacity);
            JSONArray specialties = (JSONArray) hospital.get("specialties");
            for (Object spec : specialties) {
                JSONObject specialty = (JSONObject) spec;
                String name, competence;
                name = specialty.get("name").toString();
                competence = specialty.get("competence").toString();
                argList.add(name);
                argList.add(competence);
            }
            String[] args = argList.toArray(String[]::new);
            AgentController hos = container.createNewAgent(
                    "hospital" + hospitalID++,
                    HospitalAgent.class.getName(),
                    args
            );
            hos.start();
        }
    }

    private static void readHelicopters(AgentContainer container, JSONObject obj) throws StaleProxyException {
        JSONArray helicopters = (JSONArray) obj.get("helicopters");
        for (Object o : helicopters) {
            JSONObject helicopter = (JSONObject) o;
            String x, y, radius;
            JSONObject location = (JSONObject) helicopter.get("location");
            x = location.get("x").toString();
            y = location.get("y").toString();
            radius = helicopter.get("radius").toString();
            String[] args;
            if (helicopter.containsKey("speed")) {
                String speed = helicopter.get("speed").toString();
                args = new String[]{ x, y, radius, speed };
            } else args = new String[]{ x, y, radius };
            AgentController hel = container.createNewAgent(
                    "helicopter" + helicopterID++,
                    HelicopterAgent.class.getName(),
                    args
            );
            hel.start();
        }
    }

    private static void readPatients(AgentContainer container, JSONObject obj) throws StaleProxyException{
        JSONArray patients = (JSONArray) obj.get("patients");
        for (Object o : patients) {
            JSONObject patient = (JSONObject) o;
            String x, y, injuryType, injurySeverity, waitPeriod;
            JSONObject location = (JSONObject) patient.get("location");
            x = location.get("x").toString();
            y = location.get("y").toString();
            JSONObject injury = (JSONObject) patient.get("injury");
            injuryType = injury.get("type").toString();
            injurySeverity = injury.get("severity").toString();
            String[] args;
            if (patient.containsKey("wait")) {
                waitPeriod = patient.get("wait").toString();
                args = new String[]{x, y, injuryType, injurySeverity, waitPeriod};
            } else args = new String[]{x, y, injuryType, injurySeverity};
            AgentController pat = container.createNewAgent(
                    "patient" + patientID++,
                    PatientAgent.class.getName(),
                    args
            );
            pat.start();
        }
    }

}
