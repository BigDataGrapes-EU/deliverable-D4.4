package it.cnr.isti.hpclab.engine;

import java.util.Collections;

import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.request.Request;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * A datastore is a repository for storing and managing collections of data, e.g., 
 * an inverted index or a memory caching system. It can be partitioned into 
 * multiple {@link Shard}s to allow for distributed computations. 
 * 
 * @author Matteo Catena
 */
public abstract class DatastoreReplica {

	/**
	 * The broker which feeds requests to this replica and collects the results
	 */
	protected RequestBroker broker;
	/**
	 * The shard servers which compose this replica
	 */
	protected ShardServer[] servers;
	/**
	 * A map to collect the completion times of a request on the different
	 * shards. 
	 * The end-to-end completion time is typically dominated by the longest of such times.
	 */
	protected Long2ObjectMap<LongList> times;
	private int id;

	
	/**
	 * 
	 * @param broker The broker which feeds requests to this replica and collects the results 
	 * @param cpuModel A builder for the shard servers CPUs (every server is equipped with the same CPU model).
	 * @param id The identifier for this replica
	 * @param shards The datastore composing shards.
	 */
	public DatastoreReplica(RequestBroker broker, CPUBuilder cpuModel, int id, Shard... shards) {
		
		this.broker = broker;
		this.id = id;
		servers = new ShardServer[shards.length];
		for (int i = 0; i < shards.length; i++) {
			servers[i] = newShardServerInstance(shards[i], cpuModel, i);
		}
		times = new Long2ObjectOpenHashMap<>();
	}
	
	/**
	 * Get the replica identifier
	 * @return
	 */
	public int getId() {
		
		return id;
		
	}

	/**
	 * Build a new shard server
	 * @param shard The shard which will be hosted by the new server
	 * @param cpuBuilder A builder for the shard server CPU
	 * @param id The identifier of the new shard server
	 * @return
	 */
	protected abstract ShardServer newShardServerInstance(Shard shard, CPUBuilder cpuBuilder, int id);
	
	/**
	 * Receive a new request
	 * @param query
	 */
	public abstract void receiveRequest(Request query);
	
	/**
	 * Receive results for a completed request from a shard server.
	 * @param uid The unique identifier for the completed request
	 * @param completionTime The completion time of the request
	 */
	public void receiveResults(long uid, long completionTime) {
		
		LongList timesList = null;
		if (!times.containsKey(uid)) {
			
			timesList = new LongArrayList(servers.length);
			times.put(uid, timesList);			
		}
		times.get(uid).add(completionTime);
		
		if (times.get(uid).size() == servers.length) {
			
			long actualCompletionTime = Collections.max(times.get(uid));
			broker.receiveResults(uid, actualCompletionTime);
			times.remove(uid);
		}		
	}

    /**
     * Get a reference to the simulator
     * @return
     */
	public Simulator getSimulator() {
		
		return broker.getSimulator();
	}

    /**
     * Get a reference to the broker
     * @return
     */
	public RequestBroker getBroker() {

		return broker;		
	}

	/**
	 * Get the load (i.e., number of request) of this datastore replica
	 * @return
	 */
	public abstract int getLoad();

	/**
	 * Power-off this datastore replica
	 * @param timeMicroseconds The time when the replica must be turn off (in microsec)
	 */
	public void shutdown(long timeMicroseconds) {

		for (ShardServer s : servers) s.shutdown(timeMicroseconds);
	}

	/**
	 * Set every core operating frequency to {@code frequency} at time {@code timeMicroseconds} 
	 * 
	 * @param frequency The target frequency
	 * @param timeMicroseconds The time at which the core switches frequency.
	 */
	public void setFrequency(int freq, long timeMicroseconds) {

		for (ShardServer s : servers) {
			
			for (int i = 0; i < s.cpu.getNumCores(); i++)
				s.cpu.getCore(i).setFrequency(freq, timeMicroseconds);
			
		}
	}
}
