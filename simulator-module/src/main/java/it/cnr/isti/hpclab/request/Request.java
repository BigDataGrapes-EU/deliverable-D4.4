package it.cnr.isti.hpclab.request;

import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.engine.Shard;

/**
 * This class represents the requests which can be sent to a system
 * @author Matteo Catena
 *
 */
public class Request extends eu.nicecode.queueing.Request {

	protected long rid;
	protected long uid;	
	protected long receivedServiceTime;
	
	/**
	 * 
	 * @param arrivalTime the time of arrival of the request to the system
	 * @param rid The request identifier (different request can have the same rid, e.g., two different queries asking for "new york restaurants" would have the same rid)
	 * @param uid The unique request identifier
	 */
	public Request(Time arrivalTime, long rid, long uid) {
		
		super(arrivalTime, Time.ZERO);
		this.rid = rid;
		this.uid = uid;
	}

	/**
	 * Get the request identifier
	 * @return
	 */
	public long getRid() {
		return rid;
	}
	
	/**
	 * Get the unique identifier
	 * @return
	 */
	public long getUid() {
		
		return uid;
	}
	
	@Override
	public Time getServiceTime() {
		
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Set the amount of service time received by the request for its completion
	 * @param recServTime
	 */
	public void setReceivedServiceTime(long recServTime) {
		
		this.receivedServiceTime = recServTime;
	}

	/**
	 * Get the amount of service time received by the request for its completion
	 * @param recServTime
	 */
	public long getReceivedServiceTime() {
		
		return receivedServiceTime;
	}

	/**
	 * Get the amount of service time required by the request to be completed. 
	 * This depends on the {@code shard} processing the request, and on the core {@code frequency}
	 * of the server hosting the shard.
	 * @param shard The shard which is processing this request
	 * @param frequency The core frequency of the server hosting the shard
	 * @return
	 */
	public Time getServiceTime(Shard shard, int frequency) {

		return shard.getServiceTime(rid, frequency);
	}
}
