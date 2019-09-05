package it.cnr.isti.hpclab.engine;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import eu.nicecode.queueing.Request;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.CPUBuilder;
import it.cnr.isti.hpclab.simulator.OLDISimulator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

/**
 * A request broker is the component which feeds incoming requests (e.g. queries) 
 * to {@link it.cnr.isti.hpclab.engine.DatastoreReplica} and collects the results.
 * @author Matteo Catena
 *
 */
public abstract class RequestBroker implements Agent {
	
	/**
	 * All the available DatastoreReplicas
	 */
	protected DatastoreReplica[] replicas;
	/**
	 * A map to collect the arrival times of the requests
	 */
	protected Long2LongMap arrivalTimes;
	/**
	 * The underlying jades simulator
	 */
	protected Simulator simulator;
	
	/**
	 * 
	 * @param simulator 
	 * @param cpuBuilder A builder for the shard servers CPUs (every server is equipped with the same CPU model).
	 * @param numOfReplicas the number of datastore replicas
	 * @param shards the shards which compose the datastore
	 */
	public RequestBroker(Simulator simulator, CPUBuilder cpuBuilder, int numOfReplicas, Shard... shards) {

		this.simulator = simulator;
		this.arrivalTimes = new Long2LongOpenHashMap();
		replicas = new DatastoreReplica[numOfReplicas];
		for (int i = 0; i < numOfReplicas; i++)
			replicas[i] = newDatastoreReplicaInstance(cpuBuilder, i, shards);
			
		
	}
	
	@Override
	public Request nextRequest() {
		
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendRequest(Request request, Agent to, Simulator simulator) {

		throw new UnsupportedOperationException();
		
	}

	@Override
	public void receiveRequest(Request request, Agent from, Simulator simulator) {
		
		arrivalTimes.put(((it.cnr.isti.hpclab.request.Request) request).getUid(), request.getArrivalTime().getTimeMicroseconds());		

		
		IntList leastLoadedReplicaList = new IntArrayList(replicas.length);
		int load = Integer.MAX_VALUE;
		for (int i = 0; i < replicas.length; i++) {
			
			int mLoad = replicas[i].getLoad();
			if (mLoad <= load) {
				
				if (mLoad < load) {
					load = mLoad;
					leastLoadedReplicaList.clear();
				}
				leastLoadedReplicaList.add(i);
			}			
		}
		
		if (leastLoadedReplicaList.size() > 1) {

			Collections.shuffle(leastLoadedReplicaList);
		}
		
		replicas[leastLoadedReplicaList.getInt(0)].receiveRequest((it.cnr.isti.hpclab.request.Request) request);
	}

	@Override
	public void completeRequest(Request request, Simulator simulator) {
		// do nothing
	}

	@Override
	public void afterRequestCompletion(Request request, Simulator simulator) {
		// do nothing
	}
	
	/**
	 * Instantiate a new {@link it.cnr.isti.hpclab.engine.DatastoreReplica}
	 * @param cpu A builder for the shard servers CPUs (every server is equipped with the same CPU model)
	 * @param id The datastore replica identifier
	 * @param shards the shards which compose the datastore
	 * @return
	 */
	protected abstract DatastoreReplica newDatastoreReplicaInstance(CPUBuilder cpu, int id, Shard... shards);

	/**
	 * Receive the results for a completed request
	 * @param uid The unique identifier of the request
	 * @param completionTime The completion time of the request
	 */
	public void receiveResults(long uid, long completionTime) {

		long arrivalTime = arrivalTimes.remove(uid); //get and remove;
		String out = String.format("[broker] %d %.3f", TimeUnit.MICROSECONDS.toSeconds(arrivalTime), completionTime/1e3);
		((OLDISimulator) simulator).println(out);
	}

	/**
	 * Get a reference to the simulator
	 * @return
	 */
	public Simulator getSimulator() {
	
		return simulator;
	}

	/**
	 * Power-off this request broker
	 * @param timeMicroseconds The time when the broker must be turn off (in microsec)
	 */
	public void shutdown(long timeMicroseconds) {

		for (DatastoreReplica r : replicas) r.shutdown(timeMicroseconds);
	}

	/**
	 * Set every core operating frequency to {@code frequency} at time {@code timeMicroseconds}
	 * 
     * @param frequency The target frequency
	 * @param timeMicroseconds The time at which the core switches frequency.
	 */
	public void setFrequency(int freq, long timeMicroseconds) {
		
		for (DatastoreReplica r : replicas) r.setFrequency(freq, timeMicroseconds);
	}	
}
