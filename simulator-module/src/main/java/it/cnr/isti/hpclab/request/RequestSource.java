package it.cnr.isti.hpclab.request;

import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.Simulator;

/**
 * This class represent a source of requests.
 * @author Matteo Catena
 */
public abstract class RequestSource extends eu.nicecode.queueing.RequestSource {

	/**
	 * Check whether the RequestSource has finished to generate new requests
	 * @return
	 */
	public abstract boolean isDone();
	
	/**
	 * Start generating new resources, to be sent to {@code Agent to}.
	 * @param simulator The underlying jades' simulator
	 * @param to The agent which will receive the generated requests
	 */
	public abstract void generate(Simulator simulator, Agent to);
}
