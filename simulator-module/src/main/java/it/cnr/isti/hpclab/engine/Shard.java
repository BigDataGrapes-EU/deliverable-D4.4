package it.cnr.isti.hpclab.engine;

import eu.nicecode.simulator.Time;

/**
 * A shard is a partition of a datastore in a {@link it.cnr.isti.hpclab.engine.DatastoreReplica}.
 * @author Matteo Catena
 *
 */
public abstract class Shard {

	/**
	 * Get the time required to completely process a request (identified by 
	 * a {@code rid}) when the server hosting this shard is operating at 
	 * {@code frequency} Hz.
	 * 
	 * @param rid The request identifier
	 * @param frequency The core frequency at which the shard server operates
	 * @return
	 */
	public abstract Time getServiceTime(long rid, int frequency);
	
}
