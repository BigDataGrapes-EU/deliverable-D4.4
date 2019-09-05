package it.cnr.isti.hpclab.engine;

import eu.nicecode.queueing.Request;
import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.CPU;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.cpu.Core;

/**
 * A shard server is a physical server hosting a shard, i.e., a partition of a
 * datastore (e.g., an inverted index). A ShardServer belongs to a {@link it.cnr.isti.hpclab.engine.DatastoreReplica}
 * @author Matteo Catena
 *
 */
public abstract class ShardServer {

	/**
	 * The shard which is held by this server
	 */
	protected Shard shard;
	/**
	 * The datastore replica this shardserver belongs to
	 */
	protected DatastoreReplica replica;
	/**
	 * The threads, running on this server, which process incoming requests.
	 */
	protected RequestProcessingThread[] threads;
	/**
	 * The server cpu.
	 */
	protected CPU cpu;
	/**
	 * The server identifier.
	 */
	protected int id;
	
	/**
	 * 
	 * @param replica The datastore replica to which this server belongs to
	 * @param shard The shard which is held by this server
	 * @param cpuBuilder A builder for the shard server CPU.
	 * @param id The server identifier
	 */
	public ShardServer(DatastoreReplica replica, Shard shard, CPUBuilder cpuBuilder, int id) {

		this.shard = shard;
		this.replica = replica;
		
		this.cpu = cpuBuilder.newInstance(this);
		threads = new RequestProcessingThread[cpu.getNumCores()];
		for (int i = 0; i < cpu.getNumCores(); i++)
			threads[i] = newRequestProcessingThreadInstance(cpu.getCore(i));
		this.id = id;
	}
	
	/**
	 * Instantiate a new thread
	 * @param core The CPU core on which the thread will run
	 * @return
	 */
	protected abstract RequestProcessingThread newRequestProcessingThreadInstance(Core core);

	/**
	 * Receive results for a completed request from a thread.
	 * @param uid The unique identifier for the completed request
	 * @param completionTime The completion time of the request
	 */
	public void receiveResults(long uid, long completionTime) {
		
		replica.receiveResults(uid, completionTime);
	}
	
	/**
	 * Receive a request to process
	 * @param request The request to be processed
	 */
	public abstract void receiveRequest(Request request);

	/**
	 * Get the shard which is held by this server.
	 * @return
	 */
	public Shard getShard() {

		return shard;
	}
	
    /**
     * Get a reference to the simulator
     * @return
     */
	public Simulator getSimulator() {

		return replica.getSimulator();
	}

    /**
     * Get a reference to the datastore replica
     * @return
     */
	public DatastoreReplica getDatastoreReplica() {
		
		return replica;
	}

	/**
	 * Get the server identifier
	 * @return
	 */
	public int getId() {
	
		return id;
	}

	/**
	 * Get the server load (i.e., number of requests to process)
	 * @return
	 */
	public abstract int getLoad();

	/**
	 * Power-off this server
	 * @param timeMicroseconds The time when the server must be turn off (in microsec)
	 */
	public void shutdown(long timeMicroseconds) {

		cpu.shutdown(timeMicroseconds);
		
	}
	
}
