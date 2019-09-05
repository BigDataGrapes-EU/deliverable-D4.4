package it.cnr.isti.hpclab.engine.pdrq;

import eu.nicecode.queueing.Request;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.Core;

/**
 * A specialisation of {@link it.cnr.isti.hpclab.engine.RequestProcessingThread}, 
 * whose incoming requests are enqueued at datastore replicas (pdrq stands for 
 * [p]er-[d]atastore-[r]eplica-[q]ueue).
 * @author Matteo Catena
 *
 */
public class RequestProcessingThread extends it.cnr.isti.hpclab.engine.RequestProcessingThread implements Agent {
		
	public RequestProcessingThread(ShardServer shardServer, Core core) {
		
		super(shardServer, core);
	}

	@Override
	public void afterRequestCompletion(Request request, Simulator simulator) {

		Request next = ((ShardServer) server).getNextRequest();
		if (next != null) {
			
			processRequest(next, simulator);
			
		} else {

			core.free(simulator.now().getTimeMicroseconds());
		}
	}

	@Override
	public Request nextRequest() {
		return null;
	}

	@Override
	public void sendRequest(Request request, Agent to, Simulator simulator) {
		//do nothing
	}

	@Override
	public void receiveRequest(Request request, Agent from, Simulator simulator) {
		//do nothing
	}

	/**
	 * Is the thread idling?
	 * @return
	 */
	public boolean isIdle() {

		return !core.isBusy();
	}

}
