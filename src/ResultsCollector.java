import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultsCollector extends Agent {

	private static final long serialVersionUID = 1L;
	
	private int nResults;
	
	private long startTime = System.currentTimeMillis();

	//era suposto este arrayLIst ser um par onde guardamos: 1- o tempo em que o patiente enviou o primeiro "pedido"; 2- o tempo em que o paciente chega ao hospital.
	private Map<AID, ArrayList<Long>> timeForPatient = new HashMap<>();
	private Map<AID, Integer> treatmentQualityForPatient = new HashMap<>();
	
	public ResultsCollector(int nResults) {
		this.nResults = nResults;
	}
	
	@Override
	public void setup() {
		
		//todo - stuff

		// results listener
		addBehaviour(new ResultsListener());
	}
	
	protected void printResults() {
		long took = System.currentTimeMillis() - startTime;
		System.out.println("Took: \t" + took);

		//todo - actually print results
	}

	
	private class ResultsListener extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		private MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		@Override
		public void action() {

			//todo - ouvir todas as mensagens INFORM enviadas pelo paciente para o results collector -> para cada uma guardar no map o aid do paciente + o tempo autal
			//todo - ouvir todas as mensagens INFORM enviadas pelo helicoptero para o results collector
			// 		-> para cada uma guardar no map, na entrada com o aid do paciente (que vem na mensagem) o tempo atual (no segundo elemento da lista)
			//		-> para cada uma guardar no outro map, uma entrada com o aid do paciente e a qualidade do serviço do hospital (que vem na mensagem)

			/*
			ACLMessage inform = myAgent.receive(template);
			if(inform != null) {
				Results results = null;
				try {
					results = (Results) getContentManager().extractContent(inform);
					ArrayList<ArrayList<ContractOutcome>> resultsForConsumerType = aggregatedResults.get(results.getProviderFilterSize());
					if(resultsForConsumerType == null) {
						resultsForConsumerType = new ArrayList<ArrayList<ContractOutcome>>();
						aggregatedResults.put(results.getProviderFilterSize(), resultsForConsumerType);
					}
					resultsForConsumerType.add(results.getContractOutcomes());
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
				
				if(--nResults == 0) {
					// output results
					printResults();
					
					// shutdown
					try {
						myAgent.getContainerController().getPlatformController().kill();
					} catch (ControllerException e) {
						e.printStackTrace();
					}
				}
			} else {
				block();
			}*/
			
		}
		
	}

}
