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
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.network.DefaultDrawableNode;
import utils.Logger;
import utils.ScenarioReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MedicalEmergencyHelicoptersLauncher extends Repast3Launcher {

    private int N = 50;

    private int N_CONTRACTS = 100;

    public static final boolean SEPARATE_CONTAINERS = true;
    private ContainerController mainContainer;
    private ContainerController hospitalContainer;
    private ContainerController helicopterContainer;
    private ContainerController patientContainer;

    private int mapWidth;
    private int mapLength;
    private int numPatients;
    private int numHelicopters;
    private int numHospitals;
    private int minPatientSeverity;
    private int maxPatientSeverity;
    private int minHelicopterRange;
    private int maxHelicopterRange;
    private int minHelicopterSpeed;
    private int maxHelicopterSpeed;

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
        return new String[]{
            "numPatients",
            "numHelicopters",
            "numHospitals",
            "minPatientSeverity",
            "maxPatientSeverity",
            "minHelicopterRange",
            "maxHelicopterRange",
            "minHelicopterSpeed",
            "maxHelicopterSpeed"
        };
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
        Random random = new Random(this.getRngSeed());
        setMapWidth(100);
        setMapLength(100);
        setNumPatients(random.nextInt(50));
        setNumHelicopters(random.nextInt(15));
        setNumHospitals(5);
        setMinPatientSeverity(random.nextInt(50));
        setMaxPatientSeverity(random.nextInt() % (100 - getMinPatientSeverity()) + getMinPatientSeverity());
        setMinHelicopterRange(random.nextInt(100));
        setMaxHelicopterRange(random.nextInt() % (100 - getMinHelicopterRange()) + getMinHelicopterRange());
        setMinHelicopterSpeed(random.nextInt(15));
        setMaxHelicopterSpeed(random.nextInt() % (50 - getMinHelicopterSpeed()) + getMinHelicopterSpeed());

    }

    @Override
    public void begin() {
        super.begin();

        // display surfaces, spaces, displays, plots, ...
        // ...

        displayNetworkModel();
    }

    private DisplaySurface dsurf;
    private List<DefaultDrawableNode> nodes;
    private int WIDTH = 0, HEIGHT = 0;

    private void buildNetworkModel() {
        nodes = new ArrayList<DefaultDrawableNode>();
        for (HospitalAgent hospital: ScenarioReader.getHospitalAgents()) {
            nodes.add(hospital.getNode());
            if (hospital.getLocation().getX() * 10 >= WIDTH) {
                WIDTH = (int) (hospital.getLocation().getX() * 11);
            }
            if (hospital.getLocation().getY() * 10 >= HEIGHT) {
                HEIGHT = (int) (hospital.getLocation().getY() * 11);
            }
        }
        for (HelicopterAgent helicopter: ScenarioReader.getHelicopterAgents()) {
            nodes.add(helicopter.getNode());
            if (helicopter.getLocation().getX() * 10 >= WIDTH) {
                WIDTH = (int) (helicopter.getLocation().getX() * 11);
            }
            if (helicopter.getLocation().getY() * 10 >= HEIGHT) {
                HEIGHT = (int) (helicopter.getLocation().getY() * 11);
            }
        }
        for (PatientAgent patient: ScenarioReader.getPatientAgents()) {
            nodes.add(patient.getNode());
            if (patient.getPosition().getX() * 10 >= WIDTH) {
                WIDTH = (int) (patient.getPosition().getX() * 11);
            }
            if (patient.getPosition().getY() * 10 >= HEIGHT) {
                HEIGHT = (int) (patient.getPosition().getY() * 11);
            }
        }
        WIDTH += 50;
        HEIGHT += 50;
    }

    private void displayNetworkModel() {
        if (dsurf != null) {
            dsurf.dispose();
        }

        dsurf = new DisplaySurface(this, "Service Consumer/Provider Display");
        registerDisplaySurface("Service Consumer/Provider Display", dsurf);
        Network2DDisplay display = new Network2DDisplay(nodes,WIDTH,HEIGHT);
        dsurf.addDisplayableProbeable(display, "Network Display");
        dsurf.addZoomable(display);
        addSimEventListener(dsurf);
        dsurf.display();

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
            // create results collector
            ResultsCollector resultsCollector = new ResultsCollector(N_HELICOPTERS);
            mainContainer.acceptNewAgent("ResultsCollector", resultsCollector).start();
            AID resultsCollectorAID = resultsCollector.getAID();
            ScenarioReader.readScenario(
                    hospitalContainer, helicopterContainer, patientContainer, this.jsonPath, resultsCollectorAID
            );
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        buildNetworkModel();

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
        init.loadModel(new MedicalEmergencyHelicoptersLauncher(jsonPath), null, false);

    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int setMapLength() {
        return mapLength;
    }

    public void setMapLength(int mapLength) {
        this.mapLength = mapLength;
    }

    public int getNumPatients() {
        return numPatients;
    }

    public void setNumPatients(int numPatients) {
        this.numPatients = numPatients;
    }

    public int getNumHelicopters() {
        return numHelicopters;
    }

    public void setNumHelicopters(int numHelicopters) {
        this.numHelicopters = numHelicopters;
    }

    public int getNumHospitals() {
        return numHospitals;
    }

    public void setNumHospitals(int numHospitals) {
        this.numHospitals = numHospitals;
    }

    public int getMinPatientSeverity() {
        return minPatientSeverity;
    }

    public void setMinPatientSeverity(int minPatientSeverity) {
        this.minPatientSeverity = minPatientSeverity;
    }

    public int getMaxPatientSeverity() {
        return maxPatientSeverity;
    }

    public void setMaxPatientSeverity(int maxPatientSeverity) {
        this.maxPatientSeverity = maxPatientSeverity;
    }

    public int getMinHelicopterRange() {
        return minHelicopterRange;
    }

    public void setMinHelicopterRange(int minHelicopterRange) {
        this.minHelicopterRange = minHelicopterRange;
    }

    public int getMaxHelicopterRange() {
        return maxHelicopterRange;
    }

    public void setMaxHelicopterRange(int maxHelicopterRange) {
        this.maxHelicopterRange = maxHelicopterRange;
    }

    public int getMinHelicopterSpeed() {
        return minHelicopterSpeed;
    }

    public void setMinHelicopterSpeed(int minHelicopterSpeed) {
        this.minHelicopterSpeed = minHelicopterSpeed;
    }

    public int getMaxHelicopterSpeed() {
        return maxHelicopterSpeed;
    }

    public void setMaxHelicopterSpeed(int maxHelicopterSpeed) {
        this.maxHelicopterSpeed = maxHelicopterSpeed;
    }

}
