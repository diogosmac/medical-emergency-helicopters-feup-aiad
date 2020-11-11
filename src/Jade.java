import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import utils.Logger;
import utils.ScenarioReader;

public class Jade {

    public static void main(String[] args) {

        boolean testOnly = false;

        if (args.length < 1 || args.length > 2) {
            System.out.println(
                    "Usage: java MedicalEmergencyHelicopters <json-file> [ <test> ]\n" +
                    "       json-file:  path to file containing helicopters, hospitals and patients\n" +
                    "       test:       FALSE (default) if logger should document execution, TRUE if testing only"
            );
            System.exit(1);
        }

        if (args.length == 2) {
            testOnly = Boolean.parseBoolean(args[1]);
        }

        Logger.init(testOnly);

        Runtime runt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = runt.createMainContainer(profile);
        String filename = "test_files/test1.json";

        try {
            ScenarioReader.readScenario(mainContainer, filename);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
