package helicopter;

import hospital.HospitalAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import patient.PatientAgent;
import utils.HospitalProposal;
import utils.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

public class HelicopterNetInitiator extends ContractNetInitiator {

    private HelicopterAgent helicopter;
    private int nResponders;

    public HelicopterNetInitiator(HelicopterAgent helicopter, int nResponders, ACLMessage cfp) {
        super(helicopter, cfp);
        this.helicopter = helicopter;
        this.nResponders = nResponders;
    }

    protected Vector prepareCfps(ACLMessage cfp) {
        Vector v = new Vector();
        //TODO - decent try catch
        try {
            cfp.setContentObject(helicopter.getPatientInjuryType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < nResponders; i++) {
            //TODO change this implementation
            cfp.addReceiver(new AID(helicopter.getName(), false));
            //TODO change logger accordingly
            //this.candidate.logger.info("SENT:      " + cfp.getContent() + " TO: " + candidate.getChiefsOfStaff().get(i));
        }
        v.add(cfp);

        return v;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        //TODO change logger accordingly
        System.out.println("Agent " + propose.getSender().getName() + " proposed "+propose.getContent());
    }

    protected void handleRefuse(ACLMessage refuse) {
        //TODO change logger accordingly
        System.out.println("Agent " + refuse.getSender().getName() + " refused");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            //TODO change logger accordingly
            System.out.println("Responder does not exist");
        }
        else {
            //TODO change logger accordingly
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            //TODO change logger accordingly
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }
        // Evaluate proposals.
        double bestProposal = Double.POSITIVE_INFINITY;
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                HospitalProposal proposal = null;

                //TODO decent try catch
                try {
                    proposal = (HospitalProposal) (msg.getContentObject());
                    double distance = proposal.getLocation().getDistance(helicopter.getLocation());
                    Integer levelOfCompetence = proposal.getLevelOfCompetence();
                    int hospitalValue = helicopter.hospitalEvaluation(distance, levelOfCompetence);
                    if (hospitalValue < bestProposal) {
                        bestProposal = hospitalValue;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                } catch (UnreadableException unreadableException) {
                    unreadableException.printStackTrace();
                }
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            //TODO change logger accordingly
            System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

    protected void handleInform(ACLMessage inform) {
        //TODO change logger accordingly
        System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
    }
}
