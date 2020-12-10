import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import patient.PatientAgent;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.SimInit;
import jade.wrapper.StaleProxyException;
import utils.Logger;
import utils.ScenarioReader;

public class MedicalEmergencyHelicoptersLauncher extends Repast3Launcher {

    private int N = 50;

    private int N_CONTRACTS = 100;

    public static final boolean SEPARATE_CONTAINERS = true;
    private ContainerController mainContainer;
    private ContainerController hospitalContainer;
    private ContainerController helicopterContainer;
    private ContainerController patientContainer;

    private final String jsonPath;

    public MedicalEmergencyHelicoptersLauncher(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public int getN() {
        return N;
    }

    public void setN(int N) {
        this.N = N;
    }

    public int getN_CONTRACTS() {
        return N_CONTRACTS;
    }

    public void setN_CONTRACTS(int N_CONTRACTS) {
        this.N_CONTRACTS = N_CONTRACTS;
    }

    @Override
    public String[] getInitParam() {
        return new String[0];
    }

    @Override
    public String getName() {
        return "MedicalEmergencyHelicopters -- SAJaS Repast3 Test";
    }

    @Override
    public void setup() {
        super.setup();

        // property descriptors
        // ...
    }

    @Override
    public void begin() {
        super.begin();

        // display surfaces, spaces, displays, plots, ...
        // ...
    }

    @Override
    protected void launchJADE() {

        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        p1.setParameter(Profile.GUI, "true");
        mainContainer = rt.createMainContainer(p1);

        if (SEPARATE_CONTAINERS) {
            Profile hospitalProfile = new ProfileImpl();
            hospitalProfile.setParameter(Profile.CONTAINER_NAME, "Hospitals");
            hospitalContainer = rt.createAgentContainer(hospitalProfile);
            Profile helicopterProfile = new ProfileImpl();
            helicopterProfile.setParameter(Profile.CONTAINER_NAME, "Helicopters");
            helicopterContainer = rt.createAgentContainer(helicopterProfile);
            Profile patientProfile = new ProfileImpl();
            patientProfile.setParameter(Profile.CONTAINER_NAME, "Patients");
            patientContainer = rt.createAgentContainer(patientProfile);
        } else {
            hospitalContainer = mainContainer;
            helicopterContainer = mainContainer;
            patientContainer = mainContainer;
        }

        launchAgents();
    }

    private void launchAgents() {

        int N_PATIENTS = N;
        int N_HELICOPTERS = N * 2 / 3;
        int N_HOSPITALS = N / 5;

        try {

            AID resultsCollectorAID = null;
            // create results collector
            ResultsCollector resultsCollector = new ResultsCollector(N_HELICOPTERS);
            mainContainer.acceptNewAgent("ResultsCollector", resultsCollector).start();
            resultsCollectorAID = resultsCollector.getAID();
            ScenarioReader.readScenario(
                    hospitalContainer, helicopterContainer, patientContainer, this.jsonPath, resultsCollectorAID
            );

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    /**
     * Launching Repast3
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java MedicalEmergencyHelicopters <json-file> [ <test> ]\n"
                    + "       json-file:  name of file (from test_files directory) containing helicopters, hospitals and patients\n"
                    + "       test:       FALSE (default) if logger should document execution, TRUE if testing only");
            System.exit(1);
        }

        String jsonPath = "test_files/" + args[0] + ".json";
        Logger.init(Boolean.parseBoolean(args[1]));

        SimInit init = new SimInit();
        init.setNumRuns(1);   // works only in batch mode
        init.loadModel(new MedicalEmergencyHelicoptersLauncher(jsonPath), null, true);

    }

}
