package it.cnr.isti.hpclab.example.search.pesos;

import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.DatastoreReplica;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.engine.pptq.RequestBroker;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.pptq.RequestBroker}
 * for the PESOS algorithm [1].
 * 
 * [1] Catena and Tonellotto. 2017. Energy-Efficient Query Processing in Web Search Engines. TKDE.
 * 
 * @author Matteo Catena
 */
public class QueryBroker extends RequestBroker {

	/**
	 * The time budget (i.e., queries must be processed within 'timeBudget' ms since their arrival
	 */
	protected Time timeBudget;
	/**
	 * The query efficiency predictors: components to estimate the processing cost of queries
	 */
	protected QueryEfficiencyPredictors qep;

	/**
	 * 
	 * @param simulator
	 * @param cpuBuilder
	 * @param timeBudget The time budget per query
	 * @param numOfReplicas
	 * @param shards
	 */
	public QueryBroker(Simulator simulator, CPUBuilder cpuBuilder, Time timeBudget, int numOfReplicas, Shard... shards) {
		super(simulator, cpuBuilder, numOfReplicas, shards);
		qep = new QueryEfficiencyPredictors(cpuBuilder.getFrequencies());		
		this.timeBudget = timeBudget;
	}

	/**
	 * Get the time budget
	 * @return
	 */
	public Time getTimeBudget() {
		return timeBudget;
	}
	
	@Override
	protected DatastoreReplica newDatastoreReplicaInstance(CPUBuilder cpuBuilder, int id, Shard... shards) {
		
		return new IndexReplica(this, cpuBuilder, id, shards);
	}
	
	/**
	 * Get a reference to the query efficiency predictors
	 * @return
	 */
	public QueryEfficiencyPredictors getQueryEfficiencyPredictors() {

		return qep;
			
	}
	
	

}