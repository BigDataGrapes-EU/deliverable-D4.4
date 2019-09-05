package it.cnr.isti.hpclab.request;

import java.util.concurrent.TimeUnit;

import eu.nicecode.simulator.Event;
import eu.nicecode.simulator.Simulator;
import eu.nicecode.simulator.Time;
import it.cnr.isti.hpclab.engine.RequestProcessingThread;

/**
 * This class represents a request which is being processed by a server.
 * @author Matteo Catena
 *
 */
public class RunningRequest extends Event {

	protected RequestProcessingThread thread;
	protected Request originalRequest;
		
	protected long requiredTime;
	protected long executedTime; //this value make sense only relatively to requireTime, which changes with frequency
	protected long receivedServiceTime;
	
	protected int frequency;
	
	protected boolean firstQuantum = true;

	/**
	 * 
	 * @param time
	 * @param processor
	 * @param originalQuery
	 */
	public RunningRequest(Time time, RequestProcessingThread processor, Request originalQuery) {

		super(time);

		this.thread = processor;
		this.originalRequest = originalQuery;
		
	}

	@Override
	public void execute(Simulator simulator) {

		if (firstQuantum) {
			
			this.frequency = thread.getCore().getFrequency();
			this.executedTime = 0;
			this.requiredTime = originalRequest.getServiceTime(thread.getShardServer().getShard(), frequency).getTimeMicroseconds();
			firstQuantum = false;
			
		}
		
		long inc = TimeUnit.MILLISECONDS.toMicros(1);
		executedTime+=inc;
		receivedServiceTime+=inc;
		time = time.addTime(inc, TimeUnit.MICROSECONDS);
		
		if (executedTime >= requiredTime) {
			
			//conclude
			originalRequest.setReceivedServiceTime(receivedServiceTime);
			RequestCompletion rc = new RequestCompletion(time, thread, originalRequest);
			simulator.schedule(rc);
			
			
		} else {
		
			if (thread.getCore().getFrequency() != frequency && !firstQuantum) {
				
				int newFrequency = thread.getCore().getFrequency();
		
				//re-scale depending on the new core frequency
				long newRequiredTime = originalRequest.getServiceTime(thread.getShardServer().getShard(), frequency).getTimeMicroseconds();
				long newExecutedTime = Math.round((executedTime / ((double) requiredTime)) * newRequiredTime);
				
				frequency = newFrequency;
				requiredTime = newRequiredTime;
				executedTime = newExecutedTime;
			
			}
		
			simulator.schedule(this);
		}
		
	}
	
	/**
	 * Get a reference to the original request
	 * @return
	 */
	public Request getOriginalRequest() {
		
		return originalRequest;
	}
}
