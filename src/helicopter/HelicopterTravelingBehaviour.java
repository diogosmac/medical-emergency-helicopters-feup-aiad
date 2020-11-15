package helicopter;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import utils.Logger;

public class HelicopterTravelingBehaviour extends WakerBehaviour {
    HelicopterAgent helicopterAgent;

    public HelicopterTravelingBehaviour(Agent a, long timeout) {
        super(a, timeout);
        helicopterAgent = (HelicopterAgent) a;
    }

    @Override
    protected void onWake() {
        helicopterAgent.setBusy(false);
        String logMessage = helicopterAgent.getLocalName() + ": " +
                "another happy customer!";
        Logger.writeLog(logMessage, Logger.HELICOPTER);
    }
}
