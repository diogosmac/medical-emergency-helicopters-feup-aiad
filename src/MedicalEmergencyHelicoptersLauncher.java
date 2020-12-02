import helicopter.HelicopterAgent;
import hospital.HospitalAgent;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import patient.PatientAgent;
import sajas.core.Runtime;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.SimInit;
import jade.wrapper.StaleProxyException;

public class MedicalEmergencyHelicoptersLauncher extends Repast3Launcher {

	private int N = 50;
	
	private int N_CONTRACTS = 100;
	
	public static final boolean USE_RESULTS_COLLECTOR = true;
	
	public static final boolean SEPARATE_CONTAINERS = false;
	private ContainerController mainContainer;
	private ContainerController agentContainer;
	
	public int getN() {
		return N;
	}

	public void setN(int N) {
		this.N = N;
	}

	public int getN_CONTRACTS() {
		return N_CONTRACTS;
	}

	public void setN_CONTRACTS(int N_CONTRACTS) {
		this.N_CONTRACTS = N_CONTRACTS;
	}

	@Override
	public String[] getInitParam() {
		return new String[0];
	}

	@Override
	public String getName() {
		return "MedicalEmergencyHelicopters -- SAJaS Repast3 Test";
	}

	@Override
	protected void launchJADE() {
		
		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);
		
		if(SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			agentContainer = rt.createAgentContainer(p2);
		} else {
			agentContainer = mainContainer;
		}
		
		launchAgents();
	}
	
	private void launchAgents() {
		
		int N_PATIENTS = N;
		int N_HELICOPTERS = N*2/3;
		int N_HOSPITALS = N/5;
		
		try {
			AID resultsCollectorAID = null;
			if(USE_RESULTS_COLLECTOR) {
				// create results collector
				ResultsCollector resultsCollector = new ResultsCollector(N_HELICOPTERS);
				mainContainer.acceptNewAgent("ResultsCollector", resultsCollector).start();
				resultsCollectorAID = resultsCollector.getAID();
			}
			
			// create patients
			for (int i = 0; i < N_PATIENTS; i++) {
				PatientAgent pa = new PatientAgent();
				agentContainer.acceptNewAgent("Patient" + i, pa).start();
			}

			// create helicopters
			for (int i = 0; i < N_HELICOPTERS; i++) {
				HelicopterAgent ha = new HelicopterAgent();
				agentContainer.acceptNewAgent("Helicopter" + i, ha).start();
			}

			// create hospitals
			for (int i = 0; i < N_HOSPITALS; i++) {
				HospitalAgent ha = new HospitalAgent();
				agentContainer.acceptNewAgent("Hospital" + i, ha).start();
			}


		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Launching Repast3
	 * @param args
	 */
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.setNumRuns(1);   // works only in batch mode
		init.loadModel(new MedicalEmergencyHelicoptersLauncher(), null, true);
	}

}
