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
        Profile p1 = new ProfileImpl();
        p1.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = runt.createMainContainer(p1);
        AgentController ac;
        try {
            ac = mainContainer.createNewAgent("joao", "NewAgent", null);
            ac.start();


            AgentController ac1 = mainContainer.createNewAgent("continente", "FIPAContractNetInitiatorAgent", null);
            ac1.start();


            AgentController ac2 = mainContainer.createNewAgent("joaom", "FIPAContractNetResponderAgent", null);
            ac2.start();


            AgentController ac3 = mainContainer.createNewAgent("joaop", "FIPAContractNetResponderAgent", null);
            ac3.start();


            AgentController ac4 = mainContainer.createNewAgent("joaov", "FIPAContractNetResponderAgent", null);
            ac4.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
