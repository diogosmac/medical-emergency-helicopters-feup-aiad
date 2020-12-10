import jade.core.AID;
import jade.lang.acl.UnreadableException;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.PatientAccepted;
import utils.PatientFinished;
import utils.PatientInitiating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultsCollector extends Agent {

    private static final long serialVersionUID = 1L;

    private int nResults;

    private long startTime = System.currentTimeMillis();

    //arrayList é um par onde guardamos:
    // 1- date em que o paciente escolhe o helicopter";
    // 2- date em que o paciente é aceite por um helicopter
    // 3- date em que o paciente chega ao hospital. (helicopter finishes traveling behaviour)
    private final Map<AID, ArrayList<Long>> timeForPatient = new HashMap<>();
    private final Map<AID, Integer> treatmentQualityForPatient = new HashMap<>();

    public ResultsCollector(int nResults) {
        this.nResults = nResults;
    }

    @Override
    public void setup() {

        //todo - stuff

        // results listener
        addBehaviour(new ResultsListener(this));
    }

    protected void printResults() {
        long took = System.currentTimeMillis() - startTime;
        System.out.println("Took: \t" + took);

        //todo - actually print results
    }

    private class ResultsListener extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        private MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        protected ResultsListener(Agent agent) {
            super(agent);
            myAgent = agent;
        }

        @Override
        public void action() {

            // ouvir todas as mensagens INFORM enviadas pelo paciente para o results collector -> para cada uma guardar no map o aid do paciente + o tempo autal
            // ouvir todas as mensagens INFORM enviadas pelo helicoptero para o results collector
            // 		-> para cada uma guardar no map, na entrada com o aid do paciente (que vem na mensagem) o tempo atual (no segundo elemento da lista)
            //		-> para cada uma guardar no outro map, uma entrada com o aid do paciente e a qualidade do serviço do hospital (que vem na mensagem)

            ACLMessage msg = myAgent.receive(template);

            if (msg != null) {

                Object content = null;
                try {
                    content = msg.getContentObject();
                    System.out.println(content);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

                // IDEIA: wrapper methods tipo handleMsgContent e que recebem o tipo específico do content
                // ex:  handleMsgContent(String content)
                //      handleMsgContent(PatientFinished content)
                // e depois chamar só handleMsgContent(content), very clean

                // mensagem do paciente
                if (content instanceof PatientInitiating) {
                    AID patient = msg.getSender();

                    ArrayList<Long> times = new ArrayList<>();
                    times.add(System.currentTimeMillis());
                    timeForPatient.put(patient, times);
                }
                // mensagem do helicopter quando deixa o paciente
                else if (content instanceof PatientFinished) {
                    PatientFinished patientFinished = (PatientFinished) content;
                    AID patient = patientFinished.getPatient();
                    Integer hospitalSuitability = patientFinished.getHospitalSuitability();

                    timeForPatient.get(patient).add(System.currentTimeMillis());
                    treatmentQualityForPatient.put(patient, hospitalSuitability);
                }
                // mensagem do helicopter quando aceita o paciente
                else if (content instanceof PatientAccepted) {
                    PatientAccepted patientAccepted= (PatientAccepted) content;
                    AID patient = patientAccepted.getPatient();

                    timeForPatient.get(patient).add(System.currentTimeMillis());
                }

            }

        }

    }

}
