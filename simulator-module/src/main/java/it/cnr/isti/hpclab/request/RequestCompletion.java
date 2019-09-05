package it.cnr.isti.hpclab.request;

import eu.nicecode.simulator.Agent;
import eu.nicecode.simulator.ComplexEvent;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;

/**
 * The instances of this class represent the completion of the processing of 
 * a {@link it.cnr.isti.hpclab.request.Request}.
 * @author Matteo Catena
 *
 */
public class RequestCompletion extends ComplexEvent {

	protected Request request;

	/**
	 * 
	 * @param time The time at which the request processing has been completed
	 * @param from The agent completing the request
	 * @param request The original request
	 */
	public RequestCompletion(Time time, Agent from, Request request) {
		super(time, from, null);
		this.request = request;
	}

	@Override
	public void execute(Simulator simulator) {

		from.completeRequest(this.request, simulator);
		
	}

}
