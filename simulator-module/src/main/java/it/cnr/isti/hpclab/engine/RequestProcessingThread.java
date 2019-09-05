package it.cnr.isti.hpclab.engine;

import eu.nicecode.queueing.Request;
import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;
import it.cnr.isti.hpclab.cpu.Core;
import it.cnr.isti.hpclab.engine.ShardServer;
import it.cnr.isti.hpclab.request.RunningRequest;

/**
 * Basically, this is a thread which processes requests. 
 * It runs on a {@link it.cnr.isti.hpclab.cpu.Core} of a {@link it.cnr.isti.hpclab.engine.ShardServer}.
 * @author Matteo Catena
 *
 */
public abstract class RequestProcessingThread implements Agent {

	/**
	 * The server to which this thread belongs
	 */
	protected ShardServer server;
	/**
	 * The CPU core on which the thread is running
	 */
	protected Core core;


	/**
	 * 
	 * @param shardServer The server to which this thread belongs to
	 * @param core The CPU core on which the thread is running
	 */
	public RequestProcessingThread(ShardServer shardServer, Core core) {
		
		this.server = shardServer;
		this.core = core;
		core.setMaxFrequency(server.getDatastoreReplica().getBroker().getSimulator().now().getTimeMicroseconds());
		
	}

	/**
	 * Get a reference to the CPU core on which the thread is running
	 * @return
	 */
	public Core getCore() {
		
		return core;
	}

	/**
	 * Get a reference to the server to which the thread belongs to
	 * @return
	 */
	public ShardServer getShardServer() {
		
		return server;
	}
	
	/**
	 * Process a request
	 * @param request The request to process
	 * @param simulator A reference to the simulator
	 */
	public void processRequest(Request request, Simulator simulator) {

		RunningRequest rq = new RunningRequest(simulator.now().clone(), this, 
				((it.cnr.isti.hpclab.request.Request)request));
		simulator.schedule(rq);
		core.busy(simulator.now().getTimeMicroseconds());
	}
	
	@Override
	public void completeRequest(Request request, Simulator simulator) {

		it.cnr.isti.hpclab.request.Request sq = (it.cnr.isti.hpclab.request.Request) request;
		
		long now = simulator.now().getTimeMicroseconds();
		long arrivalTime = sq.getArrivalTime().getTimeMicroseconds();
		long completionTime = now - arrivalTime;
				
		server.receiveResults(sq.getUid(), completionTime);
		
		afterRequestCompletion(sq, simulator);
	}
}
