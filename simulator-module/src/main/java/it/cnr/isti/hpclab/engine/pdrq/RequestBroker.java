package it.cnr.isti.hpclab.engine.pdrq;

import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.DatastoreReplica;
import it.cnr.isti.hpclab.engine.Shard;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.RequestBroker}, 
 * whose incoming requests are enqueued at datastore replicas (pdrq stands for 
 * [p]er-[d]atastore-[r]eplica-[q]ueue).
 * @author Matteo Catena
 *
 */
public class RequestBroker extends it.cnr.isti.hpclab.engine.RequestBroker implements Agent {
	
	public RequestBroker(Simulator simulator, CPUBuilder cpuBuilder, int numOfReplicas, Shard... shards) {
		super(simulator, cpuBuilder, numOfReplicas, shards);
	}	
	
	@Override
	protected DatastoreReplica newDatastoreReplicaInstance(CPUBuilder cpuBuilder, int id, Shard... shards) {
		
		return new it.cnr.isti.hpclab.engine.pdrq.DatastoreReplica(this, cpuBuilder, id, shards);
	}
}
