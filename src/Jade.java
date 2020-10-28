import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import utils.ScenarioReader;

public class Jade {
    public static void main(String[] args) {
        Runtime runt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = runt.createMainContainer(profile);
        String filename = "test_files/file.txt";

        try {
            ScenarioReader.readScenario(mainContainer, filename);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
