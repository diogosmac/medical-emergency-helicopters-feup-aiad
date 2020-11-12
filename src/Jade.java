import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import utils.Logger;
import utils.ScenarioReader;

public class Jade {

    public static void main(String[] args) {

        if (args.length != 5) {
            System.out.println(
                    "Usage: java MedicalEmergencyHelicopters -gui -name <name> <json-file> [ <test> ]\n" +
                    "       name:       name of the program\n" +
                    "       json-file:  path to file (from test_files directory) containing helicopters, hospitals and patients\n" +
                    "       test:       FALSE (default) if logger should document execution, TRUE if testing only"
            );
            System.exit(1);
        }

        Logger.init(Boolean.parseBoolean(args[4]));

        Runtime runt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = runt.createMainContainer(profile);
        String filename = "test_files/" + args[3];

        try {
            ScenarioReader.readScenario(mainContainer, filename);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
