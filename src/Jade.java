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

            // TODO - read file hospitals.txt to parse the hospitals
            // TODO - to be created
            /*
                FORMAT FOR EACH HOSPITAL:
                <position>      - {x, y}
                <competence-1>  - {INJURY_TYPE_1, level-from-0-to-100}
                (...)
                <competence-n>  - {INJURY_TYPE_N, level-from-0-to-100}
                <empty-line>
             */

            /*
            AgentController hos = mainContainer.createNewAgent(
                "hospital",
                "HospitalAgent",
                [position-string, competence-1-string, (...), competence-n-string]
            );
            hos.start();
            */

            AgentController hos = mainContainer.createNewAgent("hospital", "HospitalAgent", null);
            hos.start();


            // TODO - read file helicopters.txt to parse the helicopters
            // TODO - to be created
            /*
                FORMAT FOR EACH HELICOPTER:
                <position>      - {x, y}
                <empty-line>
             */

            /*
            AgentController hos = mainContainer.createNewAgent(
                "hospital",
                "HospitalAgent",
                [position-string]
            );
            hos.start();
            */

            AgentController hel = mainContainer.createNewAgent("helicopter", "PatientAgent", null);
            hel.start();


            // TODO - read file patients.txt to parse the patients
            // TODO - to be created
            /*
                FORMAT FOR EACH PATIENT:
                <position>      - {x, y}
                <injury-type>   - INJURY_TYPE
                <severity>      - level-from-0-to-100
                <empty-line>
             */

            /*
            AgentController hos = mainContainer.createNewAgent(
                "hospital",
                "HospitalAgent",
                [position-string, injury-type-string, severity-string]
            );
            hos.start();
            */

            AgentController pat = mainContainer.createNewAgent("patient", "PatientAgent", null);
            pat.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
