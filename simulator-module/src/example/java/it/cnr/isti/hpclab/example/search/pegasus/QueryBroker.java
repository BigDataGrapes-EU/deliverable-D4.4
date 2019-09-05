package it.cnr.isti.hpclab.example.search.pegasus;

import java.util.concurrent.TimeUnit;

import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.DatastoreReplica;
import it.cnr.isti.hpclab.engine.Shard;
import it.cnr.isti.hpclab.engine.pdrq.RequestBroker;
import it.cnr.isti.hpclab.util.MovingStatistics;

/**
 * A search engine-specific implementation of {@link it.cnr.isti.hpclab.engine.pdrq.RequestBroker}
 * for the PEGASUS algorithm [1].
 * 
 * [1] Lo et al. 2014. Towards energy proportionality for large-scale latency-critical workloads. ISCA '14.
 * 
 * @author Matteo Catena
 *
 */
public class QueryBroker extends RequestBroker {

	/**
	 * This object keeps track of the query completion times to determine 
	 * whether the system is meeting its service level objective (SLO)
	 */
	protected MovingStatistics mv95thtile;
	/**
	 * The target service level objective (SLO)
	 */
	protected long sloMicros;
	/**
	 * Power saving choices must not be taken until 'waitUntil' in not passed.
	 */
	protected Time waitUntil;
	
	
	/**
	 * 
	 * @param simulator
	 * @param cpuBuilder
	 * @param slo The service level objective (i.e., queries should be processed within slo ms since their arrival)
	 * @param numOfReplicas
	 * @param shards
	 */
	public QueryBroker(Simulator simulator, CPUBuilder cpuBuilder, Time slo, int numOfReplicas, Shard... shards) {
		super(simulator, cpuBuilder, numOfReplicas, shards);

		sloMicros = slo.getTimeMicroseconds();
		waitUntil = new Time(0, TimeUnit.MINUTES);
		mv95thtile = new MovingStatistics(simulator, 30, TimeUnit.SECONDS);
	}

	@Override
	public void receiveResults(long uid, long completionTime) {
		
		
		mv95thtile.add(completionTime);
		
		//rule engine
		if (simulator.now().compareTo(waitUntil) >= 0) {

			double mv95thtileMicros = mv95thtile.getPercentile(95);

			if (mv95thtileMicros > sloMicros) {

				waitUntil = new Time(simulator.now().getTimeMicroseconds() + TimeUnit.MINUTES.toMicros(5),
						TimeUnit.MICROSECONDS);
				
				setMaxCPUPowerCap();
				

			} else if (completionTime > 1.35 * sloMicros) {
				
				setMaxCPUPowerCap();
				
			} else if (completionTime > sloMicros) {
				
				multiplyCPUPowerCapBy(1.07);
				
			} else if (completionTime <= sloMicros && completionTime >= 0.85 * sloMicros) {
				
				//do nothing
				
			} else if (completionTime < 0.85 * sloMicros) {
				
				multiplyCPUPowerCapBy(0.99);
				
			} else if (completionTime < 0.60 * sloMicros) {
				
				multiplyCPUPowerCapBy(0.97);
			}
		}

		super.receiveResults(uid, completionTime);
	}

	
	private void multiplyCPUPowerCapBy(double d) {
		
		for (DatastoreReplica r : replicas) 
			((IndexReplica)r).multiplyCPUPowerCapBy(d, simulator.now().getTimeMicroseconds());		
	}

	/**
	 * 
	 * Set the CPU power cap to its maximum available value
	 * 
	 */
	protected void setMaxCPUPowerCap() {
		for (DatastoreReplica r : replicas) 
			((IndexReplica)r).setMaxCPUPowerCap(simulator.now().getTimeMicroseconds());
	}
	
	@Override
	protected DatastoreReplica newDatastoreReplicaInstance(CPUBuilder cpuBuilder, int id, Shard... shards) {
				
		return new IndexReplica(this, cpuBuilder, id, shards);
	}
}