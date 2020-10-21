import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.tools.sniffer.Sniffer;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Jade {
    public static void main(String[] args) {
        Runtime runt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = runt.createMainContainer(profile);
        try {
            AgentController hos = mainContainer.createNewAgent("hospital", "HospitalAgent", null);
            hos.start();


            AgentController hel = mainContainer.createNewAgent("helicopter", "HelicopterAgent", null);
            hel.start();


            AgentController pat = mainContainer.createNewAgent("patient", "PatientAgent", null);
            pat.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
