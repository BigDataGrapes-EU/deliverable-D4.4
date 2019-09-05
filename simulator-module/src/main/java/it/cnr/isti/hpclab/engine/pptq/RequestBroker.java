package it.cnr.isti.hpclab.engine.pptq;

import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.engine.DatastoreReplica;
import it.cnr.isti.hpclab.engine.Shard;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.RequestBroker}, 
 * whose incoming requests are enqueued directly at processing threads (pptq stands for 
 * [p]er-[p]rocessing-[t]hread-[q]ueue).
 * @author Matteo Catena
 *
 */
public class RequestBroker extends it.cnr.isti.hpclab.engine.RequestBroker {

	public RequestBroker(Simulator simulator, CPUBuilder cpuBuilder, int numOfReplicas, Shard... shards) {
		super(simulator, cpuBuilder, numOfReplicas, shards);
	}

	@Override
	protected DatastoreReplica newDatastoreReplicaInstance(CPUBuilder cpuBuilder, int id, Shard... shards) {
		return new it.cnr.isti.hpclab.engine.pptq.DatastoreReplica(this, cpuBuilder, id, shards);
	}
}
