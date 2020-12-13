package utils;

import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import injury.InjuryType;
import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import sajas.wrapper.ContainerController;
import patient.PatientAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScenarioBuilder {

    private static int hospitalID = 1;
    private static int helicopterID = 1;
    private static int patientID = 1;

    private static AID resultsCollector;
    private static int mapWidth;
    private static int mapLength;

    private static List<HospitalAgent> hospitalAgents;
    private static List<HelicopterAgent> helicopterAgents;
    private static List<PatientAgent> patientAgents;

    public static List<HospitalAgent> getHospitalAgents() { return hospitalAgents; }
    public static List<HelicopterAgent> getHelicopterAgents() { return helicopterAgents; }
    public static List<PatientAgent> getPatientAgents() { return patientAgents; }

    public static void buildScenario(
            ContainerController hospitalContainer, int numHospitals,
            ContainerController helicopterContainer, int numHelicopters,
            ContainerController patientContainer, int numPatients,
            AID resultsCollectorAID,
            int mapWidth, int mapLength,
            int minHospitalCapacity, int maxHospitalCapacity,
            int minHospitalOccupancy, int maxHospitalOccupancy,
            int minPatientSeverity, int maxPatientSeverity,
            int minHelicopterRange, int maxHelicopterRange,
            int minHelicopterSpeed, int maxHelicopterSpeed,
            int minPatientDelay, int maxPatientDelay) throws StaleProxyException {
        ScenarioBuilder.mapWidth = mapWidth;
        ScenarioBuilder.mapLength = mapLength;
        ScenarioBuilder.resultsCollector = resultsCollectorAID;

        hospitalAgents = buildHospitals(
                hospitalContainer, numHospitals,
                minHospitalCapacity, maxHospitalCapacity,
                minHospitalOccupancy, maxHospitalOccupancy);
        helicopterAgents = buildHelicopters(
                helicopterContainer, numHelicopters,
                minHelicopterRange, maxHelicopterRange,
                minHelicopterSpeed, maxHelicopterSpeed);
        patientAgents = buildPatients(
                patientContainer, numPatients,
                minPatientSeverity, maxPatientSeverity,
                minPatientDelay, maxPatientDelay);

        NodeGenerator.generateNodes(hospitalAgents, helicopterAgents, patientAgents);
        EdgeGenerator.generateEdges(hospitalAgents, helicopterAgents, patientAgents);

    }

    private static List<HospitalAgent> buildHospitals(
            ContainerController hospitalContainer, int numHospitals,
            int minHospitalCapacity, int maxHospitalCapacity,
            int minHospitalOccupancy, int maxHospitalOccupancy) throws StaleProxyException {

        List<HospitalAgent> agents = new ArrayList<>();
        Random random = new Random();
        random.setSeed(random.nextLong());

        for (int i = 0; i < numHospitals; i++) {
            String x = Integer.toString(
                    random.nextInt(mapWidth));
            String y = Integer.toString(
                    random.nextInt(mapLength));
            int hosCapacity = random.nextInt(maxHospitalCapacity - minHospitalCapacity + 1) + maxHospitalCapacity;
            int hosPatients = (
                    random.nextInt(maxHospitalOccupancy - minHospitalOccupancy + 1) + maxHospitalOccupancy)
                    * hosCapacity
                    / 100;
            String patients = Integer.toString(hosPatients);
            String capacity = Integer.toString(hosCapacity);

            List<String> argList = new ArrayList<>();
            argList.add(x);
            argList.add(y);
            argList.add(patients);
            argList.add(capacity);
            for (InjuryType type : InjuryType.values()) {
                argList.add(type.toString());
                argList.add("50");
            }
            String[] args = argList.toArray(String[]::new);

            HospitalAgent hos = new HospitalAgent(args);
            agents.add(hos);
            hospitalContainer.acceptNewAgent("hospital" + hospitalID++, hos).start();

        }

        return agents;

    }

    private static List<HelicopterAgent> buildHelicopters(
            ContainerController helicopterContainer, int numHelicopters,
            int minHelicopterRange, int maxHelicopterRange,
            int minHelicopterSpeed, int maxHelicopterSpeed) throws StaleProxyException {

        List<HelicopterAgent> agents = new ArrayList<>();
        Random random = new Random();
        random.setSeed(random.nextLong());

        for (int i = 0; i < numHelicopters; i++) {

            String x = Integer.toString(
                    random.nextInt(mapWidth));
            String y = Integer.toString(
                    random.nextInt(mapLength));
            String radius = Integer.toString(
                    random.nextInt(maxHelicopterRange - minHelicopterRange + 1) + minHelicopterRange);
            String speed = Integer.toString(
                    random.nextInt(maxHelicopterSpeed - minHelicopterSpeed + 1) + minHelicopterSpeed);

            List<String> argList = new ArrayList<>();
            argList.add(x);
            argList.add(y);
            argList.add(radius);
            argList.add(speed);
            String[] args = argList.toArray(String[]::new);

            HelicopterAgent hos = new HelicopterAgent(args);
            hos.setResultsCollector(resultsCollector);
            agents.add(hos);
            helicopterContainer.acceptNewAgent("helicopter" + helicopterID++, hos).start();

        }

        return agents;

    }

    private static List<PatientAgent> buildPatients(
            ContainerController patientContainer, int numPatients,
            int minPatientSeverity, int maxPatientSeverity,
            int minPatientDelay, int maxPatientDelay) throws StaleProxyException {

        List<PatientAgent> agents = new ArrayList<>();
        Random random = new Random();
        random.setSeed(random.nextLong());

        for (int i = 0; i < numPatients; i++) {

            String x = Integer.toString(
                    random.nextInt(mapWidth));
            String y = Integer.toString(
                    random.nextInt(mapLength));
            InjuryType[] vals = InjuryType.values();
            String type = vals[random.nextInt(vals.length)].toString();
            String severity = Integer.toString(
                    random.nextInt(maxPatientSeverity - minPatientSeverity + 1) + minPatientSeverity);
            String delay = Integer.toString(
                    random.nextInt(maxPatientDelay - minPatientDelay + 1) + minPatientDelay);

            List<String> argList = new ArrayList<>();
            argList.add(x);
            argList.add(y);
            argList.add(type);
            argList.add(severity);
            argList.add(delay);
            String[] args = argList.toArray(String[]::new);

            PatientAgent pos = new PatientAgent(args);
            pos.setResultsCollector(resultsCollector);
            agents.add(pos);
            patientContainer.acceptNewAgent("patient" + patientID++, pos).start();

        }

        return agents;

    }

}
