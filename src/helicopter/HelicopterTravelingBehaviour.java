package helicopter;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import utils.Logger;
import utils.PatientFinished;

import java.io.IOException;

public class HelicopterTravelingBehaviour extends WakerBehaviour {
    HelicopterAgent helicopterAgent;
    Integer hospitalSuitability;

    public HelicopterTravelingBehaviour(Agent a, long timeout, Integer hospitalSuitability) {
        super(a, timeout);
        helicopterAgent = (HelicopterAgent) a;
        this.hospitalSuitability = hospitalSuitability;
    }

    @Override
    protected void onWake() {
        helicopterAgent.setBusy(false);
        String logMessage = helicopterAgent.getLocalName() + ": " +
                "another happy customer!";
        Logger.writeLog(logMessage, Logger.HELICOPTER);

        //inform Results Collector
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(new PatientFinished(helicopterAgent.getPatient(), hospitalSuitability));
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(helicopterAgent.getResultsCollector());
        helicopterAgent.send(msg);
    }
}
