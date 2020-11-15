import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import utils.Logger;
import utils.ScenarioReader;

public class MedicalEmergencyHelicopters {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println(
                    "Usage: java MedicalEmergencyHelicopters <json-file> [ <test> ]\n" +
                    "       json-file:  name of file (from test_files directory) containing helicopters, hospitals and patients\n" +
                    "       test:       FALSE (default) if logger should document execution, TRUE if testing only"
            );
            System.exit(1);
        }

        Logger.init(Boolean.parseBoolean(args[1]));

        Runtime runt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = runt.createMainContainer(profile);
        String filename = "test_files/" + args[0] + ".json";

        try {
            Profile hospitalProfile = new ProfileImpl();
            hospitalProfile.setParameter(Profile.CONTAINER_NAME, "Hospitals");
            AgentContainer hospitalContainer = runt.createAgentContainer(hospitalProfile);
            Profile helicopterProfile = new ProfileImpl();
            helicopterProfile.setParameter(Profile.CONTAINER_NAME, "Helicopters");
            AgentContainer helicopterContainer = runt.createAgentContainer(helicopterProfile);
            Profile patientProfile = new ProfileImpl();
            patientProfile.setParameter(Profile.CONTAINER_NAME, "Patients");
            AgentContainer patientContainer = runt.createAgentContainer(patientProfile);
            ScenarioReader.readScenario(hospitalContainer, helicopterContainer, patientContainer, filename);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
