package patient;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class WaitBehaviour extends WakerBehaviour {
    PatientAgent patientAgent;

    public WaitBehaviour(Agent a, long timeout) {
        super(a, timeout);
        patientAgent = (PatientAgent) a;
    }

    @Override
    protected void onWake() {
        patientAgent.addBehaviour(new PatientNetInitiator(this.patientAgent, this.patientAgent.getNumberOfResponders(), new ACLMessage(ACLMessage.CFP)));
    }
}
